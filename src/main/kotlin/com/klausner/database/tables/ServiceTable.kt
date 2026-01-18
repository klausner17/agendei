package com.klausner.database.tables

import com.klausner.domains.Service
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object ServiceTable : Table() {
    val id = uuid(name = "id")
    val professionalId = uuid(name = "professional_id")
    val description = varchar(name = "description", length = 100)
    val price = integer(name = "price")
    val durationInMinutes = integer(name = "duration_in_minutes")

    fun toDomain(row: ResultRow) =
        Service(
            id = row[id],
            professionalId = row[professionalId],
            description = row[description],
            price = row[price],
            durationInMinutes = row[durationInMinutes],
        )
}
