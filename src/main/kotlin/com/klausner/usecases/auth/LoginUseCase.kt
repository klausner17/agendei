package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import com.klausner.usecases.UseCase

class LoginUseCase(
    private val userRepository: IUserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) : UseCase<LoginUseCase.Input, LoginUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        runCatching {
            val user = authenticate(input)
            Output(token = jwtService.generateToken(user), user = toUserData(user))
        }

    private fun authenticate(input: Input): User {
        val user =
            userRepository.findByEmail(input.email).getOrThrow()
                ?: throw InvalidCredentialsException()
        if (passwordHasher.verify(input.password, user.passwordHash)) return user
        throw InvalidCredentialsException()
    }

    private fun toUserData(user: User) = UserData(id = user.id.toString(), email = user.email, name = user.name)

    data class Input(
        val email: String,
        val password: String,
    )

    data class Output(
        val token: String,
        val user: UserData,
    )

    data class UserData(
        val id: String,
        val email: String,
        val name: String,
    )
}
