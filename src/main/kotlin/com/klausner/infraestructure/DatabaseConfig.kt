package com.klausner.infraestructure

object DatabaseConfig {
    val jdbcUrl: String = System.getenv("DATABASE_URL") ?: "jdbc:sqlite:./data.db"
    const val DRIVER = "org.sqlite.JDBC"
}
