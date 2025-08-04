package com.klausner.usecases.service

import com.klausner.domains.Service
import com.klausner.domains.valueobjects.Money
import com.klausner.domains.valueobjects.MoneyFixed
import com.klausner.repositories.service.IServiceRepository
import com.klausner.usecases.UseCase
import java.util.UUID

class CreateServiceUseCase(
    private val serviceRepository: IServiceRepository,
) : UseCase<CreateServiceUseCase.Input, CreateServiceUseCase.Output> {

    override fun execute(input: Input) = serviceRepository
        .create(input.toDomain())
        .map(Output::fromDomain)

    data class Input(
        val name: String,
        val professionalId: UUID,
        val description: String,
        val price: Int = 0,
    ) {
        fun toDomain() = Service(
            id = UUID.randomUUID(),
            professionalId = professionalId,
            description = description,
            price = MoneyFixed(price),
        )
    }

    data class Output(
        val id: String,
        val description: String,
        val price: Money,
        val professionalId: String,
    ) {

        companion object {
            fun fromDomain(service: Service): Output {
                return Output(
                    id = service.id.toString(),
                    description = service.description,
                    price = service.price,
                    professionalId = service.professionalId.toString()
                )
            }
        }
    }
}
