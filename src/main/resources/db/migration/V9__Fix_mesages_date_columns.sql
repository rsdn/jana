-- V7__Add_messages_table.sql (обновленная)
DROP TABLE IF EXISTS messages;

CREATE TABLE messages (
                          id INTEGER PRIMARY KEY,
                          forum_id INTEGER NOT NULL,
                          topic_id INTEGER NOT NULL,
                          parent_id INTEGER,
                          subject TEXT,
                          body TEXT,
                          user_name TEXT,
                          user_id INTEGER,
                          created_on INTEGER NOT NULL,    -- ТЕПЕРЬ INTEGER (Unix Timestamp в секундах)
                          updated_on INTEGER NOT NULL,    -- Тоже INTEGER
                          is_topic BOOLEAN NOT NULL DEFAULT 0,
                          is_read BOOLEAN DEFAULT 0,
                          answers_count INTEGER DEFAULT 0,

                          FOREIGN KEY(forum_id) REFERENCES forums(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_forum_topics ON messages(forum_id, is_topic, created_on DESC);