package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class MessageInfoPagedResult(
    val items: List<MessageInfo>,
    val total: Int,
    val offset: Int
)