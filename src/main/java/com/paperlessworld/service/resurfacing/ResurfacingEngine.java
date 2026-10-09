package com.paperlessworld.service.resurfacing;

import com.paperlessworld.dao.ActionDao;
import com.paperlessworld.dao.ThoughtDao;
import com.paperlessworld.model.Action;
import com.paperlessworld.model.ResurfacingReason;
import com.paperlessworld.model.Thought;
import com.paperlessworld.service.TransactionManager;
import com.paperlessworld.util.TimeSource;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * ResurfacingEngine coordinating Strategy rules as per PRD Section 10.3 and 11.1.
 * Ranks thoughts by importance and days neglected.
 * Provides idempotent writes into resurfacing_events table.
 */
public class ResurfacingEngine {
    private final List<ResurfacingRule> rules = new ArrayList<>();
    private final ThoughtDao thoughtDao;
    private final ActionDao actionDao;
    private final TransactionManager txManager;

    public ResurfacingEngine(ThoughtDao thoughtDao, ActionDao actionDao, TransactionManager txManager) {
        this.thoughtDao = thoughtDao;
        this.actionDao = actionDao;
        this.txManager = txManager;

        // Register rules in priority order
        registerRule(new OverdueActionRule());
        registerRule(new HighImportanceQuietRule());
        registerRule(new NeverRevisitedRule());
        registerRule(new QuestionUnansweredRule());
        registerRule(new CoolingRule());
        registerRule(new StaleInboxRule());
    }

    public synchronized void registerRule(ResurfacingRule rule) {
        if (rule != null) {
            rules.add(rule);
            rules.sort(Comparator.comparingInt(ResurfacingRule::priority));
        }
    }

    public Optional<ResurfacingReason> evaluate(Thought t, List<Action> actions, Instant now) {
        for (ResurfacingRule rule : rules) {
            Optional<ResurfacingReason> result = rule.evaluate(t, actions, now);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    /**
     * Finds and ranks resurfaced thoughts for a user, using their virtual TimeSource.
     */
    public List<Thought> findResurfacedThoughts(long userId, TimeSource timeSource) {
        Instant now = timeSource.now();
        List<Thought> candidates = txManager.inTransaction(conn -> thoughtDao.findCandidatesForResurfacing(conn, userId));
        List<Thought> resurfaced = new ArrayList<>();

        for (Thought t : candidates) {
            List<Action> actions = txManager.inTransaction(conn -> actionDao.findByThoughtId(conn, t.getId()));
            t.setActions(actions);

            Optional<ResurfacingReason> reasonOpt = evaluate(t, actions, now);
            if (reasonOpt.isPresent()) {
                t.setResurfacedReason(reasonOpt.get());
                resurfaced.add(t);
            }
        }

        // Rank by importance descending, then days neglected descending
        resurfaced.sort((a, b) -> {
            int impCmp = Integer.compare(b.getImportance(), a.getImportance());
            if (impCmp != 0) return impCmp;
            int aDays = a.getResurfacedReason() != null ? a.getResurfacedReason().daysNeglected() : 0;
            int bDays = b.getResurfacedReason() != null ? b.getResurfacedReason().daysNeglected() : 0;
            return Integer.compare(bDays, aDays);
        });

        return resurfaced;
    }

    /**
     * Executes resurfacing scan and persists idempotently into resurfacing_events table.
     * Exposes runNow(userId) for live demo button.
     */
    public int runNow(long userId, TimeSource timeSource) {
        List<Thought> resurfaced = findResurfacedThoughts(userId, timeSource);
        LocalDate today = timeSource.today();

        return txManager.inTransaction(conn -> {
            String sql = "INSERT IGNORE INTO resurfacing_events (thought_id, rule_code, reason_text, days_neglected, surfaced_on) " +
                         "VALUES (?, ?, ?, ?, ?)";
            int inserted = 0;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Thought t : resurfaced) {
                    ResurfacingReason rr = t.getResurfacedReason();
                    if (rr != null) {
                        ps.setLong(1, t.getId());
                        ps.setString(2, rr.ruleCode());
                        ps.setString(3, rr.text());
                        ps.setInt(4, rr.daysNeglected());
                        ps.setDate(5, Date.valueOf(today));
                        inserted += ps.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to persist resurfacing events", e);
            }
            return inserted;
        });
    }

    public List<ResurfacingRule> getRules() {
        return List.copyOf(rules);
    }
}
