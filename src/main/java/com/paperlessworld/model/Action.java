package com.paperlessworld.model;

import java.time.Instant;
import java.time.LocalDate;

public class Action {
    private long actionId;
    private long thoughtId;
    private String description;
    private LocalDate dueDate;
    private Instant doneAt;
    private Instant createdAt;

    public Action() {
        this.createdAt = Instant.now();
    }

    public Action(long thoughtId, String description, LocalDate dueDate) {
        this();
        this.thoughtId = thoughtId;
        this.description = description;
        this.dueDate = dueDate;
    }

    public boolean isDone() {
        return doneAt != null;
    }

    public void markDone(Instant now) {
        this.doneAt = now;
    }

    public void markUndone() {
        this.doneAt = null;
    }

    public long getActionId() { return actionId; }
    public void setActionId(long actionId) { this.actionId = actionId; }

    public long getThoughtId() { return thoughtId; }
    public void setThoughtId(long thoughtId) { this.thoughtId = thoughtId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Instant getDoneAt() { return doneAt; }
    public void setDoneAt(Instant doneAt) { this.doneAt = doneAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
