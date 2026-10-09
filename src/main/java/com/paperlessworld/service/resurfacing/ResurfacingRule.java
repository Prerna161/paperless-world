package com.paperlessworld.service.resurfacing;

import com.paperlessworld.model.Action;
import com.paperlessworld.model.ResurfacingReason;
import com.paperlessworld.model.Thought;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Strategy pattern interface matching PRD Section 10.3.
 */
public interface ResurfacingRule {
    String code();
    Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now);
    default int priority() { return 100; }
}
