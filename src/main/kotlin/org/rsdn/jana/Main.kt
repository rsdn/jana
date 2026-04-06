package org.rsdn.jana

import androidx.compose.runtime.*
import androidx.compose.ui.window.application
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.ui.MainWindow
import org.rsdn.jana.ui.SplashWindow

@OptIn(ExperimentalCoilApi::class)
fun main() = application {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }

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