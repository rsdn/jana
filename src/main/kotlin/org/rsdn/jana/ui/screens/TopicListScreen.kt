package org.rsdn.jana.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.MessageDao
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.status_offline
import org.rsdn.jana.sync.SyncManager
import org.rsdn.jana.ui.components.ErrorBanner
import org.rsdn.jana.ui.components.ServerStatus
import org.rsdn.jana.ui.components.TopAppBarWithBack
import org.rsdn.jana.ui.components.TopicCard
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.ui.models.Topic
import org.rsdn.jana.ui.utils.formatDate
import org.rsdn.jana.ui.utils.formatLastSyncStatus

@Composable
fun TopicListScreen(
    forum: Forum,
    db: DatabaseManager,
    syncManager: SyncManager,
    onBack: () -> Unit,
    onTopicClick: (Topic) -> Unit,
    scrollPositions: MutableMap<Int, Pair<Int, Int>> = mutableMapOf()
) {
    val topicDao = remember { MessageDao(db) }
    val scope = rememberCoroutineScope()
    var topics by remember { mutableStateOf<List<Topic>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isFullLoaded by remember { mutableStateOf(false) }
    var lastSeenIndex by remember { mutableStateOf(-1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isOffline = syncManager.serverStatus == ServerStatus.OFFLINE

    // --- ИСПРАВЛЕНИЕ 1: Реактивное время ---
    // derivedStateOf заставляет Compose следить за изменениями внутри мапы SyncManager
    val currentTimestamp by remember(forum.id) {
        derivedStateOf {
            syncManager.lastSyncTimes[forum.id] ?: forum.lastSyncAt
        }
    }

    fun loadNextPage() {
        if (isLoading || isFullLoaded || isOffline) return

        isLoading = true
        errorMessage = null

        scope.launch {
            try {
                val currentSize = topics.size
                syncManager.syncMoreTopics(forum.id, currentSize)

                val updatedList = topicDao.getTopicsByForum(forum.id)
                if (updatedList.size <= currentSize && currentSize > 0) {
                    isFullLoaded = true
                } else {
                    topics = updatedList
                }
            } catch (_: Exception) {
                errorMessage = "Ошибка загрузки списка тем"
            } finally {
                isLoading = false
            }
        }
    }

    // --- ИСПРАВЛЕНИЕ 2: Сначала БД, потом ВСЕГДА сеть ---
    LaunchedEffect(forum.id) {
        // 1. Сначала мгновенно подтягиваем кэш из БД
        val cached = topicDao.getTopicsByForum(forum.id)
        if (cached.isNotEmpty()) {
            topics = cached
        }

        // 2. И сразу запускаем обновление из сети (даже если темы были)
        // Это обновит время "Сегодня в..." и докачает свежак
        loadNextPage()
    }

    LaunchedEffect(lastSeenIndex, isOffline) {
        if (lastSeenIndex >= topics.size - 5 && topics.isNotEmpty() && !isOffline) {
            loadNextPage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBarWithBack(
            title = forum.title,
            subtitle = formatLastSyncStatus(currentTimestamp),
            onBack = onBack
        )

        val listState = rememberLazyListState()
        
        // Восстанавливаем позицию при загрузке экрана
        LaunchedEffect(forum.id) {
            val savedPosition = scrollPositions[forum.id]
            if (savedPosition != null) {
                listState.scrollToItem(savedPosition.first, savedPosition.second)
            }
        }
        
        // Сохраняем позицию скроллинга при изменении
        LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
            scrollPositions[forum.id] = Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(topics, key = { _, t -> t.id }) { index, topic ->
                TopicCard(
                    topic = topic,
                    dateText = formatDate(topic.lastActivity),
                    db = db,
                    onClick = { onTopicClick(topic) }
                )
                SideEffect { lastSeenIndex = index }
            }

            item {
                when {
                    isLoading && topics.isNotEmpty() -> {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }
                    isOffline && !isFullLoaded && topics.isNotEmpty() -> {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.status_offline),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Unspecified
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    text = "Автоподгрузка приостановлена: сервер недоступен",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    errorMessage != null -> {
                        ErrorBanner(
                            message = errorMessage!!,
                            onRetry = { loadNextPage() }
                        )
                    }
                }
            }
        }
    }
}