package com.paperlessworld.service.resurfacing;

import com.paperlessworld.model.Action;
import com.paperlessworld.model.ResurfacingReason;
import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtState;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Fires when an OPEN thought was never edited since creation, and its age >= cooling threshold.
 * Reason: "Open for N days, never edited"
 */
public class NeverRevisitedRule implements ResurfacingRule {

    @Override
    public String code() {
        return "NEVER_REVISITED";
    }

    @Override
    public int priority() {
        return 20;
    }

    @Override
    public Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now) {
        if (t.getState() == ThoughtState.OPEN && t.getVersionNo() == 0) {
            long daysOld = Duration.between(t.getCreatedAt(), now).toDays();
            long thresholdDays = t.coolingThreshold().toDays();
            if (daysOld >= thresholdDays) {
                return Optional.of(new ResurfacingReason(
                    code(),
                    "Open for " + daysOld + " days, never edited",
                    (int) daysOld
                ));
            }
        }
        return Optional.empty();
    }
}
