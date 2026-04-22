package org.rsdn.jana.sync

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.rsdn.jana.api.RsdnApi
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.ForumDao
import org.rsdn.jana.data.dao.MessageDao
import org.rsdn.jana.ui.components.ServerStatus
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger {}

class SyncManager(db: DatabaseManager) {
    private val api = RsdnApi()
    private val forumDao = ForumDao(db)
    private val messageDao = MessageDao(db)
    var serverStatus by mutableStateOf(ServerStatus.UNKNOWN)
        private set
    // Хранилище последних дат синхронизации в памяти для UI
    val lastSyncTimes = mutableStateMapOf<Int, Long>()

    suspend fun checkServerHealth() = withContext(Dispatchers.IO) {
        try {
            withTimeout(3000.milliseconds) {
                api.getServiceInfo()
            }
            serverStatus = ServerStatus.ONLINE
        } catch (_: Exception) {
            serverStatus = ServerStatus.OFFLINE
        }
    }

    suspend fun syncForums(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            logger.info { "Loading forums from API..." }
            val apiForums = api.getForums()

            if (apiForums.isNotEmpty()) {
                forumDao.sync(apiForums)
                logger.info { "Saved forums to DB" }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e

            logger.error(e) { "Failed to sync forums" }
            throw e
        }
    }

    suspend fun syncMoreTopics(forumId: Int, currentCount: Int) = withContext(Dispatchers.IO) {
        try {
            if (!coroutineContext.isActive) return@withContext

            logger.info { "Loading topics for forum $forumId with offset $currentCount" }
            val result = api.getTopics(forumId, limit = 50, offset = currentCount)

            if (coroutineContext.isActive && result.items.isNotEmpty()) {
                messageDao.saveMessages(result.items)

                val nowSeconds = System.currentTimeMillis() / 1000
                forumDao.updateLastSyncDate(forumId, nowSeconds)

                withContext(Dispatchers.Main) {
                    lastSyncTimes[forumId] = nowSeconds
                }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            logger.error(e) { "Sync topics error for forum $forumId: ${e.message}" }
            throw e
        }
    }

    /**
     * Синхронизировать сообщения топика с пагинацией.
     * Возвращает true, если есть ещё сообщения для загрузки
     */
    suspend fun syncTopicMessages(topicId: Int, currentCount: Int = 0): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!coroutineContext.isActive) return@withContext false

            logger.info { "Loading messages for topic $topicId with offset $currentCount" }
            val result = api.getTopicMessages(topicId, limit = 100, offset = currentCount)

            if (coroutineContext.isActive && result.items.isNotEmpty()) {
                messageDao.saveMessages(result.items)
                result.items.size >= 100
            } else {
                false
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            logger.error(e) { "Sync messages error for topic $topicId: ${e.message}" }
            throw e
        }
    }
}