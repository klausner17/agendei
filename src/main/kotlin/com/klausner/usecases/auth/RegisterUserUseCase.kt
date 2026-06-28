package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import com.klausner.usecases.UseCase
import java.util.UUID

class RegisterUserUseCase(
    private val userRepository: IUserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtService: JwtService,
) : UseCase<RegisterUserUseCase.Input, RegisterUserUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        runCatching {
            validate(input)
            ensureEmailIsAvailable(input.email)
            val user = createUser(input)
            Output(token = jwtService.generateToken(user), user = toUserData(user))
        }

    private fun validate(input: Input) {
        require(input.email.isNotBlank()) { "Email is required" }
        require(input.password.length >= MINIMUM_PASSWORD_LENGTH) {
            "Password must have at least $MINIMUM_PASSWORD_LENGTH characters"
        }
    }

    private fun ensureEmailIsAvailable(email: String) {
        val existing = userRepository.findByEmail(email).getOrThrow()
        require(existing == null) { "Email already registered" }
    }

    private fun createUser(input: Input): User {
        val user =
            User(
                id = UUID.randomUUID(),
                email = input.email,
                name = input.name,
                passwordHash = passwordHasher.hash(input.password),
            )
        return userRepository.create(user).getOrThrow()
    }

    private fun toUserData(user: User) =
        UserData(
            id = user.id.toString(),
            email = user.email,
            name = user.name,
        )

    data class Input(
        val email: String,
        val name: String,
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

    companion object {
        private const val MINIMUM_PASSWORD_LENGTH = 8
    }
}
