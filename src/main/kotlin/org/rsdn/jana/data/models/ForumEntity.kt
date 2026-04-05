package org.rsdn.jana.data.models

data class ForumEntity(
    val id: Int,
    val title: String,
    val description: String,
    val threadsCount: Int,
    val syncAt: Long = System.currentTimeMillis()
)