package org.rsdn.jana.ui.components

enum class ServerStatus {
    UNKNOWN,  // Еще не проверяли
    ONLINE,   // Сервер ответил 200 OK
    OFFLINE   // Ошибка сети или таймаут
}