package org.rsdn.jana.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import org.rsdn.jana.org.rsdn.jana.ui.screens.OutboxScreen
import org.rsdn.jana.org.rsdn.jana.ui.screens.ThreadListScreen
import org.rsdn.jana.org.rsdn.jana.ui.screens.WatchedScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(onClose: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }

    Window(
        onCloseRequest = onClose,
        title = "Jana - RSDN Client",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Jana") },
                        actions = {
                            IconButton(onClick = { /* sync */ }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Sync")
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Threads") },
                            label = { Text("Threads") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = { Icon(Icons.Default.Bookmark, contentDescription = "Watched") },
                            label = { Text("Watched") }
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = { Icon(Icons.Default.Outbox, contentDescription = "Outbox") },
                            label = { Text("Outbox") }
                        )
                    }
                }
            ) { paddingValues ->
                when (selectedTab) {
                    0 -> ThreadListScreen(paddingValues)
                    1 -> WatchedScreen(paddingValues)
                    2 -> OutboxScreen(paddingValues)
                }
            }
        }
    }
}