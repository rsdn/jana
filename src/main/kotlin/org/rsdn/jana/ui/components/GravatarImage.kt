package org.rsdn.jana.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.AvatarDao
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.ic_person

/**
 * Компонент для отображения граватара с кешированием в БД
 * Всегда показывает что-то: аватарку или placeholder
 */
@Composable
fun GravatarImage(
    gravatarHash: String?,
    size: Dp,
    db: DatabaseManager,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val avatarDao = remember { AvatarDao(db) }
    
    // Сначала пытаемся загрузить из кеша
    var cachedImage by remember { mutableStateOf<ByteArray?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(gravatarHash) {
        if (gravatarHash == null) {
            isLoading = false
        } else {
            withContext(Dispatchers.IO) {
                cachedImage = avatarDao.getAvatar(gravatarHash)
                isLoading = false
            }
        }
    }
    
    val pixelSize = size.value.toInt()
    val finalModifier = modifier
        .size(size)
        .clip(CircleShape)
    
    Box(
        modifier = finalModifier,
        contentAlignment = Alignment.Center
    ) {
        when {
            // Есть кешированное изображение
            cachedImage != null -> {
                AsyncImage(
                    model = cachedImage,
                    contentDescription = "Аватар",
                    modifier = Modifier.matchParentSize(),
                    contentScale = contentScale
                )
            }
            // Хеш есть, но ещё загружается
            gravatarHash != null && isLoading -> {
                // Показываем placeholder
                Icon(
                    painter = painterResource(Res.drawable.ic_person),
                    contentDescription = "Аватар",
                    modifier = Modifier.size(size * 0.7f),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            // Хеш есть, но нет в кеше - загружаем с gravatar.com
            gravatarHash != null && !isLoading -> {
                AsyncImage(
                    model = "https://www.gravatar.com/avatar/$gravatarHash?s=$pixelSize&d=identicon",
                    contentDescription = "Аватар",
                    modifier = Modifier.matchParentSize(),
                    contentScale = contentScale,
                    onSuccess = {
                        // Сохраняем в БД при успешной загрузке
                        Thread {
                            try {
                                avatarDao.downloadAndCacheAvatar(gravatarHash, pixelSize)
                            } catch (_: Exception) {}
                        }.start()
                    }
                )
            }
            // Хеша нет - показываем placeholder
            else -> {
                Icon(
                    painter = painterResource(Res.drawable.ic_person),
                    contentDescription = "Аватар",
                    modifier = Modifier.size(size * 0.7f),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}