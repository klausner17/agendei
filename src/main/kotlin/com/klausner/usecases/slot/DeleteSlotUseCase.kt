package com.klausner.usecases.slot

import com.klausner.domains.Professional
import com.klausner.domains.Slot
import com.klausner.infraestructure.flatMap
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.slot.ISlotRepository
import com.klausner.usecases.UseCase
import java.util.UUID

class DeleteSlotUseCase(
    private val slotRepository: ISlotRepository,
    private val professionalRepository: IProfessionalRepository,
) : UseCase<DeleteSlotUseCase.Input, Unit> {
    override fun execute(input: Input): Result<Unit> =
        slotRepository.find(input.slotId)
            .flatMap { slot -> checkOwnershipViaSlot(slot, input.requesterId) }
            .flatMap { slotRepository.delete(input.slotId) }

    private fun checkOwnershipViaSlot(
        slot: Slot,
        requesterId: UUID,
    ): Result<Unit> =
        professionalRepository.find(slot.professionalId)
            .flatMap { professional -> checkOwnership(professional, requesterId) }
            .map { }

    private fun checkOwnership(
        professional: Professional,
        requesterId: UUID,
    ): Result<Professional> =
        if (professional.userId == requesterId) {
            Result.success(professional)
        } else {
            Result.failure(SecurityException("Access denied"))
        }

    data class Input(
        val slotId: UUID,
        val requesterId: UUID,
    )
}
