package org.rsdn.jana.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.data.dao.AuthState
import org.rsdn.jana.data.dao.ThemeMode
import org.rsdn.jana.resources.*
import org.rsdn.jana.sync.SyncManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    syncManager: SyncManager,
    isSyncing: Boolean,
    themeMode: ThemeMode,
    authState: AuthState,
    onRefresh: () -> Unit,
    onToggleTheme: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onShowAbout: () -> Unit
) {
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

            // 1. Кнопка обновления
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Above
                ),
                tooltip = { PlainTooltip { Text("Обновить списки") } },
                state = rememberTooltipState()
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp).padding(end = 16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(onClick = onRefresh, enabled = !isOffline) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "Обновить",
                            tint = if (isOffline) LocalContentColor.current.copy(alpha = 0.38f)
                            else LocalContentColor.current
                        )
                    }
                }
            }

            // 2. Кнопка темы
            val themeLabel = when (themeMode) {
                ThemeMode.LIGHT -> "Светлая тема"
                ThemeMode.DARK -> "Темная тема"
                ThemeMode.SYSTEM -> "Системная тема"
            }

            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Above
                ),
                tooltip = { PlainTooltip { Text(themeLabel) } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        painter = painterResource(when(themeMode) {
                            ThemeMode.LIGHT -> Res.drawable.ic_light_mode
                            ThemeMode.DARK -> Res.drawable.ic_dark_mode
                            ThemeMode.SYSTEM -> Res.drawable.ic_settings_brightness
                        }),
                        contentDescription = themeLabel
                    )
                }
            }

            // 3. Блок профиля (выделен в UserPanel для чистоты)
            UserPanel(
                authState = authState,
                onLogin = onLogin,
                onLogout = onLogout
            )

            // 4. Кнопка "О программе"
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    positioning = TooltipAnchorPosition.Above
                ),
                tooltip = { PlainTooltip { Text("О программе") } },
                state = rememberTooltipState()
            ) {
                IconButton(onClick = onShowAbout) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_info),
                        contentDescription = "О программе"
                    )
                }
            }
        }
    )
}