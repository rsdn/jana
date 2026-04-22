package org.rsdn.jana.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.rsdn.jana.api.dtos.Account
import org.rsdn.jana.api.dtos.ForumDescription
import org.rsdn.jana.api.dtos.MessageBodyDto
import org.rsdn.jana.api.dtos.MessageInfoPagedResult
import org.rsdn.jana.api.dtos.ServiceInfo

class RsdnApi(val token: String? = null) : AutoCloseable {

    val client = HttpClient(CIO) {
        // 1. Настройка JSON сериализации
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }

        // 2. Логирование для отладки (в продакшене можно снизить до INFO или HEADERS)
        install(Logging) {
            level = LogLevel.INFO
        }

        // 3. Единая настройка всех запросов
        defaultRequest {
            url("https://api.rsdn.org/") // Базовый адрес API

            // Если токен передан, добавляем заголовок ОДИН раз
            token?.let {
                header(HttpHeaders.Authorization, "Bearer $it")
            }
        }
    }

    /**
     * Получить информацию о сервере
     */
    suspend fun getServiceInfo(): ServiceInfo {
        return client.get("service/info").body()
    }

    /**
     * Получить список доступных форумов
     */
    suspend fun getForums(): List<ForumDescription> = try {
        client.get("forums").body()
    } catch (e: Exception) {
        println("[API] Error loading forums: ${e.localizedMessage}")
        throw e
    }

    /**
     * Получить список топиков форума
     */
    suspend fun getTopics(
        forumId: Int,
        limit: Int = 50,
        offset: Int = 0
    ): MessageInfoPagedResult {
        return client.get("messages") {
            parameter("forumID", forumId)
            parameter("onlyTopics", true)
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("order", 1)
        }.body()
    }

    /**
     * Получить сообщения топика (включая корневое сообщение и все ответы)
     */
    suspend fun getTopicMessages(
        topicId: Int,
        limit: Int = 100,
        offset: Int = 0
    ): MessageInfoPagedResult {
        return client.get("messages") {
            parameter("topicID", topicId)
            parameter("onlyTopics", false)
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("order", 0) // хронологический порядок для сообщений
        }.body()
    }

    /**
     * Получить сообщение по ID с телом
     * @param messageId ID сообщения
     * @return Полное сообщение с телом в формате HTML
     */
    suspend fun getMessageBody(messageId: Int): MessageBodyDto {
        return client.get("messages/$messageId") {
            parameter("withBodies", true)
            parameter("formatBody", true)
        }.body()
    }

    /**
     * Получить профиль текущего пользователя
     */
    suspend fun getMe(): Account {
        val response = client.get("accounts/me")

        if (response.status != HttpStatusCode.OK) {
            val errorText = response.bodyAsText()
            // Выбрасываем ошибку, чтобы в MainWindow сработал catch и logout()
            throw Exception("API Error ${response.status}: $errorText")
        }

        val account = response.body<Account>()

        // Дополнительная проверка: если сервер вернул 200, но данные пустые
        if (account.displayName.isBlank()) {
            throw Exception("Сервер вернул пустой профиль (Anonymous)")
        }

        return account
    }

    override fun close() {
        client.close()
    }
}