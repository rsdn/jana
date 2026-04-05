package org.rsdn.jana.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

@Composable
fun ForumGroupHeader(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    // Анимация поворота стрелочки на 90 градусов
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        label = "ArrowRotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        // Используем чуть более темный или акцентный фон для заголовка
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка-стрелочка с анимацией
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                modifier = Modifier
                    .size(20.dp)
                    .rotate(rotationState),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}