package org.rsdn.jana.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import org.rsdn.jana.data.rememberWindowSettingsStore
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.ThemeMode
import org.rsdn.jana.ui.components.*
import org.rsdn.jana.ui.screens.*
import org.rsdn.jana.ui.state.rememberMainState
import org.rsdn.jana.ui.theme.JanaTheme
import org.rsdn.jana.org.rsdn.jana.ui.screens.OutboxScreen
import org.rsdn.jana.org.rsdn.jana.ui.screens.WatchedScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(onClose: () -> Unit, db: DatabaseManager) {
    val state = rememberMainState(db)
    val windowSettingsStore = rememberWindowSettingsStore()

    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var showAbout by remember { mutableStateOf(false) }

    // Загружаем сохранённое состояние окна
    val windowState = remember { windowSettingsStore.loadWindowState() }

    Window(
        onCloseRequest = onClose,
        title = "Jana - RSDN Client",
        state = windowState
    ) {
        // Сохраняем состояние окна при закрытии
        DisposableEffect(Unit) {
            onDispose {
                windowSettingsStore.saveWindowState(windowState)
            }
        }
        JanaTheme(themeMode = themeMode) {
            if (state.isLoading && state.forums.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.weight(1f),
                    topBar = {
                        if (state.currentForum == null && state.currentTopic == null) {
                            MainTopAppBar(
                                syncManager = state.syncManager,
                                isSyncing = state.isSyncing,
                                themeMode = themeMode,
                                authState = state.authState,
                                onRefresh = { state.refreshForums() },
                                onToggleTheme = {
                                    themeMode = when (themeMode) {
                                        ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                        ThemeMode.LIGHT -> ThemeMode.DARK
                                        ThemeMode.DARK -> ThemeMode.SYSTEM
                                    }
                                },
                                onLogin = { state.startLogin() },
                                onLogout = { state.logout() },
                                onShowAbout = { showAbout = true }
                            )
                        }
                    },
                    bottomBar = {
                        if (state.currentForum == null && state.currentTopic == null) {
                            MainBottomBar(
                                selectedTab = state.selectedTab,
                                onTabSelected = { state.selectedTab = it }
                            )
                        }
                    }
                ) { p ->
                    Column(Modifier.padding(p).fillMaxSize()) {
                        if (state.errorMessage != null && state.currentForum == null && state.selectedTab == 0) {
                            ErrorBanner(
                                message = state.errorMessage!!,
                                onRetry = { state.refreshForums() },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Box(Modifier.fillMaxSize()) {
                            when {
                                state.currentTopic != null -> TopicMessagesScreen(
                                    topic = state.currentTopic!!,
                                    onBack = { state.currentTopic = null },
                                    onReply = { parentId -> 
                                        // TODO: Реализовать ответ на сообщение
                                        println("Ответ на сообщение $parentId")
                                    },
                                    db = db,
                                    syncManager = state.syncManager,
                                    authToken = state.authState.token,
                                    onLinkHover = { url -> state.hoveredUrl = url }
                                )
                                state.currentForum != null -> TopicListScreen(
                                    state.currentForum!!, db, state.syncManager,
                                    { state.currentForum = null },
                                    { state.currentTopic = it },
                                    state.topicListScrollPositions
                                )
                                else -> when (state.selectedTab) {
                                    0 -> ForumListScreen(state.forums) { state.currentForum = it }
                                    1 -> WatchedScreen(PaddingValues(0.dp))
                                    2 -> OutboxScreen(PaddingValues(0.dp))
                                }
                            }
                        }
                    }
                }
                // Статус-бар внизу окна
                StatusBar(hoveredUrl = state.hoveredUrl)
                }
            }
        }
        if (showAbout) {
            AboutDialog(onDismiss = { showAbout = false })
        }
    }
}