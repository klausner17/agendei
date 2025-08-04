package com.klausner.usecases.professional

import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.UseCase
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

class DeleteProfessionalUseCase(
    private val IProfessionalRepository: IProfessionalRepository
) : UseCase<UUID, Unit> {

    override fun execute(input: UUID): Result<Unit> {
        return IProfessionalRepository.delete(input)
            .onSuccess { logger.info("Professional {} deleted with success", input) }
            .onFailure { logger.error("Error deleting professional {}", input, it) }
    }

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(DeleteProfessionalUseCase::class.java)
    }
}
