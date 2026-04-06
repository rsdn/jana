package org.rsdn.jana.api.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ServiceInfo(
    val name: String,
    val serverTime: String,
    val serverVersion: String,
    val serverBuildDate: String
)