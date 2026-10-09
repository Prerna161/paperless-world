package com.paperlessworld.dao;

import com.paperlessworld.model.ThoughtEvent;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class EventDao {

    public void insert(Connection c, long thoughtId, String eventType, String fromState, 
                       String toState, String note, Instant now) throws SQLException {
        String sql = "INSERT INTO thought_events (thought_id, event_type, from_state, to_state, note, occurred_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, thoughtId);
            ps.setString(2, eventType);
            ps.setString(3, fromState);
            ps.setString(4, toState);
            ps.setString(5, note);
            ps.setTimestamp(6, Timestamp.from(now));
            ps.executeUpdate();
        }
    }

    public void dismissResurfacingEvents(Connection c, long thoughtId, Instant now) throws SQLException {
        String sql = "UPDATE resurfacing_events SET dismissed_at = ? WHERE thought_id = ? AND dismissed_at IS NULL";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.from(now));
            ps.setLong(2, thoughtId);
            ps.executeUpdate();
        }
    }

    public List<ThoughtEvent> findByThoughtId(Connection c, long thoughtId) throws SQLException {
        String sql = "SELECT * FROM thought_events WHERE thought_id = ? ORDER BY event_id DESC";
        List<ThoughtEvent> list = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, thoughtId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThoughtEvent e = new ThoughtEvent();
                    e.setId(rs.getLong("event_id"));
                    e.setThoughtId(rs.getLong("thought_id"));
                    e.setEventType(rs.getString("event_type"));
                    e.setDetails(rs.getString("note"));
                    Timestamp oc = rs.getTimestamp("occurred_at");
                    if (oc != null) e.setVirtualTimestamp(oc.toInstant());
                    list.add(e);
                }
            }
        }
        return list;
    }
}
