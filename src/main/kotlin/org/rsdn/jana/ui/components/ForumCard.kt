package org.rsdn.jana.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.resources.*
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.ui.utils.getIconPainter // Используем версию с Painter

@Composable
fun ForumCard(
    forum: Forum,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentAlpha = if (forum.isService) 0.4f else 1f
    val isPrimary = forum.isSiteSubject

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 1. ИКОНКА ФОРУМА (через наш маппер ресурсов)
            Icon(
                painter = forum.getIconPainter(),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp),
                tint = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 2. НАЗВАНИЕ + СТАТУСЫ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = forum.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Иконка "TOP"
                    if (forum.isInTop) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_star),
                            contentDescription = "Top",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Иконка "Read Only"
                    if (!forum.isWriteAllowed) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_lock),
                            contentDescription = "Read Only",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // 3. ОПИСАНИЕ
                if (forum.description.isNotBlank()) {
                    Text(
                        text = forum.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                // 4. НИЖНЯЯ ПАНЕЛЬ
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = forum.code.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    if (forum.isRated && forum.rateLimit in 1..30000) {
                        Text(
                            text = "R:${forum.rateLimit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = forum.threadsCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}