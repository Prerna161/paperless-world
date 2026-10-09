package com.paperlessworld.service.resurfacing;

import com.paperlessworld.model.Action;
import com.paperlessworld.model.ResurfacingReason;
import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtState;
import com.paperlessworld.model.ThoughtType;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Fires when a QUESTION is OPEN for more than 7 days.
 * Reason: "Question still open after N days"
 */
public class QuestionUnansweredRule implements ResurfacingRule {

    @Override
    public String code() {
        return "QUESTION_UNANSWERED";
    }

    @Override
    public int priority() {
        return 25;
    }

    @Override
    public Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now) {
        if (t.type() == ThoughtType.QUESTION && t.getState() == ThoughtState.OPEN) {
            long daysOpen = Duration.between(t.getCreatedAt(), now).toDays();
            if (daysOpen >= 7) {
                return Optional.of(new ResurfacingReason(
                    code(),
                    "Question still open after " + daysOpen + " days",
                    (int) daysOpen
                ));
            }
        }
        return Optional.empty();
    }
}
