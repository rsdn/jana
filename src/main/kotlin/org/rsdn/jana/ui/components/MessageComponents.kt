package org.rsdn.jana.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.ic_expand_less
import org.rsdn.jana.resources.ic_expand_more
import org.rsdn.jana.resources.ic_folder_open
import org.rsdn.jana.ui.models.MessageNode
import org.rsdn.jana.ui.utils.formatDate

/**
 * Карточка сообщения с компактным и развёрнутым режимом
 */
@Composable
fun MessageCard(
    node: MessageNode,
    depthOffset: Int = 0,
    onReply: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggleTextExpand: () -> Unit,
    onExpandDeepBranch: (() -> Unit)? = null
) {
    // Читаем значения из MutableState
    val isExpanded = node.isExpanded.value
    val isTextExpanded = node.isTextExpanded.value
    val hasChildren = node.children.isNotEmpty()
    val formattedDate = formatMessageDate(node.message.createdOn)
    
    // Вычисляем отступ с учётом сдвига глубины
    val effectiveDepth = (node.depth - depthOffset).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (effectiveDepth * 10).dp)
            .clickable(onClick = onToggleTextExpand)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Заголовок с автором и кнопкой сворачивания
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Кнопка сворачивания/разворачивания дочерних сообщений
                    if (hasChildren) {
                        IconButton(
                            onClick = onToggleExpand,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    if (isExpanded) Res.drawable.ic_expand_less else Res.drawable.ic_expand_more
                                ),
                                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(20.dp))
                    }

                    Text(
                        text = node.message.userName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Индикатор развёрнутого текста
                if (isTextExpanded) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_expand_less),
                        contentDescription = "Свернуть текст",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Тема сообщения (если есть) - показываем всегда
            if (node.message.subject.isNotEmpty() && !node.message.isTopic) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = node.message.subject,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color(0xFF333333),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Индикатор глубокой ветки - показываем ВСЕГДА для пограничных узлов с потомками
            // (не зависит от развёрнутости текста)
            if (hasChildren && onExpandDeepBranch != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onExpandDeepBranch)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_folder_open),
                            contentDescription = "Глубокая ветка",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Ещё ${node.totalDescendants} сообщений в ветке",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "Показать ▶",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Развёрнутый текст сообщения - показываем только если узел развёрнут И текст развёрнут
            if (isExpanded && isTextExpanded) {
                Spacer(modifier = Modifier.height(4.dp))

                // Тело сообщения (заглушка)
                Text(
                    text = buildMessagePlaceholder(node.message.id),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Кнопки действий
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onReply,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Ответить")
                    }

                    if (hasChildren && node.isDeepNode) {
                        TextButton(
                            onClick = onToggleExpand,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Свернуть")
                        }
                    }
                }
            }
    }
}

/**
 * Поле для быстрого ответа
 */
@Composable
fun QuickReplyField(
    onSend: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Быстрый ответ...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = {
                    onSend()
                    text = ""
                }
            ) {
                Text("Ответить")
            }
        }
    }
}


/**
 * Форматирование даты сообщения
 */
private fun formatMessageDate(timestamp: Long): String {
    return formatDate(timestamp)
}

/**
 * Генерация текста-заглушки для сообщения
 */
private fun buildMessagePlaceholder(messageId: Int): String {
    // Заглушка на ~5 строк
    return """
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.
        
        Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.
        
        Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium.
    """.trimIndent()
}
