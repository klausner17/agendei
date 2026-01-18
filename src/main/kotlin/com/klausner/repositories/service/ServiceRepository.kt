package com.klausner.repositories.service

import com.klausner.database.tables.ServiceTable
import com.klausner.domains.Service
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ServiceRepository(
    private val database: Database,
) : IServiceRepository {
    override fun create(obj: Service): Result<Service> {
        return runCatching {
            transaction(database) {
                ServiceTable.insert {
                    it[id] = obj.id
                    it[companyId] = obj.companyId
                    it[professionalId] = obj.professionalId
                    it[description] = obj.description
                    it[price] = obj.price
                    it[durationInMinutes] = obj.durationInMinutes
                }

                return@transaction ServiceTable
                    .select(ServiceTable.columns)
                    .where { ServiceTable.id eq obj.id }
                    .map { row -> ServiceTable.toDomain(row) }
                    .singleOrNull()
            }!!
        }
    }

    override fun delete(id: UUID): Result<Unit> =
        runCatching {
            transaction(database) {
                ServiceTable.deleteWhere { ServiceTable.id eq id }
            }
        }

    override fun find(id: UUID): Result<Service> =
        runCatching {
            transaction(database) {
                ServiceTable
                    .select(ServiceTable.columns)
                    .where { ServiceTable.id eq id }
                    .map { row -> ServiceTable.toDomain(row) }
                    .singleOrNull()
            }!!
        }

    override fun update(obj: Service): Result<Service> {
        return runCatching {
            transaction(database) {
                ServiceTable.update({ ServiceTable.id eq obj.id }) {
                    it[professionalId] = obj.professionalId
                    it[description] = obj.description
                    it[price] = obj.price
                }

                return@transaction ServiceTable
                    .select(ServiceTable.columns)
                    .where { ServiceTable.id eq obj.id }
                    .map { row -> ServiceTable.toDomain(row) }
                    .singleOrNull()
            }!!
        }
    }
}
