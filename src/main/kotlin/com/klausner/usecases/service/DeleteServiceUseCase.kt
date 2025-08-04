package com.klausner.usecases.service

import com.klausner.repositories.service.IServiceRepository
import com.klausner.usecases.UseCase
import java.util.UUID

class DeleteServiceUseCase(
    private val serviceRepository: IServiceRepository,
) : UseCase<DeleteServiceUseCase.Input, Boolean> {

    override fun execute(input: Input): Result<Boolean> {
        return serviceRepository
            .delete(input.id)
            .map { true }
    }

    data class Input(val id: UUID)

}
