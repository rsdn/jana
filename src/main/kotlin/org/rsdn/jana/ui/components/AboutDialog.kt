package org.rsdn.jana.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.api.RsdnApi
import org.rsdn.jana.api.dtos.ServiceInfo
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.ic_close // ТВОЯ иконка (проверь, что в drawable лежит современный SVG/XML)
import org.rsdn.jana.resources.splash
import org.rsdn.jana.utils.AppInfo
import java.awt.Desktop
import java.net.URI

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    var serviceInfo by remember { mutableStateOf<ServiceInfo?>(null) }

    LaunchedEffect(Unit) {
        try {
            val info = withContext(Dispatchers.IO) {
                RsdnApi().use { it.getServiceInfo() }
            }
            serviceInfo = info
        } catch (_: Exception) { }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .height(520.dp)
                .clip(RoundedCornerShape(12.dp)),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // 1. Фон
                Image(
                    painter = painterResource(Res.drawable.splash),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 2. Мягкий градиент в самом низу (чтобы не перекрывать центр)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.75f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.8f)
                            )
                        )
                )

                // 3. Крестик БЕЗ кружка
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp) // Чуть меньше отступ, чтобы не «дырявить» угол
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = "Закрыть",
                        tint = Color.White.copy(alpha = 0.7f), // Сделали чуть мягче по цвету
                        modifier = Modifier.size(20.dp)
                    )
                }

                // 4. Контент в нижней «подвальной» зоне
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Кнопка сайта (сделаем её еще менее навязчивой)
                    OutlinedButton(
                        onClick = {
                            try { Desktop.getDesktop().browse(URI("https://rsdn.org")) } catch (_: Exception) {}
                        },
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Text("rsdn.org", style = MaterialTheme.typography.labelMedium)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Техническая инфа (в одну строку, чтобы не лезло вверх)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Версия клиента
                        Text(
                            text = "v${AppInfo.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        // Разделитель
                        Box(Modifier.padding(horizontal = 8.dp).size(1.dp, 8.dp).background(Color.White.copy(alpha = 0.2f)))

                        // Копирайт
                        Text(
                            text = "© 2026 RSDN Team",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }

                    // Серверная инфа (самая нижняя и тусклая строчка)
                    serviceInfo?.let { info ->
                        val date = info.serverBuildDate.split("T").firstOrNull() ?: ""
                        Text(
                            text = "API v${info.serverVersion} [$date]",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.3f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}