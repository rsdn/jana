CREATE TABLE IF NOT EXISTS forums (
                                      id INTEGER PRIMARY KEY,
                                      title TEXT NOT NULL,
                                      description TEXT,
                                      threads_count INTEGER DEFAULT 0,
                                      last_activity INTEGER,
                                      is_watched INTEGER DEFAULT 0,
                                      sync_at INTEGER
);

CREATE INDEX idx_forums_last_activity ON forums(last_activity);