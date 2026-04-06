package org.rsdn.jana.data.dao

import org.rsdn.jana.api.dtos.Account
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.jooq.tables.references.AUTH_INFO

data class AuthState(
    val token: String? = null,
    val displayName: String = "Аноним",
    val isLoggedIn: Boolean = false,
    val gravatarHash: String? = null
) {
    // Вспомогательный метод для получения URL аватарки
    val avatarUrl: String? get() = gravatarHash?.let {
        "https://www.gravatar.com/avatar/$it?s=64&d=identicon"
    }
}

class TokenStorage(private val db: DatabaseManager) {
    fun saveToken(token: String, user: Account) {
        db.dsl.deleteFrom(AUTH_INFO).execute()
        db.dsl.insertInto(AUTH_INFO)
            .set(AUTH_INFO.ACCESS_TOKEN, token)
            .set(AUTH_INFO.DISPLAY_NAME, user.displayName)
            .set(AUTH_INFO.USER_ID, user.id)
            // Добавляем новые поля (убедись, что jOOQ их видит)
            .set(AUTH_INFO.EMAIL, user.email)
            .set(AUTH_INFO.GRAVATAR_HASH, user.gravatarHash)
            .set(AUTH_INFO.LOGIN, user.login)
            .execute()
    }

    fun getAuthState(): AuthState {
        val record = db.dsl.selectFrom(AUTH_INFO).fetchOne()
        return if (record != null) {
            AuthState(
                token = record.accessToken,
                displayName = record.displayName ?: "Юзер",
                isLoggedIn = true,
                gravatarHash = record.get("gravatar_hash", String::class.java) // Безопасное получение, если jOOQ еще не обновлен
            )
        } else {
            AuthState(null)
        }
    }

    /**
     * Удаляет все данные об авторизации (разлогин)
     */
    fun logout() {
        db.dsl.deleteFrom(AUTH_INFO).execute()
    }
}