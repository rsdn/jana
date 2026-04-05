package org.rsdn.jana.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
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

class RsdnApi : AutoCloseable {
    private val client = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getForums(): List<ForumDescription> = try {
        client.get("https://api.rsdn.org/forums").body()
    } catch (e: Exception) {
        // Логируем только суть ошибки, без огромного стектрейса в консоли
        println("[API] Error loading forums: ${e.localizedMessage}")
        emptyList()
    }

    override fun close() = client.close()
}