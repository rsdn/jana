package org.rsdn.jana.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBack(
    title: String,
    subtitle: String? = null,
    depthInfo: Int? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Column {
                Text(title)
                // Всегда резервируем место под subtitle, чтобы избежать дёргания
                Text(
                    text = when {
                        depthInfo != null && depthInfo > 0 -> "Уровень $depthInfo+"
                        subtitle != null -> subtitle
                        else -> ""
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        depthInfo != null && depthInfo > 0 -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(Res.drawable.ic_back),
                    contentDescription = "Назад"
                )
            }
        }
    )
}
