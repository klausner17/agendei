package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.domains.valueobjects.Interval
import com.klausner.infraestructure.flatMap
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.professional.UpdateProfessionalSlotUseCase.Input
import com.klausner.usecases.professional.UpdateProfessionalSlotUseCase.Output
import java.util.UUID

class UpdateProfessionalSlotUseCase(
    private val professionalRepository: IProfessionalRepository,
) : UseCase<Input, Output> {

    override fun execute(input: Input): Result<Output> {
        return professionalRepository.find(input.professionalId)
            .map { insertSlots(it, input.slot) }
            .flatMap { professionalRepository.update(it) }
            .map { Output(it) }
    }

    private fun insertSlots(professional: Professional, slots: List<Interval>) = professional.copy(
        slots = professional.slots
    )

    data class Input(
        val professionalId: UUID,
        val slot: List<Interval>
    )

    data class Output(
        val professional: Professional
    )
}
