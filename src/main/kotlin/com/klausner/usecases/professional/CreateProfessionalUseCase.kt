package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.domains.valueobjects.Address
import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Phone
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.professional.CreateProfessionalUseCase.Input
import com.klausner.usecases.professional.CreateProfessionalUseCase.Output
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

class CreateProfessionalUseCase(
    private val repository: IProfessionalRepository,
) : UseCase<Input, Output> {

    override fun execute(input: Input): Result<Output> {
        return repository.create(inputToDomain(input))
            .map { domainToOutput(it) }
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

    private fun inputToDomain(input: Input) = Professional(
        id = UUID.randomUUID(),
        name = input.name,
        bio = input.bio,
        address = null,
        password = input.password,
        phone = input.phone,
        instagram = input.instagram,
        facebook = input.facebook,
        photo = input.photo,
        workHours = input.workHours ?: emptyList()
    )

    @Serializable
    data class Input(
        val name: String,
        val email: String? = null,
        val phone: Phone? = null,
        val password: String? = null,
        val establishmentId: Long? = null,
        val bio: String? = null,
        val instagram: String? = null,
        val facebook: String? = null,
        val photo: String? = null,
        val workHours: List<Interval>? = null
    )

    @Serializable
    data class Output(
        @Contextual val id: UUID,
        @Contextual val storeId: UUID?,
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
