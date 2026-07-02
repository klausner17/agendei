package com.klausner.usecases.auth

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import com.klausner.usecases.UseCase
import java.util.UUID

class RegisterProfessionalUseCase(
    private val professionalRepository: IProfessionalRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) : UseCase<RegisterProfessionalUseCase.Input, RegisterProfessionalUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        runCatching {
            validate(input)
            ensureEmailIsAvailable(input.email)
            val professional = createProfessional(input)
            Output(token = jwtService.generateToken(professional), user = toProfessionalData(professional))
        }

    private fun validate(input: Input) {
        require(input.email.isNotBlank()) { "Email is required" }
        require(input.name.isNotBlank()) { "Name is required" }
        require(input.password.length >= MINIMUM_PASSWORD_LENGTH) {
            "Password must have at least $MINIMUM_PASSWORD_LENGTH characters"
        }
    }

    private fun ensureEmailIsAvailable(email: String) {
        val existing = professionalRepository.findByEmail(email).getOrThrow()
        require(existing == null) { "Email already registered" }
    }

    private fun createProfessional(input: Input): Professional {
        val professional =
            Professional(
                id = UUID.randomUUID(),
                name = input.name,
                email = input.email,
                password = passwordHasher.hash(input.password),
            )
        return professionalRepository.create(professional).getOrThrow()
    }

    private fun toProfessionalData(professional: Professional) =
        ProfessionalData(
            id = professional.id.toString(),
            email = professional.email,
            name = professional.name,
        )

    data class Input(
        val email: String,
        val name: String,
        val password: String,
    )

    data class Output(
        val token: String,
        val user: ProfessionalData,
    )

    data class ProfessionalData(
        val id: String,
        val email: String?,
        val name: String,
    )

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH = 8
    }
}
