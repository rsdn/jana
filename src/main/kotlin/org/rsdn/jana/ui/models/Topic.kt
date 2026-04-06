package org.rsdn.jana.ui.models

data class Topic(
    val id: Int,
    val title: String,
    val author: String,
    val repliesCount: Int,
    val lastActivity: Long
)