package com.paperlessworld.exception;

/**
 * Wraps unchecked SQLExceptions at the transaction and DAO boundary.
 */
public class DatabaseOperationException extends RuntimeException {
    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseOperationException(String message) {
        super(message);
    }
}
