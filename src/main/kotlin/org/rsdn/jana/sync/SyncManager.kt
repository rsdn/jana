package org.rsdn.jana.sync

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.rsdn.jana.api.RsdnApi
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.ForumDao
import org.rsdn.jana.ui.models.Forum

private val logger = KotlinLogging.logger {}

class SyncManager(db: DatabaseManager) {
    private val api = RsdnApi()
    private val forumDao = ForumDao(db)

    suspend fun syncForums(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            logger.info { "Loading forums from API..." }
            val apiForums = api.getForums()

            if (apiForums.isNotEmpty()) {
                val forums = apiForums.map { apiForum ->
                    Forum(
                        id = apiForum.id,
                        title = apiForum.name,
                        description = apiForum.description,
                        code = apiForum.code,                   // НОВОЕ
                        threadsCount = 0,                             // Пока 0 или сколько есть
                        groupName = apiForum.forumGroup.name,
                        groupSortOrder = apiForum.forumGroup.sortOrder,
                        isInTop = apiForum.isInTop,                   // НОВОЕ
                        isSiteSubject = apiForum.isSiteSubject,       // НОВОЕ
                        isService = apiForum.isService,               // НОВОЕ
                        isRated = apiForum.isRated,                   // НОВОЕ
                        rateLimit = apiForum.rateLimit,               // НОВОЕ
                        isWriteAllowed = apiForum.isWriteAllowed      // НОВОЕ
                    )
                }

                forumDao.sync(apiForums)
                logger.info { "Saved ${forums.size} forums to DB" }
            } else {
                logger.warn { "API returned empty forums list" }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            logger.error(e) { "Failed to sync forums" }
            Result.failure(e)
        }
    }
}