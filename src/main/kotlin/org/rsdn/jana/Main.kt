package org.rsdn.jana

import androidx.compose.runtime.*
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay
import org.rsdn.jana.ui.MainWindow
import org.rsdn.jana.ui.SplashWindow

fun main() = application {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSplash = false
    }

    if (showSplash) {
        SplashWindow()
    } else {
        MainWindow(onClose = ::exitApplication)
    }
}