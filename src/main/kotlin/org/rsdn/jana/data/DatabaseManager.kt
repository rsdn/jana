package org.rsdn.jana.data

import io.github.oshai.kotlinlogging.KotlinLogging
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

private val logger = KotlinLogging.logger {}

class DatabaseManager(private val dbPath: String = "jana.db") {
    private var connection: Connection? = null
    lateinit var dsl: DSLContext
        private set

    init {
        logger.info { "[DB] Instance created: ${this.hashCode()}" }
    }

    fun connect() {
        val url = "jdbc:sqlite:$dbPath"
        logger.info { "Connecting to database: ${File(dbPath).absolutePath}" }

        connection = DriverManager.getConnection(url)
        logger.info { "Connection established" }

        val flyway = Flyway.configure()
            .dataSource("jdbc:sqlite:jana.db", null, null)
            .locations("classpath:db/migration")
            .load()

        val migrationResult = flyway.migrate()
        logger.info { "Applied ${migrationResult.migrationsExecuted} migrations" }

        // Инициализация jOOQ DSL
        dsl = DSL.using(connection, SQLDialect.SQLITE)
        logger.info { "jOOQ DSL initialized" }
    }

    fun getConnection(): Connection = connection ?: throw IllegalStateException("Database not connected")

    fun close() {
        logger.info { "Closing database connection" }
        connection?.close()
    }
}