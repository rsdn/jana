CREATE TABLE IF NOT EXISTS forum_groups (
                                            id INTEGER PRIMARY KEY,
                                            name TEXT NOT NULL,
                                            sort_order INTEGER DEFAULT 0
);

-- Добавляем колонку в существующую таблицу
ALTER TABLE forums ADD COLUMN group_id INTEGER REFERENCES forum_groups(id);