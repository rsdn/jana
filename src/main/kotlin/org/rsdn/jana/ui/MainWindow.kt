package org.rsdn.jana.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.ForumDao
import org.rsdn.jana.org.rsdn.jana.ui.screens.OutboxScreen
import org.rsdn.jana.org.rsdn.jana.ui.screens.WatchedScreen
import org.rsdn.jana.sync.SyncManager
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(
    onClose: () -> Unit,
    db: DatabaseManager           // <-- добавили
) {
    val syncManager = remember { SyncManager(db) }

    val scope = rememberCoroutineScope()
    val forumDao = remember {
        println("[UI] Creating ForumDao with DB instance: ${db.hashCode()}")
        ForumDao(db)
    }

    var forums by remember { mutableStateOf<List<Forum>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var selectedTab by remember { mutableStateOf(0) }
    var currentForum by remember { mutableStateOf<Forum?>(null) }
    var currentThread by remember { mutableStateOf<Thread?>(null) }

    // Загрузка форумов из БД при запуске
    LaunchedEffect(Unit) {
        scope.launch {
            withContext(Dispatchers.IO) {
                val forumList = forumDao.getAll()
                if (forumList.isEmpty()) {
                    // Первый запуск — синхронизируем с API
                    syncManager.syncForums()
                    forums = forumDao.getAll()
                } else {
                    forums = forumList
                }
                isLoading = false
            }
        }
    }

    Window(
        onCloseRequest = onClose,
        title = "Jana - RSDN Client",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        MaterialTheme {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Scaffold(
                    topBar = {
                        if (selectedTab == 0 && currentForum == null && currentThread == null) {
                            TopAppBar(
                                title = { Text("Jana") },
                                actions = {
                                    IconButton(onClick = {
                                        scope.launch {
                                            isLoading = true
                                            withContext(Dispatchers.IO) {
                                                syncManager.syncForums()
                                                forums = forumDao.getAll()
                                            }
                                            isLoading = false
                                        }
                                    }) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Sync")
                                    }
                                    IconButton(onClick = { /* settings */ }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (currentForum == null && currentThread == null) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Forums") },
                                    label = { Text("Форумы") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Watched") },
                                    label = { Text("Отслеживаемые") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.Outbox, contentDescription = "Outbox") },
                                    label = { Text("Исходящие") }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    when {
                        currentThread != null -> {
                            ThreadMessagesScreen(
                                thread = currentThread!!,
                                onBack = { currentThread = null },
                                onReply = { parentId ->
                                    println("Reply to message: $parentId")
                                }
                            )
                        }
                        currentForum != null -> {
                            ThreadListScreen(
                                forum = currentForum!!,
                                onBack = { currentForum = null },
                                onThreadClick = { thread -> currentThread = thread }
                            )
                        }
                        else -> {
                            when (selectedTab) {
                                0 -> ForumListScreen(
                                    forums = forums,           // <-- передаём список
                                    modifier = Modifier.padding(paddingValues),
                                    onForumClick = { forum -> currentForum = forum }
                                )
                                1 -> WatchedScreen(paddingValues)
                                2 -> OutboxScreen(paddingValues)
                            }
                        }
                    }
                }
            }
        }
    }
}