package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ShortPublicAccountInfo(
    val id: Int,
    val displayName: String? = null,
    val gravatarHash: String? = null
)