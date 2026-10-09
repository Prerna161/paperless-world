package com.paperlessworld.model;

import com.paperlessworld.exception.IllegalStateTransitionException;

import java.util.EnumSet;
import java.util.Map;

/**
 * Three mutually exclusive states of an Open Loop.
 * Defines the state transition machine table as per PRD Section 5 and 10.2.
 */
public enum ThoughtState {
    INBOX,
    OPEN,
    CLOSED;

    private static final Map<ThoughtState, EnumSet<ThoughtState>> ALLOWED_TRANSITIONS = Map.of(
        INBOX, EnumSet.of(OPEN, CLOSED),
        OPEN, EnumSet.of(CLOSED),
        CLOSED, EnumSet.of(OPEN)
    );

    public boolean canTransitionTo(ThoughtState target) {
        if (target == null) return false;
        if (this == target) return true;
        EnumSet<ThoughtState> allowed = ALLOWED_TRANSITIONS.get(this);
        return allowed != null && allowed.contains(target);
    }

    public void requireTransitionTo(ThoughtState target) {
        if (!canTransitionTo(target)) {
            throw new IllegalStateTransitionException(
                "Illegal lifecycle transition from " + this + " to " + target + ". Open loops cannot return to INBOX."
            );
        }
    }
}
