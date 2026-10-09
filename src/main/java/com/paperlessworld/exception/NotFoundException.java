package com.paperlessworld.exception;

/**
 * Thrown when a requested resource is not found or is not owned by the session user.
 * Avoids leaking ownership details by returning 404.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
