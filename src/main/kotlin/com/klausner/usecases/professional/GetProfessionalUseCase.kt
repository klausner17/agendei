package com.klausner.usecases.professional

import com.klausner.domains.valueobjects.Address
import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Phone
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.professional.GetProfessionalUseCase.Input
import com.klausner.usecases.professional.GetProfessionalUseCase.Output
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

class GetProfessionalUseCase(
    private val professionalRepository: IProfessionalRepository
) : UseCase<Input, Output> {

    override fun execute(input: Input) = professionalRepository.find(input.professionalId)
        .map { Output.fromDomain(it) }

    data class Input(
        val professionalId: UUID
    )

    @Serializable
    data class Output(
        @Contextual val id: UUID,
        val name: String,
        val bio: String?,
        val email: String?,
        val address: Address?,
        val phone: Phone?,
        val instagram: String?,
        val facebook: String?,
        val photo: String?,
        val workHours: List<Interval>?
    ) {

        companion object {
            fun fromDomain(professional: com.klausner.domains.Professional): Output {
                return Output(
                    id = professional.id,
                    name = professional.name,
                    bio = professional.bio,
                    email = professional.email,
                    address = professional.address,
                    phone = professional.phone,
                    instagram = professional.instagram,
                    facebook = professional.facebook,
                    photo = professional.photo,
                    workHours = professional.workHours
                )
            }
        }
    }
}
