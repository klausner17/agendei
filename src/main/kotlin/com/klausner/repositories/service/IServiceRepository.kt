package com.klausner.repositories.service

import com.klausner.domains.Service
import com.klausner.repositories.interfaces.BasicCrud
import java.util.UUID

interface IServiceRepository : BasicCrud<Service> {
    fun findByProfessionalId(professionalId: UUID): Result<List<Service>>
}
