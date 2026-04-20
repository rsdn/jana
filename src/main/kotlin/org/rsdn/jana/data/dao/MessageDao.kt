package org.rsdn.jana.data.dao

import org.jooq.impl.DSL
import org.jooq.impl.DSL.field
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.api.dtos.MessageInfo
import org.rsdn.jana.data.models.MessageInfoDto
import org.rsdn.jana.ui.models.Topic
import java.time.Instant

class MessageDao(private val db: DatabaseManager) {

    // Вспомогательная функция для парсинга ISO строки в Long (секунды)
    private fun parseIsoToLong(isoStr: String?): Long {
        if (isoStr.isNullOrBlank()) return 0L
        return try {
            // 1. Пытаемся распарсить как полный ISO с Z (UTC)
            Instant.parse(isoStr).epochSecond
        } catch (_: Exception) {
            try {
                // 2. Если Z нет, парсим как LocalDateTime и считаем, что это UTC (или локальное)
                // В RSDN API обычно время сервера (МСК)
                java.time.LocalDateTime.parse(isoStr)
                    .toEpochSecond(java.time.ZoneOffset.ofHours(3)) // МСК это +3
            } catch (_: Exception) {
                0L
            }
        }
    }

    fun getTopicsByForum(forumId: Int): List<Topic> {
        return db.dsl.select()
            .from(DSL.table("messages"))
            .where(field("forum_id").eq(forumId))
            .and(field("is_topic").eq(1))
            .orderBy(field("created_on").desc()) // Сортируем по дате создания темы
            .fetch { r ->
                Topic(
                    id = r.get("id", Int::class.java) ?: 0,
                    title = r.get("subject", String::class.java) ?: "Без темы",
                    author = r.get("user_name", String::class.java) ?: "Аноним",
                    repliesCount = r.get("answers_count", Int::class.java) ?: 0,
                    // Читаем из базы уже как Long
                    lastActivity = r.get("updated_on", Long::class.java) ?: r.get("created_on", Long::class.java) ?: 0L
                )
            }
    }

    /**
     * Получить все сообщения топика из БД
     * Возвращает плоский список сообщений
     */
    fun getMessagesByTopic(topicId: Int): List<MessageInfoDto> {
        return db.dsl.select()
            .from(DSL.table("messages"))
            .where(field("topic_id").eq(topicId))
            .orderBy(field("created_on").asc()) // Хронологический порядок
            .fetch { r ->
                MessageInfoDto(
                    id = r.get("id", Int::class.java) ?: 0,
                    forumID = r.get("forum_id", Int::class.java) ?: 0,
                    topicID = r.get("topic_id", Int::class.java) ?: 0,
                    parentID = r.get("parent_id", Int::class.java),
                    subject = r.get("subject", String::class.java) ?: "",
                    userName = r.get("user_name", String::class.java) ?: "Аноним",
                    userId = r.get("user_id", Int::class.java),
                    isTopic = r.get("is_topic", Int::class.java) == 1,
                    answersCount = r.get("answers_count", Int::class.java) ?: 0,
                    createdOn = r.get("created_on", Long::class.java) ?: 0L,
                    updatedOn = r.get("updated_on", Long::class.java) ?: 0L
                )
            }
    }

    fun saveMessages(messages: List<MessageInfo>) {
        val t = DSL.table("messages")
        db.dsl.transaction { conf ->
            val ctx = DSL.using(conf)
            messages.forEach { msg ->
                val createdTs = parseIsoToLong(msg.createdOn)
                val updatedTs = parseIsoToLong(msg.updatedOn ?: msg.createdOn)

                ctx.insertInto(t)
                    .set(field("id"), msg.id)
                    .set(field("forum_id"), msg.forumID)
                    .set(field("topic_id"), msg.topicID)
                    .set(field("parent_id"), msg.parentID)
                    .set(field("subject"), msg.subject ?: "")
                    .set(field("user_name"), msg.author?.displayName ?: "Аноним")
                    .set(field("user_id"), msg.author?.id)
                    .set(field("is_topic"), if (msg.isTopic) 1 else 0)
                    .set(field("answers_count"), msg.answersCount ?: 0)
                    .set(field("created_on"), createdTs) // Пишем число
                    .set(field("updated_on"), updatedTs) // Пишем число
                    .onConflict(field("id"))
                    .doUpdate()
                    .set(field("subject"), msg.subject ?: "")
                    .set(field("answers_count"), msg.answersCount ?: 0)
                    .set(field("updated_on"), updatedTs)
                    .execute()
            }
        }
    }
}