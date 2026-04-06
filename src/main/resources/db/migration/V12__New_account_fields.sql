-- Добавляем новые поля в таблицу авторизации
ALTER TABLE auth_info ADD COLUMN email TEXT;
ALTER TABLE auth_info ADD COLUMN gravatar_hash TEXT;
ALTER TABLE auth_info ADD COLUMN login TEXT;