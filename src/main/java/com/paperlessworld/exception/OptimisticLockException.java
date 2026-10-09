package com.paperlessworld.exception;

/**
 * Thrown when an update detects that the record was modified by another request
 * (version_number in database does not match the version_number submitted by client).
 */
public class OptimisticLockException extends RuntimeException {
    private final long thoughtId;
    private final int expectedVersion;

    public OptimisticLockException(long thoughtId, int expectedVersion) {
        super("Thought #" + thoughtId + " was modified concurrently. Expected version " + expectedVersion + ".");
        this.thoughtId = thoughtId;
        this.expectedVersion = expectedVersion;
    }

    public long getThoughtId() {
        return thoughtId;
    }

    public int getExpectedVersion() {
        return expectedVersion;
    }
}
