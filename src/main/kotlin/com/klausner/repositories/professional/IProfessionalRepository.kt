package com.klausner.repositories.professional

import com.klausner.domains.Professional
import com.klausner.repositories.interfaces.BasicCrud
import java.util.UUID

interface IProfessionalRepository : BasicCrud<Professional> {
    fun findAll(): Result<List<Professional>>

    fun findByUserId(userId: UUID): Result<Professional>
}
