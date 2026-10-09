-- =====================================================================
-- Paperless World: Reset Demo Script (PRD Section 21)
-- Restores the starting state for user 1 before running the 60-second viva demo
-- =====================================================================

USE paperless_world;

-- Clear previous demo thought events, resurfacing records, and thoughts for demo user
DELETE FROM resurfacing_events WHERE thought_id IN (SELECT thought_id FROM thoughts WHERE user_id = 1);
DELETE FROM thought_events WHERE thought_id IN (SELECT thought_id FROM thoughts WHERE user_id = 1);
DELETE FROM actions WHERE thought_id IN (SELECT thought_id FROM thoughts WHERE user_id = 1);
DELETE FROM thoughts WHERE user_id = 1;
DELETE FROM users WHERE user_id = 1;

-- Reset demo user: student@paperless.org / password123
INSERT INTO users (user_id, email, display_name, password_hash, demo_offset_days)
VALUES (1, 'student@paperless.org', 'Prabhjot', '600000:c2FsdF9zYWx0X3NhbHQxNg==:p5jZq0m8r2+1zZ0y3tV6gL0e7u1A==', 0);

-- 1. An idea untouched for 20 days (OPEN)
INSERT INTO thoughts (thought_id, user_id, client_token, thought_type, title, body, state, closure_reason, importance, version_no, created_at, updated_at, last_touched_at)
VALUES (1, 1, 'token-demo-1', 'IDEA', 'Look into how connection pooling actually works', 'Verify HikariCP vs Tomcat JDBC pool thread safety and eviction semantics.', 'OPEN', NULL, 4, 0, DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 2. A question with an action overdue by 3 days
INSERT INTO thoughts (thought_id, user_id, client_token, thought_type, title, body, state, closure_reason, importance, version_no, created_at, updated_at, last_touched_at)
VALUES (2, 1, 'token-demo-2', 'QUESTION', 'Reply to Aman about the demo architecture', 'Need to explain why we use a controllable clock rather than background sleep.', 'OPEN', NULL, 5, 0, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO actions (action_id, thought_id, description, due_date, done_at, created_at)
VALUES (1, 2, 'Send architecture diagram to Aman', DATE_SUB(CURDATE(), INTERVAL 3 DAY), NULL, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- 3. A note edited 2 days ago (cooling threshold for NOTE is 30 days)
INSERT INTO thoughts (thought_id, user_id, client_token, thought_type, title, body, state, closure_reason, importance, version_no, created_at, updated_at, last_touched_at)
VALUES (3, 1, 'token-demo-3', 'NOTE', 'Tomcat 10.1 classloader notes', 'WebappClassLoader isolation ensures jars in WEB-INF/lib take precedence.', 'OPEN', NULL, 3, 0, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY));

-- 4. An inbox item 9 days old
INSERT INTO thoughts (thought_id, user_id, client_token, thought_type, title, body, state, closure_reason, importance, version_no, created_at, updated_at, last_touched_at)
VALUES (4, 1, 'token-demo-4', 'IDEA', 'Explore WebAssembly for database indexing', 'Could SQLite run inside browser threads without server roundtrips?', 'INBOX', NULL, 3, 0, DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY));

-- 5. A closed thought
INSERT INTO thoughts (thought_id, user_id, client_token, thought_type, title, body, state, closure_reason, importance, version_no, created_at, updated_at, last_touched_at, closed_at)
VALUES (5, 1, 'token-demo-5', 'NOTE', 'Evaluate old rich-text editor prototype', 'Abandoned in favor of plain text and Open Loops.', 'CLOSED', 'LET_GO', 2, 1, DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 25 DAY));

INSERT INTO thought_events (event_id, thought_id, event_type, from_state, to_state, note, occurred_at)
VALUES (1, 5, 'CLOSED', 'OPEN', 'CLOSED', 'Closed with reason: LET_GO', DATE_SUB(NOW(), INTERVAL 25 DAY));
