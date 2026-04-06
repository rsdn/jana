package org.rsdn.jana.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.rsdn.jana.api.dtos.OAuthTokenResponse
import java.awt.Desktop
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import java.net.ServerSocket
import kotlin.time.Duration.Companion.milliseconds

object PkceHelper {
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    fun generateVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return encoder.encodeToString(bytes)
    }

    fun generateChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        return encoder.encodeToString(hash)
    }

    fun generateState(): String = generateVerifier() // Можно использовать тот же алгоритм
}

class AuthService(private val httpClient: HttpClient) {
    private val clientId = "test_public_client" // Используем проверенный ID

    suspend fun authenticate(): String? = withContext(Dispatchers.IO) {
        val authCodeDeferred = CompletableDeferred<String?>()

        // 1. Находим любой свободный порт
        val freePort = ServerSocket(0).use { it.localPort }
        val redirectUri = "http://127.0.0.1:$freePort/" // Слэш в конце обязателен!

        val codeVerifier = PkceHelper.generateVerifier()
        val codeChallenge = PkceHelper.generateChallenge(codeVerifier)
        val state = PkceHelper.generateState()

        val server = embeddedServer(Netty, port = freePort, host = "127.0.0.1") {
            routing {
                get("/") {
                    val code = call.request.queryParameters["code"]
                    call.respondText("Авторизация успешна! Можете закрыть это окно.")
                    authCodeDeferred.complete(code)
                }
            }
        }.start(wait = false)

        try {
            // 2. Строим URL в точности как рабочий клиент
            val authUrl = URLBuilder("https://api.rsdn.org/connect/auth").apply {
                parameters.append("response_type", "code")
                parameters.append("redirect_uri", redirectUri)
                parameters.append("client_id", clientId)
                parameters.append("scope", "offline_access") // Только этот scope
                parameters.append("state", state)
                parameters.append("code_challenge", codeChallenge)
                parameters.append("code_challenge_method", "S256")
            }.buildString()

            Desktop.getDesktop().browse(URI(authUrl))

            val code = withTimeoutOrNull(60_000.milliseconds) { authCodeDeferred.await() }
            println("[OAuth] Code received: $code") // ОТЛАДКА 1
            if (code == null) return@withContext null

// 3. ОБМЕН НА ТОКЕН
            val tokenUrl = "https://api.rsdn.org/connect/token"
            println("[OAuth] Exchanging code at $tokenUrl with redirect_uri: $redirectUri") // ОТЛАДКА 2

            val response = httpClient.submitForm(
                url = tokenUrl,
                formParameters = parameters {
                    append("grant_type", "authorization_code")
                    append("code", code)
                    append("redirect_uri", redirectUri) // Убедись, что тут тоже есть слэш в конце!
                    append("client_id", clientId)
                    append("code_verifier", codeVerifier)
                }
            )

            if (response.status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()
                println("[OAuth] Token Exchange Failed: ${response.status} - $errorBody") // ОТЛАДКА 3
                return@withContext null
            }

            val tokenData: OAuthTokenResponse = response.body()
            println("[OAuth] Access Token obtained!") // ОТЛАДКА 4
            tokenData.accessToken

        } catch (e: Throwable) {
            e.printStackTrace()
            null
        } finally {
            server.stop(500, 500)
        }
    }
}