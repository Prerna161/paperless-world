package com.paperlessworld.dao;

import com.paperlessworld.model.User;

import java.sql.*;
import java.time.Instant;
import java.util.Optional;

public class UserDao {

    public long insert(Connection c, User u) throws SQLException {
        String sql = "INSERT INTO users (email, display_name, password_hash, demo_offset_days, failed_logins, locked_until) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getDisplayName());
            ps.setString(3, u.getPasswordHash());
            ps.setInt(4, u.getDemoOffsetDays());
            ps.setInt(5, u.getFailedLogins());
            ps.setTimestamp(6, u.getLockedUntil() != null ? Timestamp.from(u.getLockedUntil()) : null);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    u.setUserId(id);
                    return id;
                }
            }
        }
        return 0L;
    }

    public Optional<User> findByEmail(Connection c, String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Connection c, long userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public void updateDemoOffset(Connection c, long userId, int offsetDays) throws SQLException {
        String sql = "UPDATE users SET demo_offset_days = ? WHERE user_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, offsetDays);
            ps.setLong(2, userId);
            ps.executeUpdate();
        }
    }

    public void recordFailedLogin(Connection c, String email, Instant lockedUntil) throws SQLException {
        String sql = "UPDATE users SET failed_logins = failed_logins + 1, locked_until = ? WHERE email = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, lockedUntil != null ? Timestamp.from(lockedUntil) : null);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public void resetFailedLogins(Connection c, String email) throws SQLException {
        String sql = "UPDATE users SET failed_logins = 0, locked_until = NULL WHERE email = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getLong("user_id"));
        u.setEmail(rs.getString("email"));
        u.setDisplayName(rs.getString("display_name"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setDemoOffsetDays(rs.getInt("demo_offset_days"));
        u.setFailedLogins(rs.getInt("failed_logins"));
        Timestamp lu = rs.getTimestamp("locked_until");
        if (lu != null) u.setLockedUntil(lu.toInstant());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toInstant());
        return u;
    }
}
