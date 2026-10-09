package com.paperlessworld.service;

import com.paperlessworld.dao.DatabaseConnectionFactory;
import com.paperlessworld.exception.DatabaseOperationException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * Transaction helper matching PRD Section 13.2.
 * Services never call commit or rollback themselves; all operations execute within inTransaction.
 */
public final class TransactionManager {
    private final DataSource dataSource;

    public TransactionManager() {
        this.dataSource = lookupDataSource();
    }

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private static DataSource lookupDataSource() {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            return (DataSource) envCtx.lookup("jdbc/paperless");
        } catch (NamingException e) {
            // Fallback for standalone tests or when running outside full JNDI container
            return new FallbackDataSource();
        }
    }

    public <R> R inTransaction(Function<Connection, R> work) {
        try (Connection c = dataSource.getConnection()) {
            boolean oldAuto = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                R result = work.apply(c);
                c.commit();
                return result;
            } catch (RuntimeException e) {
                try {
                    c.rollback();
                } catch (SQLException rollbackEx) {
                    // Suppress secondary rollback exception and throw original business exception
                    e.addSuppressed(rollbackEx);
                }
                throw e; // business exceptions pass through unchanged
            } finally {
                try {
                    c.setAutoCommit(oldAuto);
                } catch (SQLException ignored) {}
            }
        } catch (SQLException e) {
            throw new DatabaseOperationException("Database operation failed", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static class FallbackDataSource implements DataSource {
        @Override
        public Connection getConnection() {
            return DatabaseConnectionFactory.getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) {
            return DatabaseConnectionFactory.getConnection();
        }

        @Override public java.io.PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(java.io.PrintWriter out) {}
        @Override public void setLoginTimeout(int seconds) {}
        @Override public int getLoginTimeout() { return 0; }
        @Override public java.util.logging.Logger getParentLogger() { return null; }
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    }
}
