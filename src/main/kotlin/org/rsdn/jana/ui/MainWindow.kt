package org.rsdn.jana.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.ForumDao
import org.rsdn.jana.org.rsdn.jana.ui.screens.OutboxScreen
import org.rsdn.jana.org.rsdn.jana.ui.screens.WatchedScreen
import org.rsdn.jana.resources.*
import org.rsdn.jana.sync.SyncManager
import org.rsdn.jana.ui.components.ErrorBanner
import org.rsdn.jana.ui.components.ServerStatus
import org.rsdn.jana.ui.components.ServerStatusIndicator
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.ui.models.Topic
import org.rsdn.jana.ui.screens.ForumListScreen
import org.rsdn.jana.ui.screens.TopicListScreen
import org.rsdn.jana.ui.screens.TopicMessagesScreen
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(
    onClose: () -> Unit,
    db: DatabaseManager
) {
    val syncManager = remember { SyncManager(db) }
    val scope = rememberCoroutineScope()
    val forumDao = remember { ForumDao(db) }

    var forums by remember { mutableStateOf<List<Forum>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSyncing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var selectedTab by remember { mutableStateOf(0) }
    var currentForum by remember { mutableStateOf<Forum?>(null) }
    var currentTopic by remember { mutableStateOf<Topic?>(null) }

    fun refreshForums() {
        if (isSyncing) return
        isSyncing = true
        errorMessage = null
        scope.launch {
            try {
                syncManager.syncForums()
                forums = withContext(Dispatchers.IO) { forumDao.getAll() }
            } catch (_: Exception) {
                errorMessage = "Ошибка обновления"
            } finally {
                isSyncing = false
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        // Запускаем бесконечный цикл проверки статуса в отдельной корутине
        scope.launch {
            while (isActive) {
                syncManager.checkServerHealth()
                delay(15000.milliseconds) // Проверка раз в минуту
            }
        }

        val cached = withContext(Dispatchers.IO) { forumDao.getAll() }
        if (cached.isEmpty()) refreshForums() else {
            forums = cached
            isLoading = false
        }
    }

    Window(
        onCloseRequest = onClose,
        title = "Jana - RSDN Client",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        MaterialTheme {
            if (isLoading && forums.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            } else {
                Scaffold(
                    topBar = {
                        if (currentForum == null && currentTopic == null) {
                            TopAppBar(
                                title = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("RSDN")
                                        Spacer(Modifier.width(16.dp))
                                        ServerStatusIndicator(status = syncManager.serverStatus)
                                    }
                                },
                                actions = {
                                    val isOffline = syncManager.serverStatus == ServerStatus.OFFLINE

                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp).padding(end = 16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        IconButton(
                                            onClick = { refreshForums() },
                                            enabled = !isOffline // Кнопка выключается при оффлайне
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_refresh),
                                                contentDescription = "Обновить",
                                                // Визуально приглушаем иконку, когда кнопка выключена
                                                tint = if (isOffline) LocalContentColor.current.copy(alpha = 0.38f)
                                                else LocalContentColor.current
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (currentForum == null && currentTopic == null) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(painterResource(Res.drawable.ic_list), null) },
                                    label = { Text("Форумы") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(painterResource(Res.drawable.ic_bookmark), null) },
                                    label = { Text("Избранное") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(painterResource(Res.drawable.ic_outbox), null) },
                                    label = { Text("Исходящие") }
                                )
                            }
                        }
                    }
                ) { p ->
                    Column(Modifier.padding(p).fillMaxSize()) {
                        // Чистая плашка ошибки БЕЗ кнопок
                        if (errorMessage != null && currentForum == null && selectedTab == 0) {
                            ErrorBanner(
                                message = errorMessage!!,
                                onRetry = { refreshForums() },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Box(Modifier.fillMaxSize()) {
                            when {
                                currentTopic != null -> TopicMessagesScreen(
                                    currentTopic!!,
                                    { currentTopic = null },
                                    {})
                                currentForum != null -> TopicListScreen(
                                    currentForum!!,
                                    db,
                                    syncManager,
                                    { currentForum = null }, { currentTopic = it })
                                else -> when (selectedTab) {
                                    0 -> ForumListScreen(forums) { currentForum = it }
                                    1 -> WatchedScreen(PaddingValues(0.dp))
                                    2 -> OutboxScreen(PaddingValues(0.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}