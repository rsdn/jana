package org.rsdn.jana.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import org.rsdn.jana.ui.components.TopAppBarWithBack
import org.rsdn.jana.ui.models.Forum

data class Thread(
    val id: Int,
    val title: String,
    val author: String,
    val repliesCount: Int,
    val lastActivity: String
)

@Composable
fun ThreadListScreen(
    forum: Forum,
    onBack: () -> Unit,
    onThreadClick: (Thread) -> Unit
) {
    // Временные моковые данные
    val threads = remember {
        listOf(
            Thread(1, "Проблема с корутинами", "user123", 45, "2 часа назад"),
            Thread(2, "Kotlin 2.0 вышла!", "kotlin_fan", 128, "5 часов назад"),
            Thread(3, "Compose Multiplatform vs Swing", "dev_expert", 23, "вчера"),
            Thread(4, "Как оптимизировать запросы к БД?", "sql_master", 67, "вчера"),
            Thread(5, "Новая версия IntelliJ IDEA", "jetbrains_team", 89, "2 дня назад"),
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBarWithBack(
            title = forum.title,
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(threads) { thread ->
                ThreadCard(
                    thread = thread,
                    onClick = { onThreadClick(thread) }
                )
            }
        }
    }
}

@Composable
fun ThreadCard(
    thread: Thread,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = thread.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Автор - фиксированная ширина 100dp
                Row(
                    modifier = Modifier.width(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Author",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = thread.author,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }

                // Количество ответов - фиксированная ширина 60dp
                Row(
                    modifier = Modifier.width(60.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Replies",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${thread.repliesCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Время последней активности - занимает остаток
                Text(
                    text = thread.lastActivity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}