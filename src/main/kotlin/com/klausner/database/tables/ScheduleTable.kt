package com.klausner.database.tables

import com.klausner.domains.Schedule
import com.klausner.domains.valueobjects.Interval
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

object ScheduleTable : Table("schedules") {
    val id = uuid("id")
    val professionalId = uuid("professional_id")
    val customerId = uuid("customer_id")
    val observation = varchar("observation", 500)
    val startTime = varchar("start_time", 30)
    val endTime = varchar("end_time", 30)
    val type = varchar("type", 20)
    val status = varchar("status", 20)

    override val primaryKey = PrimaryKey(id)

    fun toDomain(row: ResultRow) =
        Schedule(
            id = row[id],
            professionalId = row[professionalId],
            customerId = row[customerId],
            observation = row[observation],
            interval =
                Interval(
                    startTime = LocalDateTime.parse(row[startTime]),
                    endTime = LocalDateTime.parse(row[endTime]),
                ),
            type = Schedule.Type.valueOf(row[type]),
            status = Schedule.Status.valueOf(row[status]),
        )
}
