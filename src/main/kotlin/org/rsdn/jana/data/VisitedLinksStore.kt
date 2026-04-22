package org.rsdn.jana.data

import androidx.compose.runtime.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.prefs.Preferences

/**
 * Персистентное хранилище посещённых ссылок.
 * Использует Java Preferences API для сохранения между запусками
 */
class VisitedLinksStore {
    private val prefs = Preferences.userNodeForPackage(VisitedLinksStore::class.java)
    private val VISITED_LINKS_KEY = "visited_links"
    
    private val _visitedUrls = MutableStateFlow<Set<String>>(loadVisitedUrls())
    val visitedUrls: StateFlow<Set<String>> = _visitedUrls.asStateFlow()
    
    /**
     * Загрузить посещённые ссылки из хранилища
     */
    private fun loadVisitedUrls(): Set<String> {
        val saved = prefs.get(VISITED_LINKS_KEY, "") ?: return emptySet()
        if (saved.isBlank()) return emptySet()
        return saved.split(LINK_SEPARATOR).toSet()
    }
    
    /**
     * Сохранить посещённые ссылки в хранилище
     */
    private fun saveVisitedUrls(urls: Set<String>) {
        prefs.put(VISITED_LINKS_KEY, urls.joinToString(LINK_SEPARATOR))
        prefs.flush()
    }
    
    /**
     * Проверить, посещена ли ссылка
     */
    fun isVisited(url: String): Boolean = url in _visitedUrls.value
    
    /**
     * Отметить ссылку как посещённую
     */
    fun markVisited(url: String) {
        val current = _visitedUrls.value
        if (url !in current) {
            val updated = current + url
            _visitedUrls.value = updated
            saveVisitedUrls(updated)
        }
    }
    
    /**
     * Получить все посещённые ссылки
     */
    fun getAllVisited(): Set<String> = _visitedUrls.value
    
    /**
     * Очистить все посещённые ссылки
     */
    fun clear() {
        _visitedUrls.value = emptySet()
        prefs.remove(VISITED_LINKS_KEY)
        prefs.flush()
    }
    
    companion object {
        private const val LINK_SEPARATOR = "\u0001" // Непечатаемый символ-разделитель
        
        /**
         * Singleton instance
         */
        @Volatile
        private var instance: VisitedLinksStore? = null
        
        fun getInstance(): VisitedLinksStore {
            return instance ?: synchronized(this) {
                instance ?: VisitedLinksStore().also { instance = it }
            }
        }
    }
}

/**
 * Composable для получения текущего состояния посещённых ссылок
 */
@Composable
fun rememberVisitedLinks(): VisitedLinksStore {
    return remember { VisitedLinksStore.getInstance() }
}

/**
 * Composable для получения множества посещённых URL как State
 */
@Composable
fun rememberVisitedUrls(): Set<String> {
    val store = rememberVisitedLinks()
    val visitedUrls by store.visitedUrls.collectAsState()
    return visitedUrls
}