package com.paperlessworld.model;

import com.paperlessworld.exception.InvalidClosureException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Root abstract class for the Thought domain hierarchy (PRD Section 10.1).
 * Subclasses differ in behavior (cooling threshold Duration, rules), not merely in labels.
 */
public abstract class Thought {
    private final long id;
    private final long userId;
    private String clientToken;
    private String title;
    private String body;
    private ThoughtState state;
    private ClosureReason closureReason;   // null unless CLOSED
    private int importance;                // 1 to 5
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastTouchedAt;
    private Instant closedAt;
    private long versionNo;
    private ResurfacingReason resurfacedReason;
    private final List<Action> actions = new ArrayList<>();

    protected Thought(long id, long userId, String clientToken, String title, String body,
                      ThoughtState state, ClosureReason closureReason, int importance,
                      long versionNo, Instant createdAt, Instant updatedAt,
                      Instant lastTouchedAt, Instant closedAt) {
        this.id = id;
        this.userId = userId;
        this.clientToken = clientToken;
        this.title = title;
        this.body = body;
        this.state = state != null ? state : ThoughtState.INBOX;
        this.closureReason = closureReason;
        this.importance = Math.max(1, Math.min(5, importance));
        this.versionNo = versionNo;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
        this.lastTouchedAt = lastTouchedAt != null ? lastTouchedAt : this.createdAt;
        this.closedAt = closedAt;
    }

    public abstract ThoughtType type();
    public abstract Duration coolingThreshold();   // how long before it counts as neglected

    public void close(ClosureReason reason, Instant now) {
        state.requireTransitionTo(ThoughtState.CLOSED);
        if (reason == null) {
            throw new InvalidClosureException("A closure reason is required when closing an open loop.");
        }
        this.state = ThoughtState.CLOSED;
        this.closureReason = reason;
        this.lastTouchedAt = now;
        this.updatedAt = now;
        this.closedAt = now;
    }

    public void reopen(Instant now) {
        state.requireTransitionTo(ThoughtState.OPEN);
        this.state = ThoughtState.OPEN;
        this.closureReason = null;
        this.closedAt = null;
        this.lastTouchedAt = now;
        this.updatedAt = now;
    }

    public void keep(Instant now) {
        state.requireTransitionTo(ThoughtState.OPEN);
        this.state = ThoughtState.OPEN;
        this.lastTouchedAt = now;
        this.updatedAt = now;
    }

    public void touch(Instant now) {
        this.lastTouchedAt = now;
        this.updatedAt = now;
    }

    // Getters and Setters
    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getClientToken() { return clientToken; }
    public void setClientToken(String clientToken) { this.clientToken = clientToken; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public ThoughtState getState() { return state; }
    public void setState(ThoughtState state) { this.state = state; }
    public ClosureReason getClosureReason() { return closureReason; }
    public void setClosureReason(ClosureReason closureReason) { this.closureReason = closureReason; }
    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = Math.max(1, Math.min(5, importance)); }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getLastTouchedAt() { return lastTouchedAt; }
    public void setLastTouchedAt(Instant lastTouchedAt) { this.lastTouchedAt = lastTouchedAt; }
    public Instant getClosedAt() { return closedAt; }
    public void setClosedAt(Instant closedAt) { this.closedAt = closedAt; }
    public long getVersionNo() { return versionNo; }
    public void setVersionNo(long versionNo) { this.versionNo = versionNo; }

    public ResurfacingReason getResurfacedReason() { return resurfacedReason; }
    public void setResurfacedReason(ResurfacingReason resurfacedReason) { this.resurfacedReason = resurfacedReason; }

    public List<Action> getActions() { return Collections.unmodifiableList(actions); }
    public void addAction(Action action) {
        if (action != null) actions.add(action);
    }
    public void setActions(List<Action> actions) {
        this.actions.clear();
        if (actions != null) this.actions.addAll(actions);
    }
}
