package com.klausner.repositories.slot

import com.klausner.database.tables.SlotTable
import com.klausner.domains.Slot
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class SlotRepository(
    private val database: Database,
) : ISlotRepository {

    override fun create(obj: Slot): Result<Slot> =
        runCatching {
            transaction(database) {
                SlotTable.insert {
                    it[id] = obj.id
                    it[professionalId] = obj.professionalId
                    it[serviceId] = obj.serviceId
                    it[startTime] = obj.startTime.toString()
                    it[endTime] = obj.endTime.toString()
                    it[status] = obj.status.name
                    it[customerName] = obj.customerName
                    it[customerPhone] = obj.customerPhone
                }
                    .resultedValues
                    ?.firstOrNull()
                    ?.let { SlotTable.toDomain(it) }
                    ?: error("Error creating slot")
            }
        }

    override fun createAll(slots: List<Slot>): Result<List<Slot>> =
        runCatching {
            transaction(database) {
                SlotTable.batchInsert(slots) { slot ->
                    this[SlotTable.id] = slot.id
                    this[SlotTable.professionalId] = slot.professionalId
                    this[SlotTable.serviceId] = slot.serviceId
                    this[SlotTable.startTime] = slot.startTime.toString()
                    this[SlotTable.endTime] = slot.endTime.toString()
                    this[SlotTable.status] = slot.status.name
                    this[SlotTable.customerName] = slot.customerName
                    this[SlotTable.customerPhone] = slot.customerPhone
                }.map { SlotTable.toDomain(it) }
            }
        }

    override fun find(id: UUID): Result<Slot> =
        runCatching {
            transaction(database) {
                SlotTable
                    .select(SlotTable.columns)
                    .where { SlotTable.id eq id }
                    .map { SlotTable.toDomain(it) }
                    .singleOrNull()
            } ?: error(SLOT_NOT_FOUND)
        }

    override fun update(obj: Slot): Result<Slot> =
        runCatching {
            transaction(database) {
                SlotTable.update({ SlotTable.id eq obj.id }) {
                    it[serviceId] = obj.serviceId
                    it[startTime] = obj.startTime.toString()
                    it[endTime] = obj.endTime.toString()
                    it[status] = obj.status.name
                    it[customerName] = obj.customerName
                    it[customerPhone] = obj.customerPhone
                }
                SlotTable
                    .select(SlotTable.columns)
                    .where { SlotTable.id eq obj.id }
                    .map { SlotTable.toDomain(it) }
                    .singleOrNull()
            } ?: error(SLOT_NOT_FOUND)
        }

    override fun delete(id: UUID): Result<Unit> =
        runCatching {
            transaction(database) {
                val result = SlotTable.deleteWhere { SlotTable.id eq id }
                if (result == 0) error(SLOT_NOT_FOUND)
            }
        }

    override fun findByProfessionalId(professionalId: UUID): Result<List<Slot>> =
        runCatching {
            transaction(database) {
                SlotTable
                    .select(SlotTable.columns)
                    .where { SlotTable.professionalId eq professionalId }
                    .map { SlotTable.toDomain(it) }
            }
        }

    private companion object {
        const val SLOT_NOT_FOUND = "Slot not found"
    }
}
