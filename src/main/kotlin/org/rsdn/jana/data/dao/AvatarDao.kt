package org.rsdn.jana.data.dao

import org.jooq.impl.DSL
import org.jooq.impl.DSL.field
import org.rsdn.jana.data.DatabaseManager
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URI

class AvatarDao(private val db: DatabaseManager) {

    /**
     * Получить аватар из кеша
     * @return ByteArray с изображением или null если нет в кеше
     */
    fun getAvatar(gravatarHash: String): ByteArray? {
        val t = DSL.table("avatars")
        return db.dsl.select(field("image_blob"))
            .from(t)
            .where(field("gravatar_hash").eq(gravatarHash))
            .fetchOne { r -> r.get("image_blob", ByteArray::class.java) }
    }

    /**
     * Сохранить аватар в кеш
     */
    fun saveAvatar(gravatarHash: String, imageBlob: ByteArray) {
        val t = DSL.table("avatars")
        val cachedAt = System.currentTimeMillis() / 1000

        db.dsl.insertInto(t)
            .set(field("gravatar_hash"), gravatarHash)
            .set(field("image_blob"), imageBlob)
            .set(field("cached_at"), cachedAt)
            .onConflict(field("gravatar_hash"))
            .doUpdate()
            .set(field("image_blob"), imageBlob)
            .set(field("cached_at"), cachedAt)
            .execute()
    }

    /**
     * Проверить наличие аватара в кеше
     */
    fun hasAvatar(gravatarHash: String): Boolean {
        val t = DSL.table("avatars")
        return (db.dsl.selectCount()
          .from(t)
          .where(field("gravatar_hash").eq(gravatarHash))
          .fetchOne { r -> r.get(0, Int::class.java) } ?: 0) > 0
    }

    /**
     * Загрузить аватар с gravatar.com и сохранить в кеш
     * @param size Размер изображения в пикселях
     * @return ByteArray с изображением или null при ошибке
     */
    fun downloadAndCacheAvatar(gravatarHash: String, size: Int = 32): ByteArray? {
        val url = URI.create("https://www.gravatar.com/avatar/$gravatarHash?s=$size&d=identicon").toURL()
        
        return try {
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "Jana RSDN Client")
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val outputStream = ByteArrayOutputStream()
                connection.inputStream.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
                val imageBlob = outputStream.toByteArray()
                
                // Сохраняем в кеш
                saveAvatar(gravatarHash, imageBlob)
                
                imageBlob
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Получить аватар из кеша или загрузить с сервера
     */
    fun getOrDownloadAvatar(gravatarHash: String, size: Int = 32): ByteArray? {
        // Сначала проверяем кеш
        getAvatar(gravatarHash)?.let { return it }
        
        // Если нет в кеше, загружаем
        return downloadAndCacheAvatar(gravatarHash, size)
    }
}