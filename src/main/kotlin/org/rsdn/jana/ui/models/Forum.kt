package org.rsdn.jana.ui.models

data class Forum(
    val id: Int,
    val title: String,
    val description: String,
    val code: String,
    val threadsCount: Int,
    val groupName: String,
    val groupSortOrder: Int,
    val isInTop: Boolean,
    val isSiteSubject: Boolean,
    val isService: Boolean,
    val isRated: Boolean,
    val rateLimit: Int,
    val isWriteAllowed: Boolean,
    val lastSyncAt: Long? = null
)