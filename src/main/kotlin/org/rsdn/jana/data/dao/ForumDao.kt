package org.rsdn.jana.data.dao

import org.jooq.Record
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.jooq.tables.Forums
import org.rsdn.jana.ui.screens.Forum

class ForumDao(private val db: DatabaseManager) {
    private val f = Forums.FORUMS

    fun getAll(): List<Forum> {
        return db.dsl.select()
            .from(f)
            .orderBy(f.TITLE)
            .fetch()
            .map { record: Record ->
                Forum(
                    id = record.get(f.ID),
                    title = record.get(f.TITLE),
                    description = record.get(f.DESCRIPTION) ?: "",
                    threadsCount = record.get(f.THREADS_COUNT) ?: 0
                )
            }
    }

    fun insertAll(forums: List<Forum>) {
        val now = (System.currentTimeMillis() / 1000).toInt() // конвертируем в Int
        forums.forEach { forum ->
            db.dsl.insertInto(f)
                .columns(f.ID, f.TITLE, f.DESCRIPTION, f.THREADS_COUNT, f.SYNC_AT)
                .values(forum.id, forum.title, forum.description, forum.threadsCount, now)
                .execute()
        }
    }

    fun sync(forums: List<Forum>) {
        val now = (System.currentTimeMillis() / 1000).toInt()

        db.dsl.transaction { config ->
            val ctx = config.dsl()

            // 1. Вставляем или обновляем все пришедшие форумы
            val upserts = forums.map { forum ->
                ctx.insertInto(f)
                    .set(f.ID, forum.id)
                    .set(f.TITLE, forum.title)
                    .set(f.DESCRIPTION, forum.description)
                    .set(f.SYNC_AT, now) // Ставим текущее время
                    .onConflict(f.ID)
                    .doUpdate()
                    .set(f.TITLE, forum.title)
                    .set(f.DESCRIPTION, forum.description)
                    .set(f.SYNC_AT, now)
            }
            ctx.batch(upserts).execute()

            // 2. Удаляем те записи, которые НЕ были обновлены (у них старый SYNC_AT)
            ctx.deleteFrom(f)
                .where(f.SYNC_AT.lessThan(now))
                .execute()
        }
    }

    fun clear() {
        db.dsl.deleteFrom(f).execute()
    }

    fun getCount(): Int {
        return db.dsl.fetchCount(f)
    }
}