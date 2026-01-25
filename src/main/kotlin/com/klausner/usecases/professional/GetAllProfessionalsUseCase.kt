package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.UseCaseWithoutOutput
import com.klausner.usecases.professional.GetAllProfessionalsUseCase.Output
import java.util.UUID

class GetAllProfessionalsUseCase(
    private val professionalRepository: IProfessionalRepository,
) : UseCaseWithoutOutput<Output> {

    override fun execute(): Result<Output> =
        professionalRepository
            .findAll()
            .map { professionals ->
                Output(
                    professionals = professionals.map { Output.ProfessionalOutput.fromDomain(it) }
                )
            }

    data class Output(
        val professionals: List<ProfessionalOutput>,
    ) {
        data class ProfessionalOutput(
            val id: UUID,
            val name: String,
        ) {
            companion object {
                fun fromDomain(professional: Professional): ProfessionalOutput =
                    ProfessionalOutput(
                        id = professional.id,
                        name = professional.name,
                    )
            }
        }
    }
}

