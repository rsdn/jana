package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

/**
 * Тело сообщения (из спецификации MessageBody)
 * @param isFormatted true если текст отформатирован (HTML)
 * @param text текст сообщения (сырой или HTML в зависимости от isFormatted)
 */
@Serializable
data class MessageBody(
    val isFormatted: Boolean = false,
    val text: String? = null
)

/**
 * Полное сообщение с телом
 * Используется при запросе с параметрами withBodies=true и formatBody=true
 */
@Serializable
data class MessageBodyDto(
    val id: Int,
    val forumID: Int,
    val topicID: Int,
    val parentID: Int? = null,
    val author: ShortPublicAccountInfo? = null,
    val subject: String? = null,
    val body: MessageBody? = null,
    val createdOn: String,
    val updatedOn: String? = null,
    val isTopic: Boolean,
    val answersCount: Int? = null
)
