-- V14__Create_avatars_table.sql
CREATE TABLE avatars (
    gravatar_hash TEXT PRIMARY KEY,
    image_blob BLOB NOT NULL,
    cached_at INTEGER NOT NULL
);