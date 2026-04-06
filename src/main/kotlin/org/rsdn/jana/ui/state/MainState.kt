package org.rsdn.jana.ui.state

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.rsdn.jana.auth.AuthManager
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.dao.AuthState
import org.rsdn.jana.data.dao.ForumDao
import org.rsdn.jana.data.dao.TokenStorage
import org.rsdn.jana.sync.SyncManager
import org.rsdn.jana.ui.models.Forum
import org.rsdn.jana.ui.models.Topic

class MainState(
    private val db: DatabaseManager,
    private val scope: CoroutineScope
) {
    val syncManager = SyncManager(db)
    private val forumDao = ForumDao(db)
    private val tokenStorage = TokenStorage(db)

    // --- Реактивные состояния ---
    var forums by mutableStateOf<List<Forum>>(emptyList())
    var isLoading by mutableStateOf(true)
    var isSyncing by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var authState by mutableStateOf(tokenStorage.getAuthState())

    // Навигация
    var selectedTab by mutableIntStateOf(0)
    var currentForum by mutableStateOf<Forum?>(null)
    var currentTopic by mutableStateOf<Topic?>(null)

    /**
     * Первичная загрузка и запуск фонового чека сервера
     */
    fun init() {
        scope.launch {
            val cached = withContext(Dispatchers.IO) { forumDao.getAll() }
            if (cached.isEmpty()) refreshForums() else {
                forums = cached
                isLoading = false
            }
        }
    }

    fun refreshForums() {
        if (isSyncing) return
        isSyncing = true
        errorMessage = null
        scope.launch {
            try {
                syncManager.syncForums()
                forums = withContext(Dispatchers.IO) { forumDao.getAll() }
            } catch (e: Exception) {
                errorMessage = "Ошибка обновления: ${e.message}"
            } finally {
                isSyncing = false
                isLoading = false
            }
        }
    }

    fun startLogin() {
        scope.launch {
            val newState = AuthManager.performLogin(tokenStorage)
            withContext(Dispatchers.Main) {
                if (newState != null) {
                    authState = newState
                } else {
                    errorMessage = "Не удалось войти в систему"
                }
            }
        }
    }

    fun logout() {
        tokenStorage.logout()
        authState = AuthState(null)
    }
}

/**
 * Helper-функция для создания и удержания стейта в Compose
 */
@Composable
fun rememberMainState(db: DatabaseManager, scope: CoroutineScope = rememberCoroutineScope()) =
    remember(db, scope) { MainState(db, scope).apply { init() } }