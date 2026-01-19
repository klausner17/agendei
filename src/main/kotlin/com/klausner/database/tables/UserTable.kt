package com.klausner.database.tables

import com.klausner.domains.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object UserTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255)
    val name = varchar("name", 255)
    val picture = varchar("picture", 500).nullable()
    val provider = varchar("provider", 50)
    val googleId = varchar("google_id", 255)
    val emailVerified = bool("email_verified")

    override val primaryKey = PrimaryKey(id)

    fun toDomain(row: ResultRow): User {
        return User(
            id = row[id],
            email = row[email],
            name = row[name],
            picture = row[picture],
            provider = row[provider],
            googleId = row[googleId],
            emailVerified = row[emailVerified]
        )
    }
}

