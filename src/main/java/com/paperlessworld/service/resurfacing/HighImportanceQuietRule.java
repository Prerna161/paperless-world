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
 * Fires when an OPEN thought has importance 4 or 5 and has not been touched for 10 days.
 * Reason: "Important and quiet for N days"
 */
public class HighImportanceQuietRule implements ResurfacingRule {

    @Override
    public String code() {
        return "HIGH_IMPORTANCE_QUIET";
    }

    @Override
    public int priority() {
        return 15;
    }

    @Override
    public Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now) {
        if (t.getState() == ThoughtState.OPEN && t.getImportance() >= 4) {
            long daysQuiet = Duration.between(t.getLastTouchedAt(), now).toDays();
            if (daysQuiet >= 10) {
                return Optional.of(new ResurfacingReason(
                    code(),
                    "Important and quiet for " + daysQuiet + " days",
                    (int) daysQuiet
                ));
            }
        }
        return Optional.empty();
    }
}
