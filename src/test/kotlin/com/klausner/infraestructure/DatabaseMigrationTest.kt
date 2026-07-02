package com.klausner.infraestructure

import java.io.File
import java.sql.DriverManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DatabaseMigrationTest {
    private val databaseFile = File.createTempFile("agendei-migration-test", ".db")
    private val jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"

    @AfterTest
    fun cleanup() {
        databaseFile.delete()
    }

    @Test
    fun `should create users table with password_hash column`() {
        // given
        // a fresh sqlite database file

        // when
        DatabaseMigration.runMigrations(jdbcUrl)

        // then
        assertTrue(columnExists("users", "password_hash"))
    }

    private fun columnExists(
        table: String,
        column: String,
    ): Boolean =
        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.metaData.getColumns(null, null, table, column).use { it.next() }
        }
}
