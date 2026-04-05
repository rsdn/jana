CREATE TABLE IF NOT EXISTS outbox (
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,
                                      thread_id INTEGER NOT NULL,
                                      parent_id INTEGER,
                                      content TEXT NOT NULL,
                                      created_at INTEGER NOT NULL,
                                      status TEXT DEFAULT 'pending',
                                      error TEXT,
                                      sent_at INTEGER,
                                      FOREIGN KEY (thread_id) REFERENCES threads(id) ON DELETE CASCADE
    );

CREATE INDEX idx_outbox_status ON outbox(status);
CREATE INDEX idx_outbox_created_at ON outbox(created_at);