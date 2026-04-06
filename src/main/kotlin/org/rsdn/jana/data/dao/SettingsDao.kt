package org.rsdn.jana.data.dao

import org.jooq.impl.DSL
import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.data.jooq.tables.references.SETTINGS

enum class ThemeMode { LIGHT, DARK, SYSTEM }

class SettingsDao(private val db: DatabaseManager) {
    fun getTheme(): ThemeMode {
        val value = db.dsl.select(SETTINGS.VALUE)
            .from(SETTINGS)
            .where(SETTINGS.KEY.eq("theme"))
            .fetchOneInto(String::class.java)
        return try { ThemeMode.valueOf(value ?: "SYSTEM") } catch (_: Exception) { ThemeMode.SYSTEM }
    }

    fun setTheme(mode: ThemeMode) {
        db.dsl.insertInto(SETTINGS)
            .set(SETTINGS.KEY, "theme")
            .set(SETTINGS.VALUE, mode.name)
            .onDuplicateKeyUpdate()
            .set(SETTINGS.VALUE, mode.name)
            .execute()
    }
}