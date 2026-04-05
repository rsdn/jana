CREATE TABLE IF NOT EXISTS messages (
                                        id INTEGER PRIMARY KEY,
                                        thread_id INTEGER NOT NULL,
                                        parent_id INTEGER,
                                        author TEXT NOT NULL,
                                        date INTEGER NOT NULL,
                                        content TEXT NOT NULL,
                                        is_mention INTEGER DEFAULT 0,
                                        depth INTEGER DEFAULT 0,
                                        sync_at INTEGER,
                                        FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES messages(id) ON DELETE CASCADE
    );

CREATE INDEX idx_messages_thread_id ON messages(thread_id);
CREATE INDEX idx_messages_date ON messages(date);
CREATE INDEX idx_messages_parent_id ON messages(parent_id);