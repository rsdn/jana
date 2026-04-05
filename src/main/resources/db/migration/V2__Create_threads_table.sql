CREATE TABLE IF NOT EXISTS threads (
                                       id INTEGER PRIMARY KEY,
                                       forum_id INTEGER NOT NULL,
                                       title TEXT NOT NULL,
                                       author TEXT,
                                       replies_count INTEGER DEFAULT 0,
                                       last_activity INTEGER,
                                       is_watched INTEGER DEFAULT 0,
                                       sync_at INTEGER,
                                       FOREIGN KEY (forum_id) REFERENCES forums(id) ON DELETE CASCADE
    );

CREATE INDEX idx_threads_forum_id ON threads(forum_id);
CREATE INDEX idx_threads_last_activity ON threads(last_activity);