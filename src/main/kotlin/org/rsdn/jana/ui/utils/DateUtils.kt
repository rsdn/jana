package org.rsdn.jana.ui.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Форматирование даты для отображения в UI
 */
fun formatDate(timestamp: Long?): String {
    if (timestamp == null || timestamp == 0L) return ""

    val dateTime = Instant.ofEpochSecond(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
    val now = java.time.LocalDateTime.now()

    return when {
        // Сегодня: "14:20"
        dateTime.toLocalDate() == now.toLocalDate() -> {
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        // В текущем году: "06.04 14:20"
        dateTime.year == now.year -> {
            dateTime.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"))
        }
        // Прошлые года: "06.04.25"
        else -> {
            dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yy"))
        }
    }
}

/**
 * Форматирование последней синхронизации для отображения в UI
 */
fun formatLastSyncStatus(timestamp: Long?): String {
    val time = formatDate(timestamp)
    return if (time.isEmpty()) "Не обновлялось" else "Обновлено: $time"
}