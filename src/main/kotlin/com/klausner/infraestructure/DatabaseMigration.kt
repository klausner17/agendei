package com.klausner.infraestructure

import org.flywaydb.core.Flyway

object DatabaseMigration {
    fun runMigrations(jdbcUrl: String = DatabaseConfig.jdbcUrl) {
        Flyway
            .configure()
            .dataSource(jdbcUrl, null, null)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}
