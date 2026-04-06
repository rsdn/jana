package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: Int,
    val login: String,
    val email: String? = null,
    val displayName: String,
    val gravatarHash: String? = null,
) {
}