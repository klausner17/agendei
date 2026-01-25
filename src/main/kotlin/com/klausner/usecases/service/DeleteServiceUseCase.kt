package com.klausner.usecases.service

import com.klausner.repositories.service.IServiceRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.service.DeleteServiceUseCase.Input
import com.klausner.usecases.service.DeleteServiceUseCase.Output
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

class DeleteServiceUseCase(
    private val serviceRepository: IServiceRepository,
) : UseCase<Input, Output> {

    override fun execute(input: Input): Result<Output> {
        return serviceRepository
            .delete(input.id)
            .onSuccess { logger.info("Service {} deleted with success", input.id) }
            .onFailure { logger.error("Error deleting service {}", input.id, it) }
            .map { Output.fromDeletion() }
    }

    data class Input(
        val id: UUID,
    )

    data class Output(
        val success: Boolean,
    ) {
        companion object {
            fun fromDeletion(): Output = Output(success = true)
        }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(DeleteServiceUseCase::class.java)
    }
}
