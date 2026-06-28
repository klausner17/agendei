package com.klausner.repositories.schedule

import com.klausner.domains.Schedule
import com.klausner.repositories.interfaces.BasicCrud
import java.util.UUID

interface IScheduleRepository : BasicCrud<Schedule> {
    fun findByCustomerId(customerId: UUID): Result<List<Schedule>>

    fun findByProfessionalId(professionalId: UUID): Result<List<Schedule>>
}
