package com.klausner.usecases.auth

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import com.klausner.usecases.UseCase

class LoginUseCase(
    private val professionalRepository: IProfessionalRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) : UseCase<LoginUseCase.Input, LoginUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        runCatching {
            val professional = authenticate(input)
            Output(token = jwtService.generateToken(professional), user = toProfessionalData(professional))
        }

    private fun authenticate(input: Input): Professional {
        val professional =
            professionalRepository.findByEmail(input.email).getOrThrow()
                ?: throw InvalidCredentialsException()
        val storedPassword = professional.password ?: throw InvalidCredentialsException()
        if (passwordHasher.verify(input.password, storedPassword)) return professional
        throw InvalidCredentialsException()
    }

    private fun toProfessionalData(professional: Professional) =
        ProfessionalData(id = professional.id.toString(), email = professional.email, name = professional.name)

    data class Input(
        val email: String,
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
}
