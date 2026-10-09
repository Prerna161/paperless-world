package com.paperlessworld.security;

import com.paperlessworld.dao.ActionDao;
import com.paperlessworld.dao.EventDao;
import com.paperlessworld.dao.ThoughtDao;
import com.paperlessworld.exception.NotFoundException;
import com.paperlessworld.model.ClosureReason;
import com.paperlessworld.model.IdeaThought;
import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtState;
import com.paperlessworld.service.ThoughtService;
import com.paperlessworld.service.TransactionManager;
import com.paperlessworld.util.TimeSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ThoughtOwnershipSecurityTest {

    private final long USER_A = 101L;
    private final long USER_B = 202L;
    private final long THOUGHT_ID = 505L;

    @Test
    @DisplayName("userBCannotReadUserAThoughtByGuessingId")
    void userBCannotReadUserAThoughtByGuessingId() {
        ThoughtDao mockDao = new ThoughtDao() {
            @Override
            public Optional<Thought> findById(Connection c, long thoughtId, long userId) {
                // Returns row ONLY when userId matches owner (USER_A)
                if (thoughtId == THOUGHT_ID && userId == USER_A) {
                    return Optional.of(new IdeaThought(thoughtId, USER_A, "tok", "Private idea", "secret",
                            ThoughtState.OPEN, null, 3, 0L, Instant.now(), Instant.now(), Instant.now(), null));
                }
                return Optional.empty(); // Enforces AND user_id = ?
            }
        };

        ThoughtService service = createService(mockDao);

        // User B attempts to read User A's thought -> throws NotFoundException (404 without leaking existence)
        assertThrows(NotFoundException.class, () -> service.getThought(USER_B, THOUGHT_ID));
    }

    @Test
    @DisplayName("userBCannotUpdateUserAThoughtByGuessingId")
    void userBCannotUpdateUserAThoughtByGuessingId() {
        ThoughtDao mockDao = new ThoughtDao() {
            @Override
            public Optional<Thought> findById(Connection c, long thoughtId, long userId) {
                if (thoughtId == THOUGHT_ID && userId == USER_A) {
                    return Optional.of(new IdeaThought(thoughtId, USER_A, "tok", "Private idea", "secret",
                            ThoughtState.OPEN, null, 3, 0L, Instant.now(), Instant.now(), Instant.now(), null));
                }
                return Optional.empty();
            }
        };

        ThoughtService service = createService(mockDao);

        assertThrows(NotFoundException.class, () -> 
                service.update(USER_B, THOUGHT_ID, "Hacked", "body", 5, 0L, TimeSource.system()));
    }

    @Test
    @DisplayName("userBCannotCloseUserAThoughtByGuessingId")
    void userBCannotCloseUserAThoughtByGuessingId() {
        ThoughtDao mockDao = new ThoughtDao() {
            @Override
            public Optional<Thought> findById(Connection c, long thoughtId, long userId) {
                if (thoughtId == THOUGHT_ID && userId == USER_A) {
                    return Optional.of(new IdeaThought(thoughtId, USER_A, "tok", "Private idea", "secret",
                            ThoughtState.OPEN, null, 3, 0L, Instant.now(), Instant.now(), Instant.now(), null));
                }
                return Optional.empty();
            }
        };

        ThoughtService service = createService(mockDao);

        assertThrows(NotFoundException.class, () -> 
                service.close(USER_B, THOUGHT_ID, ClosureReason.DONE, TimeSource.system()));
    }

    private ThoughtService createService(ThoughtDao dao) {
        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAutoCommit" -> true;
                    case "isClosed" -> false;
                    default -> null;
                }
        );

        DataSource mockDataSource = new DataSource() {
            @Override public Connection getConnection() { return mockConnection; }
            @Override public Connection getConnection(String u, String p) { return mockConnection; }
            @Override public PrintWriter getLogWriter() { return null; }
            @Override public void setLogWriter(PrintWriter out) {}
            @Override public void setLoginTimeout(int seconds) {}
            @Override public int getLoginTimeout() { return 0; }
            @Override public Logger getParentLogger() { return null; }
            @Override public <T> T unwrap(Class<T> iface) { return null; }
            @Override public boolean isWrapperFor(Class<?> iface) { return false; }
        };

        TransactionManager txManager = new TransactionManager(mockDataSource);
        return new ThoughtService(dao, new ActionDao(), new EventDao(), txManager);
    }
}
