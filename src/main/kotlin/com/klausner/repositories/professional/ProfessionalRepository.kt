package com.klausner.repositories.professional

import com.klausner.database.columns.Slot
import com.klausner.database.tables.ProfessionalTable
import com.klausner.domains.Professional
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ProfessionalRepository(
    private val database: Database = Database.connect(
        "jdbc:postgresql://localhost:5432/agendei",
        driver = "org.postgresql.Driver",
        user = "klausner.pinto",
        password = "",
    )
) : IProfessionalRepository {

    override fun create(obj: Professional): Result<Professional> {
        return runCatching {
            transaction(database) {
                ProfessionalTable.insert {
                    it[id] = obj.id
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
                    it[workHours] = Json.encodeToString(obj.workHours?.map { interval -> Slot.fromDomain(interval) })
                }

                return@transaction ProfessionalTable.select(ProfessionalTable.columns)
                    .where { ProfessionalTable.id eq obj.id }
                    .map { row -> ProfessionalTable.toDomain(row) }
                    .singleOrNull()
            }!!
        }
    }

    override fun delete(id: UUID): Result<Unit> {
        return runCatching {
            transaction(database) {
                val result = ProfessionalTable.deleteWhere { ProfessionalTable.id eq id }
                if (result == 0) error(PROFESSIONAL_NOT_FOUND)
            }
        }
    }

    override fun find(id: UUID): Result<Professional> {
        return runCatching {
            transaction(database) {
                ProfessionalTable.select(ProfessionalTable.columns)
                    .where { ProfessionalTable.id eq id }
                    .map { row -> ProfessionalTable.toDomain(row) }
                    .singleOrNull()
            } ?: error(PROFESSIONAL_NOT_FOUND)
        }
    }

    override fun update(obj: Professional): Result<Professional> {
        return runCatching {
            transaction(database) {
                ProfessionalTable.update({ ProfessionalTable.id eq obj.id }) {
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
                }

                return@transaction ProfessionalTable.select(ProfessionalTable.columns)
                    .where { ProfessionalTable.id eq obj.id }
                    .map { row -> ProfessionalTable.toDomain(row) }
                    .singleOrNull()
            } ?: error("Professional not found")
        }
    }

    private companion object {
        const val PROFESSIONAL_NOT_FOUND = "Professional not found"
    }
}
