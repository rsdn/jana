package org.rsdn.jana.data.models

/**
 * DTO для сообщений топика из локальной БД
 */
data class MessageInfoDto(
    val id: Int,
    val forumID: Int,
    val topicID: Int,
    val parentID: Int?,
    val subject: String,
    val userName: String,
    val userId: Int?,
    val isTopic: Boolean,
    val answersCount: Int,
    val createdOn: Long,
    val updatedOn: Long
)