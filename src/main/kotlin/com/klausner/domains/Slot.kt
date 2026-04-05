package com.klausner.domains

import java.time.LocalDateTime
import java.util.UUID

data class Slot(
    val id: UUID = UUID.randomUUID(),
    val professionalId: UUID,
    val serviceId: UUID? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: Status = Status.AVAILABLE,
    val customerName: String? = null,
    val customerPhone: String? = null,
) : Domain {

    enum class Status { AVAILABLE, BOOKED, CANCELLED }
}
