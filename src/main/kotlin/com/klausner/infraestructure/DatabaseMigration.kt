package com.klausner.infraestructure

import com.klausner.database.tables.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseMigration {
    fun runMigrations(database: Database) {
        transaction(database) {
            SchemaUtils.create(UserTable)
        }
    }
}

