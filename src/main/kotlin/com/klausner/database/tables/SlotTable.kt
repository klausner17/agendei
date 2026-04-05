package com.klausner.database.tables

import com.klausner.domains.Slot
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import java.time.LocalDateTime

object SlotTable : Table("slots") {
    val id = uuid("id")
    val professionalId = uuid("professional_id")
    val serviceId = uuid("service_id").nullable()
    val startTime = varchar("start_time", 30)
    val endTime = varchar("end_time", 30)
    val status = varchar("status", 20)
    val customerName = varchar("customer_name", 200).nullable()
    val customerPhone = varchar("customer_phone", 30).nullable()

    override val primaryKey = PrimaryKey(id)

    fun toDomain(row: ResultRow) =
        Slot(
            id = row[id],
            professionalId = row[professionalId],
            serviceId = row[serviceId],
            startTime = LocalDateTime.parse(row[startTime]),
            endTime = LocalDateTime.parse(row[endTime]),
            status = Slot.Status.valueOf(row[status]),
            customerName = row[customerName],
            customerPhone = row[customerPhone],
        )
}
