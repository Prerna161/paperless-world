package com.paperlessworld.service;

import com.paperlessworld.dao.ActionDao;
import com.paperlessworld.dao.EventDao;
import com.paperlessworld.dao.ThoughtDao;
import com.paperlessworld.model.ClosureReason;
import com.paperlessworld.model.IdeaThought;
import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtState;
import com.paperlessworld.util.TimeSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class CloseTransactionTest {

    @Test
    @DisplayName("closeTransactionRollsBackWhenEventInsertFails")
    void closeTransactionRollsBackWhenEventInsertFails() {
        AtomicBoolean rollbackCalled = new AtomicBoolean(false);
        AtomicBoolean commitCalled = new AtomicBoolean(false);

        Connection mockConnection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAutoCommit" -> true;
                    case "setAutoCommit" -> null;
                    case "commit" -> {
                        commitCalled.set(true);
                        yield null;
                    }
                    case "rollback" -> {
                        rollbackCalled.set(true);
                        yield null;
                    }
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

        ThoughtDao mockDao = new ThoughtDao() {
            @Override
            public Optional<Thought> findById(Connection c, long thoughtId, long userId) {
                IdeaThought t = new IdeaThought(thoughtId, userId, "tok", "Idea", "body",
                        ThoughtState.OPEN, null, 3, 0L, Instant.now(), Instant.now(), Instant.now(), null);
                return Optional.of(t);
            }

            @Override
            public boolean updateState(Connection c, long thoughtId, long userId, ThoughtState newState,
                                       ClosureReason reason, long expectedVersion, Instant now) {
                return true;
            }
        };

        // EventDao deliberately simulates failure on step 2 (INSERT thought_events)
        EventDao failingEventDao = new EventDao() {
            @Override
            public void insert(Connection c, long thoughtId, String eventType, String fromState, String toState, String note, Instant now) {
                throw new RuntimeException("Simulated disk error during audit event write");
            }
        };

        ThoughtService service = new ThoughtService(mockDao, new ActionDao(), failingEventDao, txManager);

        assertThrows(RuntimeException.class, () -> 
                service.close(1L, 10L, ClosureReason.DONE, TimeSource.system()));

        assertTrue(rollbackCalled.get(), "TransactionManager must call rollback() when any step in closure fails");
        assertFalse(commitCalled.get(), "commit() must never be called on failure");
    }
}
