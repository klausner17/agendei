package com.klausner.usecases.service

import com.klausner.domains.Service
import com.klausner.repositories.service.IServiceRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.service.CreateServiceUseCase.Input
import com.klausner.usecases.service.CreateServiceUseCase.Output
import java.util.UUID

class CreateServiceUseCase(
    private val serviceRepository: IServiceRepository,
) : UseCase<Input, Output> {
    override fun execute(input: Input) =
        serviceRepository
            .create(input.toDomain())
            .map(Output::fromDomain)

    data class Input(
        val name: String,
        val professionalId: UUID,
        val description: String?,
        val price: Int,
        val durationInMinutes: Int,
    ) {
        fun toDomain() =
            Service(
                id = UUID.randomUUID(),
                professionalId = professionalId,
                name = name,
                description = description,
                price = price,
                durationInMinutes = durationInMinutes,
            )
    }

    data class Output(
        val id: String,
        val name: String,
        val description: String?,
        val price: Int,
        val professionalId: String,
    ) {
        companion object {
            fun fromDomain(service: Service): Output =
                Output(
                    id = service.id.toString(),
                    name = service.name,
                    description = service.description,
                    price = service.price,
                    professionalId = service.professionalId.toString(),
                )
        }
    }
}
