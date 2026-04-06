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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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

    private fun getRelativeDateTimeString(timestamp: Long?): String {
        if (timestamp == null || timestamp == 0L) return ""

        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(timestamp),
            ZoneId.systemDefault()
        )
        val now = LocalDateTime.now()

        return when {
            // Сегодня: "14:20"
            dateTime.toLocalDate() == now.toLocalDate() -> {
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            }
            // В текущем году: "06.04 14:20" (без года, чтобы влезло)
            dateTime.year == now.year -> {
                dateTime.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
            }
            // Прошлые года: "06.04.25" (время убираем, так как дата важнее)
            else -> {
                dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
            }
        }
    }

    fun formatLastSyncStatus(timestamp: Long?): String {
        val time = getRelativeDateTimeString(timestamp)
        return if (time.isEmpty()) "Не обновлялось" else "Обновлено: $time"
    }

    fun formatTopicDate(timestamp: Long?): String {
        return getRelativeDateTimeString(timestamp)
    }

    suspend fun checkServerHealth() = withContext(Dispatchers.IO) {
        try {
            // Устанавливаем жесткий таймаут для проверки связи
            withTimeout(3000.milliseconds) {
                api.getServiceInfo()
            }
            serverStatus = ServerStatus.ONLINE
        } catch (_: Exception) {
            // Любая ошибка (Network, Timeout, 404) — сервер недоступен
            serverStatus = ServerStatus.OFFLINE
        }
    }

    suspend fun syncForums(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            logger.info { "Loading forums from API..." }
            val apiForums = api.getForums()

            if (apiForums.isNotEmpty()) {
                // Здесь оставляем твою логику маппинга и сохранения
                forumDao.sync(apiForums)
                logger.info { "Saved forums to DB" }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Пробрасываем CancellationException, чтобы корутины работали корректно
            if (e is kotlinx.coroutines.CancellationException) throw e

            logger.error(e) { "Failed to sync forums" }
            // КИДАЕМ ОШИБКУ ДАЛЬШЕ, чтобы MainWindow поймал её в catch и показал плашку
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

                // 1. Получаем текущий Unix Timestamp (секунды)
                val nowSeconds = System.currentTimeMillis() / 1000

                // 2. Пишем в БД число
                forumDao.updateLastSyncDate(forumId, nowSeconds)

                // 3. Обновляем StateMap числом
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
}