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
 * Fires when an OPEN thought's last touched time is older than the type's cooling threshold.
 * Reason: "Not touched for N days"
 */
public class CoolingRule implements ResurfacingRule {

    @Override
    public String code() {
        return "COOLING";
    }

    @Override
    public int priority() {
        return 30;
    }

    @Override
    public Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now) {
        if (t.getState() == ThoughtState.OPEN) {
            long daysUntouched = Duration.between(t.getLastTouchedAt(), now).toDays();
            long thresholdDays = t.coolingThreshold().toDays();
            if (daysUntouched >= thresholdDays) {
                return Optional.of(new ResurfacingReason(
                    code(),
                    "Not touched for " + daysUntouched + " days",
                    (int) daysUntouched
                ));
            }
        }
        return Optional.empty();
    }
}
