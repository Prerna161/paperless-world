package com.paperlessworld.exception;

/**
 * Thrown when an optimistic concurrency update fails (version_no does not match).
 * Propagated to user as "This thought changed in another tab. Reload to see the latest version."
 */
public class StaleThoughtException extends RuntimeException {
    private final long thoughtId;
    private final long expectedVersion;

    public StaleThoughtException(long thoughtId, long expectedVersion) {
        super("This thought changed in another tab. Reload to see the latest version (Thought #" + thoughtId + ").");
        this.thoughtId = thoughtId;
        this.expectedVersion = expectedVersion;
    }

    public long getThoughtId() {
        return thoughtId;
    }

    public long getExpectedVersion() {
        return expectedVersion;
    }
}
