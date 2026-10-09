package com.paperlessworld.service;

import com.paperlessworld.dao.ActionDao;
import com.paperlessworld.dao.EventDao;
import com.paperlessworld.dao.ThoughtDao;
import com.paperlessworld.exception.InvalidClosureException;
import com.paperlessworld.exception.NotFoundException;
import com.paperlessworld.exception.StaleThoughtException;
import com.paperlessworld.model.*;
import com.paperlessworld.util.TimeSource;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Thought service managing Open Loop lifecycle, optimistic locking, and transactions (PRD Section 9, 13.4).
 */
public class ThoughtService {
    private final ThoughtDao thoughtDao;
    private final ActionDao actionDao;
    private final EventDao eventDao;
    private final TransactionManager txManager;

    public ThoughtService(ThoughtDao thoughtDao, ActionDao actionDao, EventDao eventDao, TransactionManager txManager) {
        this.thoughtDao = thoughtDao;
        this.actionDao = actionDao;
        this.eventDao = eventDao;
        this.txManager = txManager;
    }

    public Thought capture(long userId, String title, String body, ThoughtType type, int importance,
                           String clientToken, TimeSource timeSource) {
        if (clientToken == null || clientToken.isBlank()) {
            clientToken = UUID.randomUUID().toString();
        }
        final String finalToken = clientToken;

        return txManager.inTransaction(conn -> {
            try {
                // Idempotency check: if token already exists, return existing row
                Optional<Thought> existing = thoughtDao.findByClientToken(conn, userId, finalToken);
                if (existing.isPresent()) {
                    return existing.get();
                }

                Thought t = ThoughtFactory.create(type, userId, title, body, importance);
                t.setClientToken(finalToken);
                t.setCreatedAt(timeSource.now());
                t.setUpdatedAt(timeSource.now());
                t.setLastTouchedAt(timeSource.now());

                long id = thoughtDao.insert(conn, t);
                eventDao.insert(conn, id, "CAPTURED", null, "INBOX", "Captured into inbox", timeSource.now());
                return thoughtDao.findById(conn, id, userId).orElseThrow();
            } catch (SQLException e) {
                throw new RuntimeException("Capture failed", e);
            }
        });
    }

    public void keep(long userId, long thoughtId, TimeSource timeSource) {
        txManager.inTransaction(conn -> {
            try {
                Thought t = thoughtDao.findById(conn, thoughtId, userId)
                    .orElseThrow(() -> new NotFoundException("Thought not found or unauthorized"));
                t.keep(timeSource.now());

                boolean ok = thoughtDao.updateState(conn, thoughtId, userId, ThoughtState.OPEN, null,
                                                   t.getVersionNo(), timeSource.now());
                if (!ok) {
                    throw new StaleThoughtException(thoughtId, t.getVersionNo());
                }

                eventDao.insert(conn, thoughtId, "KEPT", "INBOX", "OPEN", "Moved from inbox to open loop", timeSource.now());
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Keep failed", e);
            }
        });
    }

    /**
     * Executes the exact atomic 3-write transaction from PRD Section 13.4:
     * 1. UPDATE thoughts (state, closure reason, closed_at, version bump)
     * 2. INSERT thought_events (the transition)
     * 3. UPDATE resurfacing_events SET dismissed_at = ? for open rows of that thought
     */
    public void close(long userId, long thoughtId, ClosureReason reason, TimeSource timeSource) {
        if (reason == null) {
            throw new InvalidClosureException("A closure reason is required");
        }

        txManager.inTransaction(conn -> {
            try {
                Thought t = thoughtDao.findById(conn, thoughtId, userId)
                    .orElseThrow(() -> new NotFoundException("Thought not found or unauthorized"));
                t.close(reason, timeSource.now());

                // 1. UPDATE thoughts
                boolean ok = thoughtDao.updateState(conn, thoughtId, userId, ThoughtState.CLOSED, reason,
                                                   t.getVersionNo(), timeSource.now());
                if (!ok) {
                    throw new StaleThoughtException(thoughtId, t.getVersionNo());
                }

                // 2. INSERT thought_events
                eventDao.insert(conn, thoughtId, "CLOSED", t.getState().name(), "CLOSED",
                                "Closed with reason: " + reason, timeSource.now());

                // 3. UPDATE resurfacing_events (dismiss active surfacing)
                eventDao.dismissResurfacingEvents(conn, thoughtId, timeSource.now());

                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Closure transaction failed", e);
            }
        });
    }

    public void reopen(long userId, long thoughtId, TimeSource timeSource) {
        txManager.inTransaction(conn -> {
            try {
                Thought t = thoughtDao.findById(conn, thoughtId, userId)
                    .orElseThrow(() -> new NotFoundException("Thought not found or unauthorized"));
                t.reopen(timeSource.now());

                boolean ok = thoughtDao.updateState(conn, thoughtId, userId, ThoughtState.OPEN, null,
                                                   t.getVersionNo(), timeSource.now());
                if (!ok) {
                    throw new StaleThoughtException(thoughtId, t.getVersionNo());
                }

                eventDao.insert(conn, thoughtId, "REOPENED", "CLOSED", "OPEN", "Reopened open loop", timeSource.now());
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Reopen failed", e);
            }
        });
    }

    public void update(long userId, long thoughtId, String title, String body, int importance,
                       long expectedVersion, TimeSource timeSource) {
        txManager.inTransaction(conn -> {
            try {
                Thought t = thoughtDao.findById(conn, thoughtId, userId)
                    .orElseThrow(() -> new NotFoundException("Thought not found or unauthorized"));
                t.setTitle(title);
                t.setBody(body);
                t.setImportance(importance);
                t.touch(timeSource.now());

                boolean ok = thoughtDao.update(conn, t, expectedVersion, timeSource.now());
                if (!ok) {
                    throw new StaleThoughtException(thoughtId, expectedVersion);
                }

                eventDao.insert(conn, thoughtId, "EDITED", t.getState().name(), t.getState().name(),
                                "Updated title/body", timeSource.now());
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Update failed", e);
            }
        });
    }

    public void addAction(long userId, long thoughtId, String description, LocalDate dueDate, TimeSource timeSource) {
        txManager.inTransaction(conn -> {
            try {
                thoughtDao.findById(conn, thoughtId, userId)
                    .orElseThrow(() -> new NotFoundException("Thought not found or unauthorized"));

                Action a = new Action(thoughtId, description, dueDate);
                a.setCreatedAt(timeSource.now());
                actionDao.insert(conn, a);

                // Touching thought resets cooling period
                thoughtDao.touch(conn, thoughtId, userId, timeSource.now());
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to add action", e);
            }
        });
    }

    public void markActionDone(long userId, long actionId, long thoughtId, TimeSource timeSource) {
        txManager.inTransaction(conn -> {
            try {
                thoughtDao.findById(conn, thoughtId, userId)
                    .orElseThrow(() -> new NotFoundException("Thought not found or unauthorized"));

                actionDao.markDone(conn, actionId, timeSource.now());
                thoughtDao.touch(conn, thoughtId, userId, timeSource.now());
                return null;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to mark action done", e);
            }
        });
    }

    public Thought getThought(long userId, long thoughtId) {
        return txManager.inTransaction(conn -> {
            try {
                Thought t = thoughtDao.findById(conn, thoughtId, userId)
                    .orElseThrow(() -> new NotFoundException("Thought not found or unauthorized"));
                List<Action> actions = actionDao.findByThoughtId(conn, thoughtId);
                t.setActions(actions);
                return t;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to load thought", e);
            }
        });
    }

    public List<Thought> listByState(long userId, ThoughtState state) {
        return txManager.inTransaction(conn -> {
            try {
                List<Thought> list = thoughtDao.findByState(conn, userId, state);
                for (Thought t : list) {
                    t.setActions(actionDao.findByThoughtId(conn, t.getId()));
                }
                return list;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to list thoughts by state", e);
            }
        });
    }

    public List<Thought> search(long userId, String query) {
        return txManager.inTransaction(conn -> {
            try {
                return thoughtDao.search(conn, userId, query);
            } catch (SQLException e) {
                throw new RuntimeException("Search failed", e);
            }
        });
    }

    public int countByState(long userId, ThoughtState state) {
        return txManager.inTransaction(conn -> {
            try {
                return thoughtDao.countByState(conn, userId, state);
            } catch (SQLException e) {
                throw new RuntimeException("Count failed", e);
            }
        });
    }
}
