package org.rsdn.jana.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class Forum(
    val id: Int,
    val title: String,
    val description: String,
    val threadsCount: Int
)

@Composable
fun ForumListScreen(
    modifier: Modifier = Modifier,  // добавили параметр
    onForumClick: (Forum) -> Unit
) {
    val forums = remember {
        listOf(
            Forum(1, "Программирование", "Обсуждение общих вопросов программирования", 1234),
            Forum(2, "Kotlin", "Вопросы по Kotlin и KMP", 567),
            Forum(3, "Java", "Java-технологии и фреймворки", 890),
            Forum(4, "Алгоритмы", "Алгоритмы и структуры данных", 432),
            Forum(5, "Базы данных", "SQL, NoSQL и всё о БД", 765),
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(), // используем переданный modifier
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(forums) { forum ->
            ForumCard(
                forum = forum,
                onClick = { onForumClick(forum) }
            )
        }
    }
}

@Composable
fun ForumCard(
    forum: Forum,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = forum.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = forum.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Тем: ${forum.threadsCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}