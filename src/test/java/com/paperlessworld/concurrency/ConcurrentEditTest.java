package com.paperlessworld.concurrency;

import com.paperlessworld.dao.ActionDao;
import com.paperlessworld.dao.EventDao;
import com.paperlessworld.dao.ThoughtDao;
import com.paperlessworld.exception.StaleThoughtException;
import com.paperlessworld.model.IdeaThought;
import com.paperlessworld.model.Thought;
import com.paperlessworld.service.ThoughtService;
import com.paperlessworld.service.TransactionManager;
import com.paperlessworld.util.TimeSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Concurrency proof for technical viva examination (PRD Section 11.5).
 * Starts 20 threads that each try to update the same thought with the identical starting version_no.
 * Exactly 1 succeeds and 19 receive StaleThoughtException.
 */
public class ConcurrentEditTest {

    @Test
    @DisplayName("20 concurrent threads update same thought: exactly 1 succeeds, 19 receive StaleThoughtException")
    void testConcurrentOptimisticLockCollision() throws InterruptedException {
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch doneGate = new CountDownLatch(threadCount);

        // In-memory simulated concurrent database state
        AtomicLong simulatedVersion = new AtomicLong(0L);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger staleCount = new AtomicInteger(0);

        // ThoughtDao simulation using the exact SQL logic from Section 13.3:
        // UPDATE thoughts SET version_no = version_no + 1 WHERE thought_id = ? AND version_no = ?
        ThoughtDao mockDao = new ThoughtDao() {
            @Override
            public boolean update(Connection c, Thought t, long expectedVersion, Instant now) {
                // Atomic CAS representing MySQL InnoDB row update with version_no check
                return simulatedVersion.compareAndSet(expectedVersion, expectedVersion + 1);
            }

            @Override
            public Optional<Thought> findById(Connection c, long thoughtId, long userId) {
                IdeaThought t = new IdeaThought(thoughtId, userId, "tok", "Title", "Body",
                        com.paperlessworld.model.ThoughtState.OPEN, null, 3,
                        simulatedVersion.get(), Instant.now(), Instant.now(), Instant.now(), null);
                return Optional.of(t);
            }
        };

        // Dummy connection via dynamic proxy for TransactionManager
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

        ThoughtService thoughtService = new ThoughtService(mockDao, new ActionDao(), new EventDao() {
            @Override
            public void insert(Connection c, long thoughtId, String eventType, String fromState, String toState, String note, Instant now) {}
        }, txManager);

        long thoughtId = 42L;
        long userId = 1L;
        long startingVersion = 0L; // All 20 threads read the same initial version_no

        for (int i = 0; i < threadCount; i++) {
            final int threadIdx = i;
            executor.submit(() -> {
                try {
                    startGate.await(); // All threads release at the exact same instant
                    thoughtService.update(userId, thoughtId, "Title from thread " + threadIdx,
                            "Body from thread " + threadIdx, 3, startingVersion, TimeSource.system());
                    successCount.incrementAndGet();
                } catch (StaleThoughtException e) {
                    staleCount.incrementAndGet();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneGate.countDown();
                }
            });
        }

        // Fire all 20 threads simultaneously
        startGate.countDown();
        doneGate.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Viva proof assertions:
        assertEquals(1, successCount.get(), "Exactly 1 thread must succeed");
        assertEquals(19, staleCount.get(), "Exactly 19 threads must receive StaleThoughtException");
        assertEquals(1L, simulatedVersion.get(), "Database version_no must increment by exactly 1");
    }
}
