package org.rsdn.jana.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ForumGroupInfo(val id: Int, val name: String, val sortOrder: Int)

@Serializable
data class ForumDescription(
    val id: Int,
    val code: String = "",
    val name: String = "",
    val description: String = "", // Теперь, если поля нет, будет пустая строка
    val forumGroup: ForumGroupInfo,
    val isInTop: Boolean = false,
    val isSiteSubject: Boolean = false,
    val isService: Boolean = false,
    val isRated: Boolean = false,
    val rateLimit: Int = 0,
    val isWriteAllowed: Boolean = false
)

@Serializable
data class ShortPublicAccountInfo(
    val id: Int,
    val displayName: String? = null,
    val gravatarHash: String? = null
)

@Serializable
data class MessageInfo(
    val id: Int,
    val forumID: Int,
    val topicID: Int,
    val parentID: Int? = null, // ДОБАВИТЬ ЭТО
    val author: ShortPublicAccountInfo? = null,
    val subject: String? = null,
    val createdOn: String,
    val updatedOn: String? = null,
    val isTopic: Boolean,
    val answersCount: Int? = null
)

@Serializable
data class MessageInfoPagedResult(
    val items: List<MessageInfo>,
    val total: Int,
    val offset: Int
)

@Serializable
data class ServiceInfo(
    val name: String,
    val serverTime: String,
    val serverVersion: String,
    val serverBuildDate: String
)

class RsdnApi : AutoCloseable {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        defaultRequest {
            url("https://api.rsdn.org/") // Базовый URL
        }
    }

    suspend fun getServiceInfo(): ServiceInfo {
        return client.get("https://api.rsdn.org/service/info").body()
    }

    suspend fun getForums(): List<ForumDescription> = try {
        client.get("https://api.rsdn.org/forums").body()
    } catch (e: Exception) {
        // Логируем только суть ошибки, без огромного стектрейса в консоли
        println("[API] Error loading forums: ${e.localizedMessage}")
        throw e
    }

    suspend fun getTopics(
        forumId: Int,
        limit: Int = 50,
        offset: Int = 0
    ): MessageInfoPagedResult {
        return client.get("https://api.rsdn.org/messages") {
            parameter("forumID", forumId)
            parameter("onlyTopics", true)
            parameter("limit", limit)
            parameter("offset", offset)
            parameter("order", 1) // По дате обновления (из Swagger)
        }.body()
    }

    override fun close() = client.close()
}