package org.rsdn.jana.data

import androidx.compose.runtime.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.util.prefs.Preferences

/**
 * Персистентное хранилище настроек главного окна.
 * Использует Java Preferences API для сохранения между запусками.
 */
class WindowSettingsStore {
    private val prefs = Preferences.userNodeForPackage(WindowSettingsStore::class.java)
    
    companion object {
        private const val KEY_X = "window_x"
        private const val KEY_Y = "window_y"
        private const val KEY_WIDTH = "window_width"
        private const val KEY_HEIGHT = "window_height"
        private const val KEY_PLACEMENT = "window_placement"
        
        private const val DEFAULT_WIDTH = 1024
        private const val DEFAULT_HEIGHT = 768
        
        @Volatile
        private var instance: WindowSettingsStore? = null
        
        fun getInstance(): WindowSettingsStore {
            return instance ?: synchronized(this) {
                instance ?: WindowSettingsStore().also { instance = it }
            }
        }
    }
    
    /**
     * Получить сохранённую позицию окна без создания WindowState.
     * Возвращает Pair<x, y> или null если позиция не сохранена или экран недоступен.
     */
    fun getSavedPosition(): Pair<Int, Int>? {
        val x = prefs.getInt(KEY_X, -1)
        val y = prefs.getInt(KEY_Y, -1)
        val width = prefs.getInt(KEY_WIDTH, DEFAULT_WIDTH)
        val height = prefs.getInt(KEY_HEIGHT, DEFAULT_HEIGHT)
        
        if (x < 0 || y < 0 || !isPositionOnAvailableScreen(x, y, width, height)) {
            return null
        }
        
        return Pair(x, y)
    }
    
    /**
     * Загрузить сохранённые настройки окна.
     * Проверяет доступность экрана и валидирует позицию.
     */
    fun loadWindowState(): WindowState {
        val x = prefs.getInt(KEY_X, -1)
        val y = prefs.getInt(KEY_Y, -1)
        val width = prefs.getInt(KEY_WIDTH, DEFAULT_WIDTH)
        val height = prefs.getInt(KEY_HEIGHT, DEFAULT_HEIGHT)
        val placementName = prefs.get(KEY_PLACEMENT, WindowPlacement.Floating.name)
        
        val placement = try {
            WindowPlacement.valueOf(placementName)
        } catch (_: Exception) {
            WindowPlacement.Floating
        }
        
        // Если позиция не сохранена или невалидна — центрируем окно
        if (x < 0 || y < 0 || !isPositionOnAvailableScreen(x, y, width, height)) {
            return createCenteredState(width, height, placement)
        }
        
        return WindowState(
            placement = placement,
            position = WindowPosition(x.dp, y.dp),
            size = DpSize(width.dp, height.dp)
        )
    }
    
    /**
     * Сохранить текущее состояние окна.
     */
    fun saveWindowState(windowState: WindowState) {
        when (windowState.placement) {
            WindowPlacement.Maximized -> {
                // Для максимизированного окна сохраняем только состояние
                prefs.put(KEY_PLACEMENT, WindowPlacement.Maximized.name)
            }
            WindowPlacement.Fullscreen -> {
                prefs.put(KEY_PLACEMENT, WindowPlacement.Fullscreen.name)
            }
            WindowPlacement.Floating -> {
                val x = windowState.position.x.value.toInt()
                val y = windowState.position.y.value.toInt()
                val width = windowState.size.width.value.toInt()
                val height = windowState.size.height.value.toInt()
                
                prefs.putInt(KEY_X, x)
                prefs.putInt(KEY_Y, y)
                prefs.putInt(KEY_WIDTH, width)
                prefs.putInt(KEY_HEIGHT, height)
                prefs.put(KEY_PLACEMENT, WindowPlacement.Floating.name)
            }
        }
        prefs.flush()
    }
    
    /**
     * Проверить, находится ли позиция на доступном экране.
     */
    private fun isPositionOnAvailableScreen(x: Int, y: Int, width: Int, height: Int): Boolean {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screenDevices = ge.screenDevices
        
        // Область окна (с небольшим запасом для заголовка)
        val windowBounds = Rectangle(x, y, width, 50)
        
        for (screen in screenDevices) {
            val config = screen.defaultConfiguration
            val screenBounds = config.bounds
            
            // Проверяем пересечение верхней части окна с экраном
            if (screenBounds.intersects(windowBounds)) {
                return true
            }
        }
        
        return false
    }
    
    /**
     * Создать состояние окна с центрированием на главном экране.
     */
    private fun createCenteredState(width: Int, height: Int, placement: WindowPlacement): WindowState {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val screenDevice = ge.defaultScreenDevice
        val screenBounds = screenDevice.defaultConfiguration.bounds
        
        // Центрируем окно на экране
        val centerX = screenBounds.x + (screenBounds.width - width) / 2
        val centerY = screenBounds.y + (screenBounds.height - height) / 2
        
        return WindowState(
            placement = placement,
            position = WindowPosition(centerX.dp, centerY.dp),
            size = DpSize(width.dp, height.dp)
        )
    }
}

/**
 * Composable для получения WindowSettingsStore.
 */
@Composable
fun rememberWindowSettingsStore(): WindowSettingsStore {
    return remember { WindowSettingsStore.getInstance() }
}