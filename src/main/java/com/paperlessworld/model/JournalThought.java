package com.paperlessworld.model;

import java.time.Duration;
import java.time.Instant;

public class JournalThought extends Thought {
    public JournalThought(long id, long userId, String clientToken, String title, String body,
                          ThoughtState state, ClosureReason closureReason, int importance,
                          long versionNo, Instant createdAt, Instant updatedAt,
                          Instant lastTouchedAt, Instant closedAt) {
        super(id, userId, clientToken, title, body, state, closureReason, importance,
              versionNo, createdAt, updatedAt, lastTouchedAt, closedAt);
    }

    public JournalThought(long userId, String clientToken, String title, String body, int importance) {
        this(0, userId, clientToken, title, body, ThoughtState.INBOX, null, importance, 0,
             Instant.now(), Instant.now(), Instant.now(), null);
    }

    @Override
    public ThoughtType type() {
        return ThoughtType.JOURNAL;
    }

    @Override
    public Duration coolingThreshold() {
        return Duration.ofDays(30);
    }
}
