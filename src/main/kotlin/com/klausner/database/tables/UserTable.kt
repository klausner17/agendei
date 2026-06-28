package com.klausner.database.tables

import com.klausner.domains.User
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object UserTable : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 255)
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)

    fun toDomain(row: ResultRow): User {
        return User(
            id = row[id],
            email = row[email],
            name = row[name],
            passwordHash = row[passwordHash],
        )
    }
}
