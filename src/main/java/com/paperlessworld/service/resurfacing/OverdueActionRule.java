package com.paperlessworld.service.resurfacing;

import com.paperlessworld.model.Action;
import com.paperlessworld.model.ResurfacingReason;
import com.paperlessworld.model.Thought;
import com.paperlessworld.model.ThoughtState;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Fires when an action is past due and not completed (PRD Section 10.3).
 * Reason: "Action overdue by N days"
 */
public class OverdueActionRule implements ResurfacingRule {

    @Override
    public String code() {
        return "OVERDUE_ACTION";
    }

    @Override
    public int priority() {
        return 10; // Highest priority
    }

    @Override
    public Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now) {
        if (t.getState() != ThoughtState.OPEN || actions == null || actions.isEmpty()) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());

        for (Action action : actions) {
            if (!action.isDone() && action.getDueDate() != null) {
                if (action.getDueDate().isBefore(today)) {
                    long daysOverdue = ChronoUnit.DAYS.between(action.getDueDate(), today);
                    return Optional.of(new ResurfacingReason(
                        code(),
                        "Action overdue by " + daysOverdue + " days: \"" + action.getDescription() + "\"",
                        (int) daysOverdue
                    ));
                }
            }
        }
        return Optional.empty();
    }
}
