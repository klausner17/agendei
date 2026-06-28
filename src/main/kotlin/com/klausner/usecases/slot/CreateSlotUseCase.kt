package com.klausner.usecases.slot

import com.klausner.domains.Professional
import com.klausner.domains.Slot
import com.klausner.infraestructure.flatMap
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.slot.ISlotRepository
import com.klausner.usecases.UseCase
import java.time.LocalDateTime
import java.util.UUID

class CreateSlotUseCase(
    private val slotRepository: ISlotRepository,
    private val professionalRepository: IProfessionalRepository,
) : UseCase<CreateSlotUseCase.Input, List<CreateSlotUseCase.Output>> {
    override fun execute(input: Input): Result<List<Output>> =
        professionalRepository.find(input.professionalId)
            .flatMap { professional -> checkOwnership(professional, input.requesterId) }
            .flatMap { createSlots(input) }

    private fun checkOwnership(
        professional: Professional,
        requesterId: UUID,
    ): Result<Professional> =
        if (professional.userId == requesterId) {
            Result.success(professional)
        } else {
            Result.failure(SecurityException("Access denied"))
        }

    private fun createSlots(input: Input): Result<List<Output>> {
        val weeks = input.recurrenceWeeks ?: 1
        val slots =
            (0 until weeks).map { week ->
                Slot(
                    professionalId = input.professionalId,
                    serviceId = input.serviceId,
                    startTime = input.startTime.plusWeeks(week.toLong()),
                    endTime = input.endTime.plusWeeks(week.toLong()),
                )
            }

        return if (slots.size == 1) {
            slotRepository.create(slots.first()).map { listOf(domainToOutput(it)) }
        } else {
            slotRepository.createAll(slots).map { it.map(::domainToOutput) }
        }
    }

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

    data class Input(
        val professionalId: UUID,
        val requesterId: UUID,
        val serviceId: UUID? = null,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val recurrenceWeeks: Int? = null,
    )

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
