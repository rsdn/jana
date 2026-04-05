package org.rsdn.jana.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.rsdn.jana.ui.components.TopAppBarWithBack

data class Message(
    val id: Int,
    val author: String,
    val date: String,
    val content: String,
    val depth: Int = 0 // для вложенных ответов
)

@Composable
fun ThreadMessagesScreen(
    thread: Thread,
    onBack: () -> Unit,
    onReply: (Int?) -> Unit // parentMessageId или null для ответа в тему
) {
    // Временные моковые данные с иерархией
    val messages = remember {
        listOf(
            Message(1, "Автор темы", "вчера", "Первый пост с вопросом", 0),
            Message(2, "Эксперт1", "вчера", "Ответ на вопрос", 0),
            Message(3, "Новичок", "вчера", "Уточняющий вопрос", 1),
            Message(4, "Эксперт1", "сегодня", "Подробный ответ с примером кода", 1),
            Message(5, "Другой пользователь", "сегодня", "Альтернативное решение", 0),
        )
    }

    val listState = rememberLazyListState()

    // Прокрутка вниз при загрузке
    LaunchedEffect(Unit) {
        listState.animateScrollToItem(messages.size - 1)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBarWithBack(
            title = thread.title,
            onBack = onBack
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                MessageCard(
                    message = message,
                    onReply = { onReply(message.id) }
                )
            }
        }

        // Поле для быстрого ответа
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
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Быстрый ответ...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(onClick = { onReply(null) }) {
                    Text("Ответить")
                }
            }
        }
    }
}

@Composable
fun MessageCard(
    message: Message,
    onReply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (message.depth * 16).dp) // отступ для вложенных
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message.author,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message.content,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onReply,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Ответить")
            }
        }
    }
}