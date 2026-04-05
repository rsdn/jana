-- Добавляем новые флаги и поля в таблицу forums
ALTER TABLE forums ADD COLUMN code TEXT;
ALTER TABLE forums ADD COLUMN is_in_top INTEGER DEFAULT 0;
ALTER TABLE forums ADD COLUMN is_site_subject INTEGER DEFAULT 0;
ALTER TABLE forums ADD COLUMN is_service INTEGER DEFAULT 0;
ALTER TABLE forums ADD COLUMN is_rated INTEGER DEFAULT 0;
ALTER TABLE forums ADD COLUMN rate_limit INTEGER DEFAULT 0;
ALTER TABLE forums ADD COLUMN is_write_allowed INTEGER DEFAULT 1;