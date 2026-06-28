package com.klausner.repositories.schedule

import com.klausner.database.tables.ScheduleTable
import com.klausner.domains.Schedule
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ScheduleRepository(
    private val database: Database,
) : IScheduleRepository {
    override fun create(obj: Schedule): Result<Schedule> =
        runCatching {
            transaction(database) {
                ScheduleTable.insert {
                    it[id] = obj.id
                    it[professionalId] = obj.professionalId
                    it[customerId] = obj.customerId
                    it[observation] = obj.observation
                    it[startTime] = obj.interval.startTime.toString()
                    it[endTime] = obj.interval.endTime.toString()
                    it[type] = obj.type.name
                    it[status] = obj.status.name
                }
                    .resultedValues
                    ?.firstOrNull()
                    ?.let { ScheduleTable.toDomain(it) }
                    ?: error(SCHEDULE_NOT_FOUND)
            }
        }

    override fun find(id: UUID): Result<Schedule> =
        runCatching {
            transaction(database) {
                ScheduleTable
                    .select(ScheduleTable.columns)
                    .where { ScheduleTable.id eq id }
                    .map { ScheduleTable.toDomain(it) }
                    .singleOrNull()
            } ?: error(SCHEDULE_NOT_FOUND)
        }

    override fun update(obj: Schedule): Result<Schedule> =
        runCatching {
            transaction(database) {
                ScheduleTable.update({ ScheduleTable.id eq obj.id }) {
                    it[observation] = obj.observation
                    it[startTime] = obj.interval.startTime.toString()
                    it[endTime] = obj.interval.endTime.toString()
                    it[type] = obj.type.name
                    it[status] = obj.status.name
                }
                ScheduleTable
                    .select(ScheduleTable.columns)
                    .where { ScheduleTable.id eq obj.id }
                    .map { ScheduleTable.toDomain(it) }
                    .singleOrNull()
            } ?: error(SCHEDULE_NOT_FOUND)
        }

    override fun delete(id: UUID): Result<Unit> =
        runCatching {
            transaction(database) {
                val result = ScheduleTable.deleteWhere { ScheduleTable.id eq id }
                if (result == 0) error(SCHEDULE_NOT_FOUND)
            }
        }

    override fun findByCustomerId(customerId: UUID): Result<List<Schedule>> =
        runCatching {
            transaction(database) {
                ScheduleTable
                    .select(ScheduleTable.columns)
                    .where { ScheduleTable.customerId eq customerId }
                    .map { ScheduleTable.toDomain(it) }
            }
        }

    override fun findByProfessionalId(professionalId: UUID): Result<List<Schedule>> =
        runCatching {
            transaction(database) {
                ScheduleTable
                    .select(ScheduleTable.columns)
                    .where { ScheduleTable.professionalId eq professionalId }
                    .map { ScheduleTable.toDomain(it) }
            }
        }

    private companion object {
        const val SCHEDULE_NOT_FOUND = "Schedule not found"
    }
}
