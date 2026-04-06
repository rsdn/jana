package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class MessageInfo(
    val id: Int,
    val forumID: Int,
    val topicID: Int,
    val parentID: Int? = null,
    val author: ShortPublicAccountInfo? = null,
    val subject: String? = null,
    val createdOn: String,
    val updatedOn: String? = null,
    val isTopic: Boolean,
    val answersCount: Int? = null
)