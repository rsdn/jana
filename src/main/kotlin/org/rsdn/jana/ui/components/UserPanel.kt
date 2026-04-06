package org.rsdn.jana.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.data.dao.AuthState
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.ic_logout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPanel(
    authState: AuthState,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation = 2.dp,
        shadowElevation = 1.dp, // Добавим легкую тень для объема
        shape = RoundedCornerShape(24.dp),
        modifier = modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp, end = 4.dp).height(40.dp)
        ) {
            if (authState.isLoggedIn) {
                AsyncImage(
                    model = authState.avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = authState.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Тултип с актуальным API
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above, // Явно передаем обязательный параметр
                        spacingBetweenTooltipAndAnchor = 4.dp     // Опциональный отступ
                    ),
                    tooltip = { PlainTooltip { Text("Выйти") } },
                    state = rememberTooltipState()
                ) {
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_logout),
                            contentDescription = "Выйти",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                TextButton(
                    onClick = onLogin,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text("Войти", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}