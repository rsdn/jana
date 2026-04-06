-- V7__Add_messages_table.sql
DROP TABLE IF EXISTS messages; -- Удаляем старую несовместимую версию

CREATE TABLE messages (
                          id INTEGER PRIMARY KEY,
                          forum_id INTEGER NOT NULL,
                          topic_id INTEGER NOT NULL,   -- ID корневого сообщения ветки
                          parent_id INTEGER,           -- ID родительского сообщения (для дерева)
                          subject TEXT,
                          body TEXT,
                          user_name TEXT,
                          user_id INTEGER,
                          created_on TEXT NOT NULL,    -- ISO8601 строка
                          updated_on INTEGER NOT NULL,
                          is_topic BOOLEAN NOT NULL DEFAULT 0,
                          is_read BOOLEAN DEFAULT 0,
                          answers_count INTEGER DEFAULT 0,

                          FOREIGN KEY(forum_id) REFERENCES forums(id) ON DELETE CASCADE
);

CREATE INDEX idx_messages_forum_topics ON messages(forum_id, is_topic, created_on DESC);
CREATE INDEX idx_messages_tree ON messages(topic_id, parent_id);