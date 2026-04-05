package org.rsdn.jana.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import org.rsdn.jana.org.rsdn.jana.ui.screens.OutboxScreen
import org.rsdn.jana.org.rsdn.jana.ui.screens.WatchedScreen
import org.rsdn.jana.ui.screens.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(onClose: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    // Навигация внутри вкладки форумов
    var currentForum by remember { mutableStateOf<Forum?>(null) }
    var currentThread by remember { mutableStateOf<Thread?>(null) }

    Window(
        onCloseRequest = onClose,
        title = "Jana - RSDN Client",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    // Показываем TopAppBar только на главном экране форумов
                    if (selectedTab == 0 && currentForum == null && currentThread == null) {
                        TopAppBar(
                            title = { Text("Jana") },
                            actions = {
                                IconButton(onClick = { /* sync */ }) {
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
                    // Скрываем нижнюю навигацию на вложенных экранах
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
                    // Навигация по тредам
                    currentThread != null -> {
                        ThreadMessagesScreen(
                            thread = currentThread!!,
                            onBack = { currentThread = null },
                            onReply = { parentId ->
                                // TODO: открыть диалог/экран для ответа
                                println("Reply to message: $parentId")
                            }
                        )
                    }
                    // Навигация по форумам
                    currentForum != null -> {
                        ThreadListScreen(
                            forum = currentForum!!,
                            onBack = { currentForum = null },
                            onThreadClick = { thread -> currentThread = thread }
                        )
                    }
                    // Главный экран (вкладки)
                    else -> {
                        when (selectedTab) {
                            0 -> ForumListScreen(
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