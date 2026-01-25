package com.klausner.repositories.professional

import com.klausner.database.columns.Slot
import com.klausner.database.tables.ProfessionalTable
import com.klausner.domains.Professional
import com.klausner.infraestructure.objectMapper
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ProfessionalRepository(
    private val database: Database,
) : IProfessionalRepository {
    override fun create(obj: Professional): Result<Professional> =
        runCatching {
            transaction(database) {
                val result =
                    ProfessionalTable
                        .insert {
                            it[id] = obj.id.toString()
                            it[storeId] = obj.storeId
                            it[professionalName] = obj.name
                            it[bio] = obj.bio
                            it[email] = obj.email
                            it[password] = obj.password
                            it[houseNumber] = obj.address?.number
                            it[complement] = obj.address?.complement
                            it[neighborhood] = obj.address?.neighborhood
                            it[city] = obj.address?.city
                            it[state] = obj.address?.state
                            it[country] = obj.address?.country
                            it[zipCode] = obj.address?.zipCode
                            it[phone] = obj.phone?.number
                            it[instagram] = obj.instagram
                            it[facebook] = obj.facebook
                            it[photo] = obj.photo
                            it[workHours] =
                                objectMapper.writeValueAsString(
                                    obj.workHours?.map { interval -> Slot.fromDomain(interval) },
                                )
                        }.resultedValues

                val row = result?.get(0) ?: error("Error creating professional")
                ProfessionalTable.toDomain(row)
            }
        }

    override fun delete(id: UUID): Result<Unit> =
        runCatching {
            transaction(database) {
                val result = ProfessionalTable.deleteWhere { ProfessionalTable.id eq id.toString() }
                if (result == 0) error(PROFESSIONAL_NOT_FOUND)
            }
        }

    override fun find(id: UUID): Result<Professional> =
        runCatching {
            transaction(database) {
                ProfessionalTable
                    .select(ProfessionalTable.columns)
                    .where { ProfessionalTable.id eq id.toString() }
                    .map { row -> ProfessionalTable.toDomain(row) }
                    .singleOrNull()
            } ?: error(PROFESSIONAL_NOT_FOUND)
        }

    override fun update(obj: Professional): Result<Professional> {
        return runCatching {
            transaction(database) {
                ProfessionalTable.update({ ProfessionalTable.id eq obj.id.toString() }) {
                    it[storeId] = obj.storeId
                    it[professionalName] = obj.name
                    it[bio] = obj.bio
                    it[email] = obj.email
                    it[password] = obj.password
                    it[houseNumber] = obj.address?.number
                    it[complement] = obj.address?.complement
                    it[neighborhood] = obj.address?.neighborhood
                    it[city] = obj.address?.city
                    it[state] = obj.address?.state
                    it[country] = obj.address?.country
                    it[zipCode] = obj.address?.zipCode
                    it[phone] = obj.phone?.number
                    it[instagram] = obj.instagram
                    it[facebook] = obj.facebook
                    it[photo] = obj.photo
                    it[workHours] = obj.workHours.toString()
                    it[slots] = obj.slots.toString()
                }

                return@transaction ProfessionalTable
                    .select(ProfessionalTable.columns)
                    .where { ProfessionalTable.id eq obj.id.toString() }
                    .map { row -> ProfessionalTable.toDomain(row) }
                    .singleOrNull()
            } ?: error("Professional not found")
        }
    }

    override fun findAll(): Result<List<Professional>> =
        runCatching {
            transaction(database) {
                ProfessionalTable
                    .select(ProfessionalTable.columns)
                    .map { row -> ProfessionalTable.toDomain(row) }
            }
        }

    private companion object {
        const val PROFESSIONAL_NOT_FOUND = "Professional not found"
    }
}
