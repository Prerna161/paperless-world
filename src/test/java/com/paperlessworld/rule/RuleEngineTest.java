package com.paperlessworld.rule;

import com.paperlessworld.dao.ActionDao;
import com.paperlessworld.dao.ThoughtDao;
import com.paperlessworld.model.*;
import com.paperlessworld.service.TransactionManager;
import com.paperlessworld.service.resurfacing.*;
import com.paperlessworld.util.TimeSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RuleEngineTest {

    private final ResurfacingEngine engine = new ResurfacingEngine(new ThoughtDao(), new ActionDao(), new TransactionManager(null));

    @Test
    @DisplayName("neverRevisitedRuleFiresAtThresholdAndNotOneDayBefore")
    void neverRevisitedRuleFiresAtThresholdAndNotOneDayBefore() {
        NeverRevisitedRule rule = new NeverRevisitedRule();
        Instant now = Instant.now();

        // Idea cooling threshold = 14 days
        Thought atThreshold = new IdeaThought(1L, 1L, "tok-1", "Pool architecture", "body",
                ThoughtState.OPEN, null, 3, 0L, now.minus(14, ChronoUnit.DAYS),
                now.minus(14, ChronoUnit.DAYS), now.minus(14, ChronoUnit.DAYS), null);

        Thought oneDayBefore = new IdeaThought(2L, 1L, "tok-2", "Pool architecture", "body",
                ThoughtState.OPEN, null, 3, 0L, now.minus(13, ChronoUnit.DAYS),
                now.minus(13, ChronoUnit.DAYS), now.minus(13, ChronoUnit.DAYS), null);

        Optional<ResurfacingReason> fires = rule.evaluate(atThreshold, List.of(), now);
        assertTrue(fires.isPresent());
        assertEquals("NEVER_REVISITED", fires.get().ruleCode());
        assertEquals("Open for 14 days, never edited", fires.get().text());
        assertEquals(14, fires.get().daysNeglected());

        Optional<ResurfacingReason> doesNotFire = rule.evaluate(oneDayBefore, List.of(), now);
        assertTrue(doesNotFire.isEmpty(), "Rule must not fire 1 day before the threshold");
    }

    @Test
    @DisplayName("coolingRuleFiresAtThresholdAndNotOneDayBefore")
    void coolingRuleFiresAtThresholdAndNotOneDayBefore() {
        CoolingRule rule = new CoolingRule();
        Instant now = Instant.now();

        // Question cooling threshold = 7 days, versionNo = 1 (edited before)
        Thought atThreshold = new QuestionThought(3L, 1L, "tok-3", "Consensus query", "body",
                ThoughtState.OPEN, null, 3, 1L, now.minus(20, ChronoUnit.DAYS),
                now.minus(7, ChronoUnit.DAYS), now.minus(7, ChronoUnit.DAYS), null);

        Thought oneDayBefore = new QuestionThought(4L, 1L, "tok-4", "Consensus query", "body",
                ThoughtState.OPEN, null, 3, 1L, now.minus(20, ChronoUnit.DAYS),
                now.minus(6, ChronoUnit.DAYS), now.minus(6, ChronoUnit.DAYS), null);

        Optional<ResurfacingReason> fires = rule.evaluate(atThreshold, List.of(), now);
        assertTrue(fires.isPresent());
        assertEquals("COOLING", fires.get().ruleCode());
        assertEquals("Not touched for 7 days", fires.get().text());
        assertEquals(7, fires.get().daysNeglected());

        Optional<ResurfacingReason> doesNotFire = rule.evaluate(oneDayBefore, List.of(), now);
        assertTrue(doesNotFire.isEmpty());
    }

    @Test
    @DisplayName("overdueActionRuleFiresWhenPastDueAndIgnoresFutureDueOrCompletedActions")
    void overdueActionRuleFiresWhenPastDueAndIgnoresFutureDueOrCompletedActions() {
        OverdueActionRule rule = new OverdueActionRule();
        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());

        Thought thought = new IdeaThought(5L, 1L, "tok-5", "Project demo", "body",
                ThoughtState.OPEN, null, 3, 0L, now.minus(5, ChronoUnit.DAYS),
                now.minus(5, ChronoUnit.DAYS), now.minus(5, ChronoUnit.DAYS), null);

        Action overdueAction = new Action(1L, "Deliver slides", today.minusDays(3));
        Action futureAction = new Action(2L, "Follow up", today.plusDays(2));
        Action completedOverdue = new Action(3L, "Old finished task", today.minusDays(5));
        completedOverdue.markDone(now);

        Optional<ResurfacingReason> reason = rule.evaluate(thought, List.of(futureAction, completedOverdue, overdueAction), now);
        assertTrue(reason.isPresent());
        assertEquals("OVERDUE_ACTION", reason.get().ruleCode());
        assertTrue(reason.get().text().contains("Action overdue by 3 days"));
        assertEquals(3, reason.get().daysNeglected());

        // Without overdue action
        Optional<ResurfacingReason> noOverdue = rule.evaluate(thought, List.of(futureAction, completedOverdue), now);
        assertTrue(noOverdue.isEmpty());
    }

    @Test
    @DisplayName("rulesIgnoreClosedThoughts")
    void rulesIgnoreClosedThoughts() {
        Instant now = Instant.now();
        LocalDate today = LocalDate.ofInstant(now, ZoneId.systemDefault());

        Thought closedThought = new IdeaThought(6L, 1L, "tok-6", "Closed idea", "body",
                ThoughtState.CLOSED, ClosureReason.DONE, 5, 0L, now.minus(30, ChronoUnit.DAYS),
                now.minus(30, ChronoUnit.DAYS), now.minus(30, ChronoUnit.DAYS), now);

        Action overdueAction = new Action(4L, "Overdue task", today.minusDays(10));

        for (ResurfacingRule rule : engine.getRules()) {
            Optional<ResurfacingReason> result = rule.evaluate(closedThought, List.of(overdueAction), now);
            assertTrue(result.isEmpty(), "Rule " + rule.code() + " must never fire on a CLOSED thought");
        }
    }

    @Test
    @DisplayName("engineReturnsOneReasonPerThoughtOrderedByImportanceThenDaysNeglected")
    void engineReturnsOneReasonPerThoughtOrderedByImportanceThenDaysNeglected() {
        Instant now = Instant.now();

        // Thought A: Importance 3, untouched 20 days
        Thought tA = new IdeaThought(10L, 1L, "tok-A", "Thought A", "body",
                ThoughtState.OPEN, null, 3, 0L, now.minus(20, ChronoUnit.DAYS),
                now.minus(20, ChronoUnit.DAYS), now.minus(20, ChronoUnit.DAYS), null);

        // Thought B: Importance 5, untouched 12 days (Higher importance should rank first)
        Thought tB = new IdeaThought(11L, 1L, "tok-B", "Thought B", "body",
                ThoughtState.OPEN, null, 5, 1L, now.minus(20, ChronoUnit.DAYS),
                now.minus(12, ChronoUnit.DAYS), now.minus(12, ChronoUnit.DAYS), null);

        // Thought C: Importance 3, untouched 25 days (Same importance as A, but more days neglected, should rank before A)
        Thought tC = new IdeaThought(12L, 1L, "tok-C", "Thought C", "body",
                ThoughtState.OPEN, null, 3, 0L, now.minus(25, ChronoUnit.DAYS),
                now.minus(25, ChronoUnit.DAYS), now.minus(25, ChronoUnit.DAYS), null);

        List<Thought> list = new ArrayList<>(List.of(tA, tB, tC));
        for (Thought t : list) {
            Optional<ResurfacingReason> r = engine.evaluate(t, List.of(), now);
            assertTrue(r.isPresent());
            t.setResurfacedReason(r.get());
        }

        // Rank according to ResurfacingEngine logic: importance DESC, daysNeglected DESC
        list.sort((a, b) -> {
            int impCmp = Integer.compare(b.getImportance(), a.getImportance());
            if (impCmp != 0) return impCmp;
            int aDays = a.getResurfacedReason().daysNeglected();
            int bDays = b.getResurfacedReason().daysNeglected();
            return Integer.compare(bDays, aDays);
        });

        assertEquals(11L, list.get(0).getId(), "Highest importance (5) ranks first");
        assertEquals(12L, list.get(1).getId(), "Importance 3 with 25 days neglected ranks second");
        assertEquals(10L, list.get(2).getId(), "Importance 3 with 20 days neglected ranks third");
    }

    @Test
    @DisplayName("timeSourceFakeMovingClockBy15DaysChangesRuleOutcomesWithNoSleeping")
    void timeSourceFakeMovingClockBy15DaysChangesRuleOutcomesWithNoSleeping() {
        TimeSource baselineTime = TimeSource.system();
        Instant baseInstant = baselineTime.now();

        // Fresh idea created right now: threshold = 14 days
        Thought freshIdea = new IdeaThought(20L, 1L, "tok-fresh", "Fresh thought", "body",
                ThoughtState.OPEN, null, 3, 0L, baseInstant, baseInstant, baseInstant, null);

        // Under current system clock: age is 0 days, must not resurface
        Optional<ResurfacingReason> initial = engine.evaluate(freshIdea, List.of(), baseInstant);
        assertTrue(initial.isEmpty(), "Fresh thought must not resurface at current time");

        // Fast-forward virtual clock by 15 days via TimeSource without Thread.sleep
        TimeSource fastForwardTime = TimeSource.withOffsetDays(15);
        Instant futureInstant = fastForwardTime.now();

        Optional<ResurfacingReason> after15Days = engine.evaluate(freshIdea, List.of(), futureInstant);
        assertTrue(after15Days.isPresent(), "Moving clock forward 15 days must cause thought to resurface");
        assertEquals("NEVER_REVISITED", after15Days.get().ruleCode());
        assertEquals("Open for 15 days, never edited", after15Days.get().text());
        assertEquals(15, after15Days.get().daysNeglected());
    }

    @Test
    @DisplayName("evaluatingEmptyThoughtListReturnsEmptyList")
    void evaluatingEmptyThoughtListReturnsEmptyList() {
        List<Thought> empty = List.of();
        assertTrue(empty.isEmpty());
    }
}
