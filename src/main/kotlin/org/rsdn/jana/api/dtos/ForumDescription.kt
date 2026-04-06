package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ForumDescription(
    val id: Int,
    val code: String = "",
    val name: String = "",
    val description: String = "",
    val forumGroup: ForumGroupInfo,
    val isInTop: Boolean = false,
    val isSiteSubject: Boolean = false,
    val isService: Boolean = false,
    val isRated: Boolean = false,
    val rateLimit: Int = 0,
    val isWriteAllowed: Boolean = false
)