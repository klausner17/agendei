package com.klausner.repositories.user

import com.klausner.database.tables.UserTable
import com.klausner.domains.User
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepository(
    private val database: Database,
) : IUserRepository {
    override fun create(obj: User): Result<User> =
        runCatching {
            transaction(database) {
                UserTable.insert {
                    it[id] = obj.id
                    it[email] = obj.email
                    it[name] = obj.name
                    it[picture] = obj.picture
                    it[provider] = obj.provider
                    it[googleId] = obj.googleId
                    it[emailVerified] = obj.emailVerified
                }

                UserTable
                    .selectAll()
                    .where { UserTable.id eq obj.id }
                    .map { row -> UserTable.toDomain(row) }
                    .singleOrNull()!!
            }
        }

    override fun findByEmail(email: String): Result<User?> =
        runCatching {
            transaction(database) {
                UserTable
                    .selectAll()
                    .where { UserTable.email eq email }
                    .map { row -> UserTable.toDomain(row) }
                    .singleOrNull()
            }
        }

    override fun findByGoogleId(googleId: String): Result<User?> =
        runCatching {
            transaction(database) {
                UserTable
                    .selectAll()
                    .where { UserTable.googleId eq googleId }
                    .map { row -> UserTable.toDomain(row) }
                    .singleOrNull()
            }
        }

    override fun delete(id: UUID): Result<Unit> =
        runCatching {
            transaction(database) {
                UserTable
                    .select(UserTable.columns)
                    .where { UserTable.id eq id }
            }
        }

    override fun find(id: UUID): Result<User> =
        runCatching {
            transaction(database) {
                UserTable
                    .selectAll()
                    .where { UserTable.id eq id }
                    .map { row -> UserTable.toDomain(row) }
                    .singleOrNull()
            }!!
        }

    override fun update(obj: User): Result<User> =
        runCatching {
            transaction(database) {
                UserTable
                    .selectAll()
                    .where { UserTable.id eq obj.id }
                    .map { row -> UserTable.toDomain(row) }
                    .singleOrNull()!!
            }
        }
}
