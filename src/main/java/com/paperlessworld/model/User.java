package com.paperlessworld.model;

import java.time.Instant;

public class User {
    private long userId;
    private String email;
    private String displayName;
    private String passwordHash;
    private int demoOffsetDays;
    private int failedLogins;
    private Instant lockedUntil;
    private Instant createdAt;

    public User() {
        this.demoOffsetDays = 0;
        this.failedLogins = 0;
        this.createdAt = Instant.now();
    }

    public User(String email, String displayName, String passwordHash) {
        this();
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public int getDemoOffsetDays() { return demoOffsetDays; }
    public void setDemoOffsetDays(int demoOffsetDays) { this.demoOffsetDays = demoOffsetDays; }

    public int getFailedLogins() { return failedLogins; }
    public void setFailedLogins(int failedLogins) { this.failedLogins = failedLogins; }

    public Instant getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(Instant lockedUntil) { this.lockedUntil = lockedUntil; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
