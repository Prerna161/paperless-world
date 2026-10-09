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
 * Fires when a thought has lingered in INBOX for more than 7 days.
 * Reason: "In the inbox for N days"
 */
public class StaleInboxRule implements ResurfacingRule {

    @Override
    public String code() {
        return "STALE_INBOX";
    }

    @Override
    public int priority() {
        return 40;
    }

    @Override
    public Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now) {
        if (t.getState() == ThoughtState.INBOX) {
            long daysInInbox = Duration.between(t.getCreatedAt(), now).toDays();
            if (daysInInbox >= 7) {
                return Optional.of(new ResurfacingReason(
                    code(),
                    "In the inbox for " + daysInInbox + " days",
                    (int) daysInInbox
                ));
            }
        }
        return Optional.empty();
    }
}
