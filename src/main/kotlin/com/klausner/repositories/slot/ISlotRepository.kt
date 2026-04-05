package com.klausner.repositories.slot

import com.klausner.domains.Slot
import com.klausner.repositories.interfaces.BasicCrud
import java.util.UUID

interface ISlotRepository : BasicCrud<Slot> {

    fun createAll(slots: List<Slot>): Result<List<Slot>>

    fun findByProfessionalId(professionalId: UUID): Result<List<Slot>>
}
