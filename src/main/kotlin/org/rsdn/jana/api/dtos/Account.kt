package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: Int,
    val login: String? = null,
    val email: String? = null,
    val displayName: String,
    val gravatarHash: String? = null,
    val role: String
)