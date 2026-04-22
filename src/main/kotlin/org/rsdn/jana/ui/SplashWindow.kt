package org.rsdn.jana.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.WindowPosition
import org.jetbrains.compose.resources.painterResource
import org.rsdn.jana.resources.Res
import org.rsdn.jana.resources.splash
import java.awt.GraphicsEnvironment

@Composable
fun SplashWindow(targetWindowPosition: Pair<Int, Int>? = null) {
    val splashWidth = 656
    val splashHeight = 400
    
    // Определяем экран для отображения splash
    val position = remember {
        calculateSplashPosition(splashWidth, splashHeight, targetWindowPosition)
    }

    Window(
        onCloseRequest = {},
        title = "",
        state = rememberWindowState(
            width = splashWidth.dp,
            height = splashHeight.dp,
            position = WindowPosition(position.first.dp, position.second.dp)
        ),
        resizable = false,
        undecorated = true,
        transparent = true
    ) {
        MaterialTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
                    .shadow(8.dp, shape = MaterialTheme.shapes.medium)
            ) {
                Image(
                    painter = painterResource(Res.drawable.splash),
                    contentDescription = "Splash",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Контент между 75% (300dp) и 90% (360dp) от высоты 400dp
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 300.dp, bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RSDN@Home",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                offset = Offset(2f, 2f),
                                blurRadius = 8f
                            )
                        ),
                        color = Color.White
                    )

                    Text(
                        text = "rsdn.org offline client",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black.copy(alpha = 0.7f),
                                offset = Offset(1f, 1f),
                                blurRadius = 6f
                            )
                        ),
                        color = Color.White
                    )

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

/**
 * Вычислить позицию splash окна на том же экране, где будет главное окно.
 */
private fun calculateSplashPosition(
    splashWidth: Int,
    splashHeight: Int,
    targetPosition: Pair<Int, Int>?
): Pair<Int, Int> {
    val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
    
    // Если есть сохранённая позиция, находим экран для неё
    if (targetPosition != null) {
        val (targetX, targetY) = targetPosition
        
        // Находим экран, на котором будет главное окно
        for (screen in ge.screenDevices) {
            val screenBounds = screen.defaultConfiguration.bounds
            
            if (screenBounds.contains(targetX, targetY)) {
                // Центрируем splash на этом экране
                val centerX = screenBounds.x + (screenBounds.width - splashWidth) / 2
                val centerY = screenBounds.y + (screenBounds.height - splashHeight) / 2
                return Pair(centerX, centerY)
            }
        }
    }
    
    // Fallback: центрируем на главном экране
    val defaultScreen = ge.defaultScreenDevice
    val screenBounds = defaultScreen.defaultConfiguration.bounds
    val centerX = screenBounds.x + (screenBounds.width - splashWidth) / 2
    val centerY = screenBounds.y + (screenBounds.height - splashHeight) / 2
    return Pair(centerX, centerY)
}