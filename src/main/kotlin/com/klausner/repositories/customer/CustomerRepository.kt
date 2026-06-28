package com.klausner.repositories.customer

import com.klausner.database.tables.CustomerTable
import com.klausner.domains.Customer
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class CustomerRepository(
    private val database: Database,
) : ICustomerRepository {
    override fun create(obj: Customer): Result<Customer> =
        runCatching {
            transaction(database) {
                CustomerTable.insert {
                    it[id] = obj.id
                    it[name] = obj.name
                    it[countryCode] = obj.phone.countryCode
                    it[areaCode] = obj.phone.areaCode
                    it[phoneNumber] = obj.phone.number
                    it[isWhatsApp] = obj.phone.isWhatsApp
                    it[email] = obj.email
                    it[password] = obj.password
                }
                    .resultedValues
                    ?.firstOrNull()
                    ?.let { CustomerTable.toDomain(it) }
                    ?: error(CUSTOMER_NOT_FOUND)
            }
        }

    override fun find(id: UUID): Result<Customer> =
        runCatching {
            transaction(database) {
                CustomerTable
                    .select(CustomerTable.columns)
                    .where { CustomerTable.id eq id }
                    .map { CustomerTable.toDomain(it) }
                    .singleOrNull()
            } ?: error(CUSTOMER_NOT_FOUND)
        }

    override fun update(obj: Customer): Result<Customer> =
        runCatching {
            transaction(database) {
                CustomerTable.update({ CustomerTable.id eq obj.id }) {
                    it[name] = obj.name
                    it[countryCode] = obj.phone.countryCode
                    it[areaCode] = obj.phone.areaCode
                    it[phoneNumber] = obj.phone.number
                    it[isWhatsApp] = obj.phone.isWhatsApp
                    it[email] = obj.email
                    it[password] = obj.password
                }
                CustomerTable
                    .select(CustomerTable.columns)
                    .where { CustomerTable.id eq obj.id }
                    .map { CustomerTable.toDomain(it) }
                    .singleOrNull()
            } ?: error(CUSTOMER_NOT_FOUND)
        }

    override fun delete(id: UUID): Result<Unit> =
        runCatching {
            transaction(database) {
                val result = CustomerTable.deleteWhere { CustomerTable.id eq id }
                if (result == 0) error(CUSTOMER_NOT_FOUND)
            }
        }

    private companion object {
        const val CUSTOMER_NOT_FOUND = "Customer not found"
    }
}
