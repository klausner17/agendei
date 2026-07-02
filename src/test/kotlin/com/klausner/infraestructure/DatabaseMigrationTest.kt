package com.klausner.infraestructure

import java.io.File
import java.sql.DriverManager
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DatabaseMigrationTest {
    private val databaseFile = File.createTempFile("agendei-migration-test", ".db")
    private val jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}"

    @AfterTest
    fun cleanup() {
        databaseFile.delete()
    }

    @Test
    fun `should keep professionals credentials columns`() {
        // given
        // a fresh sqlite database file

        // when
        DatabaseMigration.runMigrations(jdbcUrl)

        // then
        assertTrue(columnExists("professionals", "email"))
        assertTrue(columnExists("professionals", "password"))
    }

    @Test
    fun `should drop user link column and users table`() {
        // given
        // a fresh sqlite database file

        // when
        DatabaseMigration.runMigrations(jdbcUrl)

        // then
        assertFalse(columnExists("professionals", "user_id"))
        assertFalse(tableExists("users"))
    }

    private fun columnExists(
        table: String,
        column: String,
    ): Boolean =
        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.metaData.getColumns(null, null, table, column).use { it.next() }
        }

    private fun tableExists(table: String): Boolean =
        DriverManager.getConnection(jdbcUrl).use { connection ->
            connection.metaData.getTables(null, null, table, null).use { it.next() }
        }
}
