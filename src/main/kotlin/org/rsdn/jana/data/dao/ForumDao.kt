package org.rsdn.jana.data.dao

import org.jooq.impl.DSL
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.jooq.tables.Forums
import org.rsdn.jana.data.jooq.tables.ForumGroups // Появится после генерации!
import org.rsdn.jana.api.ForumDescription // Импортируй модель из API
import org.rsdn.jana.ui.models.Forum

class ForumDao(private val db: DatabaseManager) {
    fun getAll(): List<Forum> {
        val f = Forums.FORUMS
        val fg = ForumGroups.FORUM_GROUPS

        return db.dsl.select()
            .from(f)
            .join(fg).on(f.GROUP_ID.eq(fg.ID))
            .orderBy(
                f.IS_SERVICE.asc(),
                fg.SORT_ORDER.asc(),
                f.TITLE.asc()
            )
            .fetch { r ->
                Forum(
                    id = r.get(f.ID),
                    title = r.get(f.TITLE),
                    description = r.get(f.DESCRIPTION) ?: "",
                    code = r.get(f.CODE) ?: "",
                    threadsCount = r.get(f.THREADS_COUNT) ?: 0,
                    groupName = r.get(fg.NAME),
                    groupSortOrder = r.get(fg.SORT_ORDER) ?: 0,
                    // Конвертируем Int из SQLite в Boolean
                    isInTop = r.get(f.IS_IN_TOP) == 1,
                    isSiteSubject = r.get(f.IS_SITE_SUBJECT) == 1,
                    isService = r.get(f.IS_SERVICE) == 1,
                    isRated = r.get(f.IS_RATED) == 1,
                    rateLimit = r.get(f.RATE_LIMIT) ?: 0,
                    isWriteAllowed = r.get(f.IS_WRITE_ALLOWED) == 1
                )
            }
    }

    fun sync(apiForums: List<ForumDescription>) {
        val now = (System.currentTimeMillis() / 1000).toInt()

        db.dsl.transaction { config ->
            val ctx = DSL.using(config)
            val f = Forums.FORUMS
            val fg = ForumGroups.FORUM_GROUPS

            // 1. Обновляем группы
            apiForums.map { it.forumGroup }.distinctBy { it.id }.forEach { group ->
                ctx.insertInto(fg)
                    .set(fg.ID, group.id)
                    .set(fg.NAME, group.name)
                    .set(fg.SORT_ORDER, group.sortOrder)
                    .onConflict(fg.ID).doUpdate()
                    .set(fg.NAME, group.name)
                    .set(fg.SORT_ORDER, group.sortOrder)
                    .execute()
            }

            // 2. Обновляем форумы (добавляем все новые поля)
            val queries = apiForums.map { api ->
                ctx.insertInto(f)
                    .set(f.ID, api.id)
                    .set(f.TITLE, api.name)
                    .set(f.DESCRIPTION, api.description)
                    .set(f.CODE, api.code) // <--- ПОШЛИ НОВЫЕ ПОЛЯ
                    .set(f.GROUP_ID, api.forumGroup.id)
                    .set(f.IS_IN_TOP, if (api.isInTop) 1 else 0)
                    .set(f.IS_SITE_SUBJECT, if (api.isSiteSubject) 1 else 0)
                    .set(f.IS_SERVICE, if (api.isService) 1 else 0)
                    .set(f.IS_RATED, if (api.isRated) 1 else 0)
                    .set(f.RATE_LIMIT, api.rateLimit)
                    .set(f.IS_WRITE_ALLOWED, if (api.isWriteAllowed) 1 else 0)
                    .set(f.SYNC_AT, now)
                    .onConflict(f.ID).doUpdate()
                    .set(f.TITLE, api.name)
                    .set(f.DESCRIPTION, api.description)
                    .set(f.CODE, api.code)
                    .set(f.GROUP_ID, api.forumGroup.id)
                    .set(f.IS_IN_TOP, if (api.isInTop) 1 else 0)
                    .set(f.IS_SITE_SUBJECT, if (api.isSiteSubject) 1 else 0)
                    .set(f.IS_SERVICE, if (api.isService) 1 else 0)
                    .set(f.IS_RATED, if (api.isRated) 1 else 0)
                    .set(f.RATE_LIMIT, api.rateLimit)
                    .set(f.IS_WRITE_ALLOWED, if (api.isWriteAllowed) 1 else 0)
                    .set(f.SYNC_AT, now)
            }
            ctx.batch(queries).execute()

            // 3. Удаляем старое
            ctx.deleteFrom(f).where(f.SYNC_AT.lt(now)).execute()
        }
    }
}