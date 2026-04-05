package org.rsdn.jana.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.ui.utils.getIcon // Наш новый маппер

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
            verticalAlignment = Alignment.Top // Выравнивание по верху для многострочных названий
        ) {
            // 1. ИКОНКА ФОРУМА
            Icon(
                imageVector = forum.getIcon(),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp),
                // Если форум официальный — иконка в цвете Primary, иначе — чуть приглушенная
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

                    if (forum.isInTop) {
                        Icon(
                            Icons.Default.Star, "Top",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (!forum.isWriteAllowed) {
                        Icon(
                            Icons.Default.Lock, "Read Only",
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

                // 4. НИЖНЯЯ ПАНЕЛЬ: CODE + RATE + THREADS
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Чипса кода (делаем чуть меньше отступы, чтобы было компактнее)
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

                    // Счетчик сообщений
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