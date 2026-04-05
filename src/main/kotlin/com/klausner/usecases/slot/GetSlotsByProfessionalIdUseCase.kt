package com.klausner.usecases.slot

import com.klausner.domains.Slot
import com.klausner.repositories.slot.ISlotRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.slot.GetSlotsByProfessionalIdUseCase.Input
import com.klausner.usecases.slot.GetSlotsByProfessionalIdUseCase.Output
import java.time.LocalDateTime
import java.util.UUID

class GetSlotsByProfessionalIdUseCase(
    private val repository: ISlotRepository,
) : UseCase<Input, List<Output>> {

    override fun execute(input: Input): Result<List<Output>> =
        repository
            .findByProfessionalId(input.professionalId)
            .map { slots -> slots.map { domainToOutput(it) } }

    private fun domainToOutput(slot: Slot) =
        Output(
            id = slot.id,
            professionalId = slot.professionalId,
            serviceId = slot.serviceId,
            startTime = slot.startTime,
            endTime = slot.endTime,
            status = slot.status.name,
            customerName = slot.customerName,
            customerPhone = slot.customerPhone,
        )

    data class Input(val professionalId: UUID)

    data class Output(
        val id: UUID,
        val professionalId: UUID,
        val serviceId: UUID?,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val status: String,
        val customerName: String?,
        val customerPhone: String?,
    )
}
