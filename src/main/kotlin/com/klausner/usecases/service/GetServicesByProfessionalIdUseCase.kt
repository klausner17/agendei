package com.klausner.usecases.service

import com.klausner.domains.Service
import com.klausner.repositories.service.IServiceRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.service.GetServicesByProfessionalIdUseCase.Input
import com.klausner.usecases.service.GetServicesByProfessionalIdUseCase.Output
import java.util.UUID

class GetServicesByProfessionalIdUseCase(
    private val serviceRepository: IServiceRepository,
) : UseCase<Input, Output> {
    override fun execute(input: Input) =
        serviceRepository
            .findByProfessionalId(input.professionalId)
            .map { services -> Output(services.map(Output.ServiceOutput::fromDomain)) }

    data class Input(
        val professionalId: UUID,
    )

    data class Output(
        val services: List<ServiceOutput>,
    ) {
        data class ServiceOutput(
            val id: UUID,
            val professionalId: UUID,
            val description: String?,
            val price: Int,
            val durationInMinutes: Int,
        ) {
            companion object {
                fun fromDomain(service: Service) =
                    ServiceOutput(
                        id = service.id,
                        professionalId = service.professionalId,
                        description = service.description,
                        price = service.price,
                        durationInMinutes = service.durationInMinutes,
                    )
            }
        }
    }
}
