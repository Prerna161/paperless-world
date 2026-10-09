package com.paperlessworld.model;

import java.time.Instant;

/**
 * Audit log entry for lifecycle state transitions, resumptions, and closures.
 */
public class ThoughtEvent {
    private long id;
    private long thoughtId;
    private long userId;
    private String eventType;
    private String details;
    private Instant virtualTimestamp;
    private Instant createdAt;

    public ThoughtEvent() {
        this.createdAt = Instant.now();
    }

    public ThoughtEvent(long thoughtId, long userId, String eventType, String details, Instant virtualTimestamp) {
        this();
        this.thoughtId = thoughtId;
        this.userId = userId;
        this.eventType = eventType;
        this.details = details;
        this.virtualTimestamp = virtualTimestamp;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getThoughtId() { return thoughtId; }
    public void setThoughtId(long thoughtId) { this.thoughtId = thoughtId; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getVirtualTimestamp() { return virtualTimestamp; }
    public void setVirtualTimestamp(Instant virtualTimestamp) { this.virtualTimestamp = virtualTimestamp; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
