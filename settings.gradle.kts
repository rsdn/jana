rootProject.name = "Jana"

dependencyResolutionManagement {
    repositories {
        // ... другие репозитории
        gradlePluginPortal() // <-- Явно добавляем portal для плагинов
    }
}