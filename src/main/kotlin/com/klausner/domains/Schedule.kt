package com.klausner.domains

import com.klausner.domains.valueobjects.Interval
import java.util.UUID

data class Schedule(
    val id: UUID,
    val professionalId: UUID,
    val customerId: UUID,
    val observation: String,
    val interval: Interval,
    val type: Type = Type.AVAILABLE,
    val status: Status = Status.CREATED
) : AggregateRoot {

    enum class Type { BLOCK, AVAILABLE }
    enum class Status { CREATED, CONCLUDED, CANCELLED }
}
