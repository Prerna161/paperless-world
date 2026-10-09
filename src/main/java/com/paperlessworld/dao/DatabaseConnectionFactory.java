package com.paperlessworld.dao;

import com.paperlessworld.exception.DataAccessException;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe JDBC Connection Factory.
 * Reads properties from classpath or falls back to standard local defaults.
 */
public final class DatabaseConnectionFactory {
    private static String jdbcUrl;
    private static String dbUser;
    private static String dbPassword;

    static {
        loadConfiguration();
    }

    private DatabaseConnectionFactory() {}

    private static void loadConfiguration() {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception ignored) {
            // Fall back to environment or defaults
        }

        jdbcUrl = System.getenv().getOrDefault("DB_URL", 
            props.getProperty("db.url", "jdbc:mysql://localhost:3306/paperless_world?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
        dbUser = System.getenv().getOrDefault("DB_USER", 
            props.getProperty("db.user", "root"));
        dbPassword = System.getenv().getOrDefault("DB_PASSWORD", 
            props.getProperty("db.password", "root"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Driver may also be loaded automatically by JDBC 4.0+
        }
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
        } catch (SQLException e) {
            throw new DataAccessException("Unable to establish MySQL connection to: " + jdbcUrl, e);
        }
    }
}
