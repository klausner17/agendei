package com.klausner.usecases.service

import com.klausner.repositories.service.IServiceRepository
import com.klausner.usecases.UseCase
import java.util.UUID

class DeleteServiceUseCase(
    private val serviceRepository: IServiceRepository,
) : UseCase<DeleteServiceUseCase.Input, DeleteServiceUseCase.Output> {

    override fun execute(input: Input): Result<Output> {
        return serviceRepository
            .delete(input.id)
            .map { Output() }
    }

    data class Input(val id: UUID)

    data class Output {

    }
}
