package org.rsdn.jana.utils

import java.util.Properties

object AppInfo {
    val version: String by lazy {
        val props = Properties()
        try {
            // В чистоном JVM проекте файл будет лежать в корне ресурсов
            val inputStream = javaClass.getResourceAsStream("/version.properties")
            if (inputStream != null) {
                props.load(inputStream)
                val v = props.getProperty("version")
                if (!v.isNullOrBlank() && v != "unspecified") return@lazy v
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        "1.0.0-dev" // Фоллбек, если файл не найден или пуст
    }
}