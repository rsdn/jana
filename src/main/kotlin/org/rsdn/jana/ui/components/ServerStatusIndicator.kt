package org.rsdn.jana.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.resources.*

/**
 * Отдельный компонент для отображения статуса сервера RSDN.
 * Использует иконки из ресурсов проекта.
 */
@Composable
fun ServerStatusIndicator(
    status: ServerStatus,
    modifier: Modifier = Modifier
) {
    // Выбираем иконку в зависимости от стейта
    val iconResource = when (status) {
        ServerStatus.ONLINE -> Res.drawable.status_online
        ServerStatus.OFFLINE -> Res.drawable.status_offline
        ServerStatus.UNKNOWN -> Res.drawable.status_unknown
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Сама иконка (24dp как договорились)
        Icon(
            painter = painterResource(iconResource),
            contentDescription = "Server Status: $status",
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified // Чтобы не перекрашивать твои цветные иконки
        )

        // Дополнительный текст, если мы в оффлайне
        if (status == ServerStatus.OFFLINE) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Offline",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}