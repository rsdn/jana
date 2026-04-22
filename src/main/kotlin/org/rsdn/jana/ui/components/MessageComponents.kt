package org.rsdn.jana.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.ic_expand_less
import org.rsdn.jana.resources.ic_expand_more
import org.rsdn.jana.resources.ic_folder_open
import org.rsdn.jana.ui.models.ComposeElement
import org.rsdn.jana.ui.models.MessageNode
import org.rsdn.jana.ui.theme.LocalMessageStyle
import org.rsdn.jana.ui.utils.formatDate

/**
 * Карточка сообщения с компактным и развёрнутым режимом
 */
@Composable
fun MessageCard(
    node: MessageNode,
    depthOffset: Int = 0,
    db: DatabaseManager,
    onReply: () -> Unit,
    onToggleExpand: () -> Unit,
    onToggleTextExpand: () -> Unit,
    onExpandDeepBranch: (() -> Unit)? = null,
    messageBody: List<ComposeElement>? = null,
    isLoadingBody: Boolean = false,
    isLoadingBodySize: Boolean = false,
    bodyLength: Int = 0,
    onLoadBody: (() -> Unit)? = null,
    onLinkHover: ((String?) -> Unit)? = null
) {
    // Читаем значения из MutableState
    val isExpanded = node.isExpanded.value
    val isTextExpanded = node.isTextExpanded.value
    val hasChildren = node.children.isNotEmpty()
    val formattedDate = formatMessageDate(node.message.createdOn)
    
    // Вычисляем отступ с учётом сдвига глубины
    val effectiveDepth = (node.depth - depthOffset).coerceAtLeast(0)
    val style = LocalMessageStyle.current
    
    // Автозагрузка тела при разворачивании текста
    LaunchedEffect(isExpanded && isTextExpanded) {
        if (isExpanded && isTextExpanded && messageBody == null && !isLoadingBody) {
            onLoadBody?.invoke()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (effectiveDepth * 10).dp, top = 4.dp, bottom = 4.dp)
    ) {
        // Основное содержимое с рамкой
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, style.messageBorderColor, RoundedCornerShape(4.dp))
        ) {
            // Внутреннее содержимое с паддингами
            Column(
                modifier = Modifier
                    .fillMaxWidth()
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

                        // Граватар автора с кешированием (показывает placeholder если hash null)
                        GravatarImage(
                            gravatarHash = node.message.gravatarHash,
                            size = 20.dp,
                            db = db
                        )
                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = node.message.userName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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

                    // Тело сообщения
                    when {
                        isLoadingBody -> {
                            // Индикатор загрузки
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                        !messageBody.isNullOrEmpty() -> {
                            // Отрендеренное тело сообщения
                            MessageBodyView(
                                elements = messageBody,
                                onLinkHover = onLinkHover
                            )
                        }
                        else -> {
                            // Заглушка если нет данных (ошибка загрузки)
                            Text(
                                text = "Текст сообщения недоступен",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

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

            // Полоска для разворачивания/сворачивания текста сообщения - ВСЕГДА когда isExpanded
            // Находится вне паддинга, примыкает к рамке
            if (isExpanded) {
                // Вычисляем размер тела
                // Если тело загружено - считаем из него, иначе используем переданный bodyLength
                val bodySize = messageBody?.let { body ->
                    body.sumOf { element ->
                        when (element) {
                            is ComposeElement.Paragraph -> element.segments.sumOf { 
                                when (it) {
                                    is ComposeElement.ParagraphSegment.Text -> it.text.length
                                    is ComposeElement.ParagraphSegment.Link -> it.text.length
                                }
                            }
                            is ComposeElement.CodeBlock -> element.code.length
                            is ComposeElement.BlockQuote -> 100 // Приблизительно
                            is ComposeElement.ListBlock -> element.items.size * 50
                            is ComposeElement.Table -> element.rows.sumOf { it.cells.size * 20 }
                            else -> 50
                        }
                    }
                } ?: bodyLength
                
                // Текст размера: три состояния
                val sizeText = when {
                    bodySize > 0 -> "$bodySize символов"
                    isLoadingBodySize -> "Загрузка..."
                    else -> null // Нет тела (оффлайн или ошибка)
                }
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onToggleTextExpand),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Слева: пустой spacer чтобы центрировать шеврон
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // По центру: шеврон
                        Icon(
                            painter = painterResource(
                                if (isTextExpanded) Res.drawable.ic_expand_less else Res.drawable.ic_expand_more
                            ),
                            contentDescription = if (isTextExpanded) "Свернуть текст" else "Развернуть текст",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        
                        // Справа: размер (три состояния)
                        Spacer(modifier = Modifier.weight(1f))
                        if (sizeText != null) {
                            Text(
                                text = sizeText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
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