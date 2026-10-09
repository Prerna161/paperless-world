package com.paperlessworld.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public final class ThoughtFactory {

    private ThoughtFactory() {}

    public static Thought create(ThoughtType type, long userId, String title, String body, int importance) {
        String token = UUID.randomUUID().toString();
        if (type == null) type = ThoughtType.NOTE;
        return switch (type) {
            case IDEA -> new IdeaThought(userId, token, title, body, importance);
            case QUESTION -> new QuestionThought(userId, token, title, body, importance);
            case JOURNAL -> new JournalThought(userId, token, title, body, importance);
            case REFERENCE -> new ReferenceThought(userId, token, title, body, importance);
            case NOTE -> new NoteThought(userId, token, title, body, importance);
        };
    }

    public static Thought fromRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("thought_id");
        long userId = rs.getLong("user_id");
        String token = rs.getString("client_token");
        String typeStr = rs.getString("thought_type");
        ThoughtType type = ThoughtType.valueOf(typeStr);
        String title = rs.getString("title");
        String body = rs.getString("body");
        ThoughtState state = ThoughtState.valueOf(rs.getString("state"));
        String crStr = rs.getString("closure_reason");
        ClosureReason closureReason = crStr != null ? ClosureReason.valueOf(crStr) : null;
        int importance = rs.getInt("importance");
        long versionNo = rs.getLong("version_no");

        Instant createdAt = toInstant(rs.getTimestamp("created_at"));
        Instant updatedAt = toInstant(rs.getTimestamp("updated_at"));
        Instant lastTouchedAt = toInstant(rs.getTimestamp("last_touched_at"));
        Instant closedAt = toInstant(rs.getTimestamp("closed_at"));

        return switch (type) {
            case IDEA -> new IdeaThought(id, userId, token, title, body, state, closureReason, importance, versionNo, createdAt, updatedAt, lastTouchedAt, closedAt);
            case QUESTION -> new QuestionThought(id, userId, token, title, body, state, closureReason, importance, versionNo, createdAt, updatedAt, lastTouchedAt, closedAt);
            case JOURNAL -> new JournalThought(id, userId, token, title, body, state, closureReason, importance, versionNo, createdAt, updatedAt, lastTouchedAt, closedAt);
            case REFERENCE -> new ReferenceThought(id, userId, token, title, body, state, closureReason, importance, versionNo, createdAt, updatedAt, lastTouchedAt, closedAt);
            case NOTE -> new NoteThought(id, userId, token, title, body, state, closureReason, importance, versionNo, createdAt, updatedAt, lastTouchedAt, closedAt);
        };
    }

    private static Instant toInstant(Timestamp ts) {
        return ts != null ? ts.toInstant() : null;
    }
}
