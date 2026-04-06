package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ForumGroupInfo(val id: Int, val name: String, val sortOrder: Int)