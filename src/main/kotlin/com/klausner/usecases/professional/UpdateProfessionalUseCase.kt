package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.domains.valueobjects.Address
import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Phone
import com.klausner.infraestructure.flatten
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.professional.UpdateProfessionalUseCase.Input
import com.klausner.usecases.professional.UpdateProfessionalUseCase.Output
import java.util.UUID

class UpdateProfessionalUseCase(
    private val professionalRepository: IProfessionalRepository,
) : UseCase<Input, Output> {

    override fun execute(input: Input): Result<Output> {
        return professionalRepository.find(input.id)
            .map {
                val updatedProfessional = it.copy(
                    name = input.name ?: it.name,
                    bio = input.bio ?: it.bio,
                    address = input.address ?: it.address,
                    password = input.password ?: it.password,
                    phone = input.phone ?: it.phone,
                    instagram = input.instagram ?: it.instagram,
                    facebook = input.facebook ?: it.facebook,
                    photo = input.photo ?: it.photo,
                    workHours = input.workHours ?: it.workHours
                )

                professionalRepository.update(updatedProfessional)
                    .map { domainToOutput(it) }
            }.flatten()
    }

    private fun domainToOutput(domain: Professional) = Output(
        id = domain.id,
        storeId = domain.storeId,
        name = domain.name,
        bio = domain.bio,
        address = domain.address,
        password = domain.password,
        phone = domain.phone,
        instagram = domain.instagram,
        facebook = domain.facebook,
        photo = domain.photo,
        workHours = domain.workHours
    )

    data class Input(
        val id: UUID,
        val name: String? = null,
        val bio: String? = null,
        val address: Address? = null,
        val password: String? = null,
        val phone: Phone? = null,
        val instagram: String? = null,
        val facebook: String? = null,
        val photo: String? = null,
        val doLogin: Boolean? = null,
        val workHours: List<Interval>? = null
    )

    data class Output(
        val id: UUID,
        val storeId: UUID?,
        val name: String,
        val bio: String?,
        val address: Address?,
        val password: String?,
        val phone: Phone?,
        val instagram: String?,
        val facebook: String?,
        val photo: String?,
        val workHours: List<Interval>?
    )
}
