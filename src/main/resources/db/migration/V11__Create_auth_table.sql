CREATE TABLE auth_info (
                           id INTEGER PRIMARY KEY CHECK (id = 1), -- Всегда одна запись
                           access_token TEXT,
                           refresh_token TEXT,
                           expires_at INTEGER,
                           user_id INTEGER,
                           display_name TEXT,
                           role TEXT
);