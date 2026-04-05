package org.rsdn.jana.data.dao

import org.rsdn.jana.data.DatabaseManager
import org.rsdn.jana.ui.screens.Forum

class ForumDao(private val db: DatabaseManager) {

    fun getAll(): List<Forum> {
        val result = mutableListOf<Forum>()
        val conn = db.getConnection()
        val stmt = conn.prepareStatement("SELECT id, title, description, threads_count FROM forums ORDER BY title")
        val rs = stmt.executeQuery()

        while (rs.next()) {
            result.add(
                Forum(
                    id = rs.getInt("id"),
                    title = rs.getString("title"),
                    description = rs.getString("description") ?: "",
                    threadsCount = rs.getInt("threads_count")
                )
            )
        }
        rs.close()
        stmt.close()
        return result
    }

    fun insertAll(forums: List<Forum>) {
        val conn = db.getConnection()
        val stmt = conn.prepareStatement(
            "INSERT OR REPLACE INTO forums (id, title, description, threads_count, sync_at) VALUES (?, ?, ?, ?, ?)"
        )

        val now = System.currentTimeMillis()
        forums.forEach { forum ->
            stmt.setInt(1, forum.id)
            stmt.setString(2, forum.title)
            stmt.setString(3, forum.description)
            stmt.setInt(4, forum.threadsCount)
            stmt.setLong(5, now)
            stmt.executeUpdate()
        }
        stmt.close()
    }

    fun clear() {
        val conn = db.getConnection()
        val stmt = conn.prepareStatement("DELETE FROM forums")
        stmt.executeUpdate()
        stmt.close()
    }

    fun getCount(): Int {
        val conn = db.getConnection()
        val stmt = conn.prepareStatement("SELECT COUNT(*) FROM forums")
        val rs = stmt.executeQuery()
        val count = rs.getInt(1)
        rs.close()
        stmt.close()
        return count
    }
}