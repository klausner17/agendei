package com.klausner.usecases.professional

import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.professional.GetMyProfessionalUseCase.Input
import com.klausner.usecases.professional.GetProfessionalUseCase.Output
import java.util.UUID

class GetMyProfessionalUseCase(
    private val professionalRepository: IProfessionalRepository,
) : UseCase<Input, Output> {
    override fun execute(input: Input) =
        professionalRepository
            .findByUserId(input.userId)
            .map { Output.fromDomain(it) }

    data class Input(
        val userId: UUID,
    )
}
