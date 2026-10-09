-- =====================================================================
-- Paperless World: Complete 7-Table MySQL 8 Relational Schema
-- Fully aligned with Section 12 of Redesigned PRD
-- =====================================================================

CREATE DATABASE IF NOT EXISTS paperless_world 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE paperless_world;

-- Drop in reverse dependency order for clean migrations
DROP TABLE IF EXISTS resurfacing_events;
DROP TABLE IF EXISTS thought_events;
DROP TABLE IF EXISTS thought_tags;
DROP TABLE IF EXISTS tags;
DROP TABLE IF EXISTS actions;
DROP TABLE IF EXISTS thoughts;
DROP TABLE IF EXISTS users;

-- 1. Users Table
CREATE TABLE users (
  user_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  email            VARCHAR(190) NOT NULL UNIQUE,
  display_name     VARCHAR(80)  NOT NULL,
  password_hash    VARCHAR(255) NOT NULL,
  demo_offset_days INT NOT NULL DEFAULT 0,
  failed_logins    INT NOT NULL DEFAULT 0,
  locked_until     DATETIME(3) NULL,
  created_at       DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Single Unified Thoughts Table
CREATE TABLE thoughts (
  thought_id      BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id         BIGINT NOT NULL,
  client_token    CHAR(36) NOT NULL,
  thought_type    VARCHAR(16) NOT NULL DEFAULT 'IDEA',
  title           VARCHAR(200) NOT NULL,
  body            TEXT NULL,
  state           VARCHAR(10) NOT NULL DEFAULT 'INBOX',
  closure_reason  VARCHAR(10) NULL,
  importance      TINYINT NOT NULL DEFAULT 3,
  version_no      BIGINT NOT NULL DEFAULT 0,
  created_at      DATETIME(3) NOT NULL,
  updated_at      DATETIME(3) NOT NULL,
  last_touched_at DATETIME(3) NOT NULL,
  closed_at       DATETIME(3) NULL,
  CONSTRAINT fk_thoughts_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT uq_thoughts_token UNIQUE (user_id, client_token),
  CONSTRAINT ck_thoughts_type CHECK (thought_type IN ('IDEA','NOTE','QUESTION','JOURNAL','REFERENCE')),
  CONSTRAINT ck_thoughts_state CHECK (state IN ('INBOX','OPEN','CLOSED')),
  CONSTRAINT ck_thoughts_importance CHECK (importance BETWEEN 1 AND 5),
  CONSTRAINT ck_thoughts_closure CHECK (
    (state = 'CLOSED' AND closure_reason IN ('DONE','LET_GO','RECORDED'))
    OR (state <> 'CLOSED' AND closure_reason IS NULL))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_thoughts_user_state ON thoughts (user_id, state, last_touched_at);
CREATE FULLTEXT INDEX ft_thoughts_text ON thoughts (title, body);

-- 3. Actions Table
CREATE TABLE actions (
  action_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
  thought_id  BIGINT NOT NULL,
  description VARCHAR(255) NOT NULL,
  due_date    DATE NULL,
  done_at     DATETIME(3) NULL,
  created_at  DATETIME(3) NOT NULL,
  CONSTRAINT fk_actions_thought FOREIGN KEY (thought_id) REFERENCES thoughts(thought_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX ix_actions_due ON actions (due_date, done_at);

-- 4. Tags Table
CREATE TABLE tags (
  tag_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  name    VARCHAR(40) NOT NULL,
  CONSTRAINT fk_tags_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT uq_tags UNIQUE (user_id, name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Thought Tags Join Table
CREATE TABLE thought_tags (
  thought_id BIGINT NOT NULL,
  tag_id     BIGINT NOT NULL,
  PRIMARY KEY (thought_id, tag_id),
  CONSTRAINT fk_tt_thought FOREIGN KEY (thought_id) REFERENCES thoughts(thought_id) ON DELETE CASCADE,
  CONSTRAINT fk_tt_tag     FOREIGN KEY (tag_id)     REFERENCES tags(tag_id)         ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Thought Events Audit Table
CREATE TABLE thought_events (
  event_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
  thought_id  BIGINT NOT NULL,
  event_type  VARCHAR(20) NOT NULL,
  from_state  VARCHAR(10) NULL,
  to_state    VARCHAR(10) NULL,
  note        VARCHAR(255) NULL,
  occurred_at DATETIME(3) NOT NULL,
  CONSTRAINT fk_te_thought FOREIGN KEY (thought_id) REFERENCES thoughts(thought_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Resurfacing Events Table
CREATE TABLE resurfacing_events (
  resurfacing_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  thought_id     BIGINT NOT NULL,
  rule_code      VARCHAR(30) NOT NULL,
  reason_text    VARCHAR(255) NOT NULL,
  days_neglected INT NOT NULL,
  surfaced_on    DATE NOT NULL,
  dismissed_at   DATETIME(3) NULL,
  CONSTRAINT fk_re_thought FOREIGN KEY (thought_id) REFERENCES thoughts(thought_id) ON DELETE CASCADE,
  CONSTRAINT uq_re_day UNIQUE (thought_id, surfaced_on)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
