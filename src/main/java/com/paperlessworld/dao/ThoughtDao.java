package com.paperlessworld.dao;

import com.paperlessworld.model.*;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Thoughts (PRD Section 13).
 * Every method receives an explicit Connection managed by TransactionManager.
 * Enforces ownership (WHERE user_id = ?), optimistic locking (version_no), and PreparedStatement.
 */
public class ThoughtDao {

    public long insert(Connection c, Thought t) throws SQLException {
        String sql = "INSERT INTO thoughts (user_id, client_token, thought_type, title, body, state, " +
                     "closure_reason, importance, version_no, created_at, updated_at, last_touched_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, t.getUserId());
            ps.setString(2, t.getClientToken());
            ps.setString(3, t.type().name());
            ps.setString(4, t.getTitle());
            ps.setString(5, t.getBody());
            ps.setString(6, t.getState().name());
            ps.setString(7, t.getClosureReason() != null ? t.getClosureReason().name() : null);
            ps.setInt(8, t.getImportance());
            ps.setLong(9, 0L);
            ps.setTimestamp(10, Timestamp.from(t.getCreatedAt()));
            ps.setTimestamp(11, Timestamp.from(t.getUpdatedAt()));
            ps.setTimestamp(12, Timestamp.from(t.getLastTouchedAt()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return 0L;
    }

    public Optional<Thought> findById(Connection c, long thoughtId, long userId) throws SQLException {
        String sql = "SELECT * FROM thoughts WHERE thought_id = ? AND user_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, thoughtId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ThoughtFactory.fromRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Thought> findByClientToken(Connection c, long userId, String clientToken) throws SQLException {
        String sql = "SELECT * FROM thoughts WHERE user_id = ? AND client_token = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, clientToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(ThoughtFactory.fromRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Thought> findByState(Connection c, long userId, ThoughtState state) throws SQLException {
        String sql = "SELECT * FROM thoughts WHERE user_id = ? AND state = ? ORDER BY last_touched_at DESC";
        List<Thought> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, state.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(ThoughtFactory.fromRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Resurfacing candidate query matching PRD Section 13.5.
     */
    public List<Thought> findCandidatesForResurfacing(Connection c, long userId) {
        String sql = "SELECT t.*, MAX(a.due_date) AS latest_due " +
                     "FROM thoughts t " +
                     "LEFT JOIN actions a ON a.thought_id = t.thought_id AND a.done_at IS NULL " +
                     "WHERE t.user_id = ? AND t.state IN ('INBOX','OPEN') " +
                     "GROUP BY t.thought_id " +
                     "ORDER BY t.last_touched_at ASC " +
                     "LIMIT 200";
        List<Thought> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(ThoughtFactory.fromRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query resurfacing candidates", e);
        }
        return list;
    }

    /**
     * Optimistic update matching PRD Section 13.3.
     * Returns true if 1 row updated, false if stale version_no detected.
     */
    public boolean update(Connection c, Thought t, long expectedVersion, Instant now) throws SQLException {
        String sql = "UPDATE thoughts SET title = ?, body = ?, importance = ?, " +
                     "version_no = version_no + 1, updated_at = ?, last_touched_at = ? " +
                     "WHERE thought_id = ? AND user_id = ? AND version_no = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getBody());
            ps.setInt(3, t.getImportance());
            ps.setTimestamp(4, Timestamp.from(now));
            ps.setTimestamp(5, Timestamp.from(now));
            ps.setLong(6, t.getId());
            ps.setLong(7, t.getUserId());
            ps.setLong(8, expectedVersion);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean updateState(Connection c, long thoughtId, long userId, ThoughtState targetState, 
                               ClosureReason reason, long expectedVersion, Instant now) throws SQLException {
        String sql = "UPDATE thoughts SET state = ?, closure_reason = ?, closed_at = ?, " +
                     "version_no = version_no + 1, updated_at = ?, last_touched_at = ? " +
                     "WHERE thought_id = ? AND user_id = ? AND version_no = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, targetState.name());
            ps.setString(2, targetState == ThoughtState.CLOSED && reason != null ? reason.name() : null);
            ps.setTimestamp(3, targetState == ThoughtState.CLOSED ? Timestamp.from(now) : null);
            ps.setTimestamp(4, Timestamp.from(now));
            ps.setTimestamp(5, Timestamp.from(now));
            ps.setLong(6, thoughtId);
            ps.setLong(7, userId);
            ps.setLong(8, expectedVersion);
            return ps.executeUpdate() == 1;
        }
    }

    public void touch(Connection c, long thoughtId, long userId, Instant now) throws SQLException {
        String sql = "UPDATE thoughts SET last_touched_at = ?, updated_at = ? WHERE thought_id = ? AND user_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(now));
            ps.setTimestamp(2, Timestamp.from(now));
            ps.setLong(3, thoughtId);
            ps.setLong(4, userId);
            ps.executeUpdate();
        }
    }

    public int countByState(Connection c, long userId, ThoughtState state) throws SQLException {
        String sql = "SELECT COUNT(*) FROM thoughts WHERE user_id = ? AND state = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, state.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Thought> search(Connection c, long userId, String query) throws SQLException {
        String sql = "SELECT * FROM thoughts WHERE user_id = ? AND (title LIKE ? OR body LIKE ?) ORDER BY last_touched_at DESC";
        List<Thought> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            String wildcard = "%" + query + "%";
            ps.setString(2, wildcard);
            ps.setString(3, wildcard);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(ThoughtFactory.fromRow(rs));
                }
            }
        }
        return list;
    }
}
