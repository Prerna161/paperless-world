package com.paperlessworld.service;

import com.paperlessworld.dao.UserDao;
import com.paperlessworld.exception.ValidationException;
import com.paperlessworld.model.User;
import com.paperlessworld.util.PasswordHasher;
import com.paperlessworld.util.TimeSource;

import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class UserService {
    private final UserDao userDao;
    private final TransactionManager txManager;

    public UserService(UserDao userDao, TransactionManager txManager) {
        this.userDao = userDao;
        this.txManager = txManager;
    }

    public User authenticate(String email, String password, TimeSource timeSource) {
        Instant now = timeSource.now();
        return txManager.inTransaction(conn -> {
            try {
                Optional<User> userOpt = userDao.findByEmail(conn, email);
                if (userOpt.isEmpty()) {
                    // Constant-time dummy check to prevent timing attacks
                    PasswordHasher.verify("dummy".toCharArray(), "600000:c2FsdA==:aGFzaA==");
                    throw new ValidationException("Invalid email or password.");
                }

                User user = userOpt.get();
                if (user.isLocked(now)) {
                    long minutesLeft = Duration.between(now, user.getLockedUntil()).toMinutes() + 1;
                    throw new ValidationException("Account temporarily locked due to too many failed attempts. Try again in " + minutesLeft + " minutes.");
                }

                boolean valid = PasswordHasher.verify(password.toCharArray(), user.getPasswordHash());
                if (!valid) {
                    Instant lockedUntil = null;
                    if (user.getFailedLogins() + 1 >= 5) {
                        lockedUntil = now.plus(Duration.ofMinutes(10));
                    }
                    userDao.recordFailedLogin(conn, email, lockedUntil);
                    throw new ValidationException("Invalid email or password.");
                }

                // Reset failed login counter on success
                if (user.getFailedLogins() > 0) {
                    userDao.resetFailedLogins(conn, email);
                }

                return user;
            } catch (SQLException e) {
                throw new RuntimeException("Authentication failed", e);
            }
        });
    }

    public User register(String email, String displayName, String password) {
        String hash = PasswordHasher.hash(password.toCharArray());
        User u = new User(email.trim().toLowerCase(), displayName.trim(), hash);

        return txManager.inTransaction(conn -> {
            try {
                Optional<User> existing = userDao.findByEmail(conn, u.getEmail());
                if (existing.isPresent()) {
                    throw new ValidationException("Email is already registered.");
                }
                long id = userDao.insert(conn, u);
                u.setUserId(id);
                return u;
            } catch (SQLException e) {
                throw new RuntimeException("Registration failed", e);
            }
        });
    }

    public void updateDemoOffset(long userId, int offsetDays) {
        txManager.inTransaction(conn -> {
            try {
                userDao.updateDemoOffset(conn, userId, offsetDays);
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to update demo offset", e);
            }
        });
    }

    public Optional<User> findById(long userId) {
        return txManager.inTransaction(conn -> {
            try {
                return userDao.findById(conn, userId);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load user", e);
            }
        });
    }
}
