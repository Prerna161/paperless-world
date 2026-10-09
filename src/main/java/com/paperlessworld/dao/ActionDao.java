package com.paperlessworld.dao;

import com.paperlessworld.model.Action;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ActionDao {

    public long insert(Connection c, Action a) throws SQLException {
        String sql = "INSERT INTO actions (thought_id, description, due_date, done_at, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, a.getThoughtId());
            ps.setString(2, a.getDescription());
            ps.setDate(3, a.getDueDate() != null ? Date.valueOf(a.getDueDate()) : null);
            ps.setTimestamp(4, a.getDoneAt() != null ? Timestamp.from(a.getDoneAt()) : null);
            ps.setTimestamp(5, Timestamp.from(a.getCreatedAt()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    a.setActionId(id);
                    return id;
                }
            }
        }
        return 0L;
    }

    public List<Action> findByThoughtId(Connection c, long thoughtId) {
        String sql = "SELECT * FROM actions WHERE thought_id = ? ORDER BY due_date ASC";
        List<Action> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, thoughtId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query actions for thought #" + thoughtId, e);
        }
        return list;
    }

    public void markDone(Connection c, long actionId, Instant now) throws SQLException {
        String sql = "UPDATE actions SET done_at = ? WHERE action_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(now));
            ps.setLong(2, actionId);
            ps.executeUpdate();
        }
    }

    public void markUndone(Connection c, long actionId) throws SQLException {
        String sql = "UPDATE actions SET done_at = NULL WHERE action_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, actionId);
            ps.executeUpdate();
        }
    }

    public void delete(Connection c, long actionId) throws SQLException {
        String sql = "DELETE FROM actions WHERE action_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, actionId);
            ps.executeUpdate();
        }
    }

    private Action mapRow(ResultSet rs) throws SQLException {
        Action a = new Action();
        a.setActionId(rs.getLong("action_id"));
        a.setThoughtId(rs.getLong("thought_id"));
        a.setDescription(rs.getString("description"));
        Date d = rs.getDate("due_date");
        if (d != null) a.setDueDate(d.toLocalDate());
        Timestamp dt = rs.getTimestamp("done_at");
        if (dt != null) a.setDoneAt(dt.toInstant());
        Timestamp ct = rs.getTimestamp("created_at");
        if (ct != null) a.setCreatedAt(ct.toInstant());
        return a;
    }
}
