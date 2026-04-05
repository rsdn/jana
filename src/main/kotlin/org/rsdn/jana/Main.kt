package org.rsdn.jana

import androidx.compose.runtime.*
import androidx.compose.ui.window.application
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.ui.MainWindow
import org.rsdn.jana.ui.SplashWindow

fun main() = application {
    // Создаём ДО композиции и запоминаем
    val db = remember { DatabaseManager() }
    var isDbReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        db.connect()
        isDbReady = true
    }

    if (!isDbReady) {
        SplashWindow()
    } else {
        MainWindow(
            onClose = {
                db.close()
                exitApplication()
            },
            db = db
        )
    }
}