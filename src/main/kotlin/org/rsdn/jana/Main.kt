package org.rsdn.jana

import androidx.compose.runtime.*
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.ui.MainWindow
import org.rsdn.jana.ui.SplashWindow
import kotlin.time.Duration.Companion.milliseconds

fun main() = application {
    val db = DatabaseManager()

    LaunchedEffect(Unit) {
        db.connect()
    }

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000.milliseconds)
        showSplash = false
    }

    if (showSplash) {
        SplashWindow()
    } else {
        MainWindow(
            onClose = {
                db.close()      // <-- закрываем БД
                exitApplication()
            }
        )
    }
}