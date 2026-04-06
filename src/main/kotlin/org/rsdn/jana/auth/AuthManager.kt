package org.rsdn.jana.auth

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.rsdn.jana.api.RsdnApi
import org.rsdn.jana.data.dao.AuthState
import org.rsdn.jana.data.dao.TokenStorage

object AuthManager {
    private val jsonConfig = Json { ignoreUnknownKeys = true }

    /**
     * Выполняет полный цикл авторизации:
     * 1. Открывает браузер для получения токена
     * 2. Запрашивает данные профиля (getMe)
     * 3. Сохраняет всё в БД через TokenStorage
     */
    suspend fun performLogin(tokenStorage: TokenStorage): AuthState? {
        // Создаем временный клиент для процесса логина
        val authClient = HttpClient(CIO) {
            install(ContentNegotiation) { json(jsonConfig) }
        }

        return try {
            authClient.use { client ->
                // 1. Получаем токен (OAuth/Browser)
                val token = AuthService(client).authenticate() ?: return null

                // 2. Получаем данные пользователя
                val api = RsdnApi(token)
                val user = api.getMe()

                // 3. Сохраняем в базу (в IO потоке)
                withContext(Dispatchers.IO) {
                    tokenStorage.saveToken(token, user)
                }

                api.close()

                // Возвращаем актуальное состояние из БД
                tokenStorage.getAuthState()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}