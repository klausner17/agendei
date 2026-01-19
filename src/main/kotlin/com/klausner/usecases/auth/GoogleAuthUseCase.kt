package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.GoogleAuthService
import com.klausner.services.JwtService
import com.klausner.usecases.UseCase
import java.util.UUID

class GoogleAuthUseCase(
    private val googleAuthService: GoogleAuthService,
    private val jwtService: JwtService,
    private val userRepository: IUserRepository
) : UseCase<GoogleAuthUseCase.Input, GoogleAuthUseCase.Output> {

    override fun execute(input: Input): Result<Output> {
        return runCatching {
            // 1. Validar credential com Google
            val payload = googleAuthService.verifyIdToken(input.credential)
                ?: throw IllegalArgumentException("Invalid Google credential")

            // 2. Extrair dados do usuário
            val googleId = payload.subject
            val email = payload.email
            val name = payload["name"] as? String ?: email
            val picture = payload["picture"] as? String
            val emailVerified = payload.emailVerified

            // 3. Buscar ou criar usuário
            val user = userRepository.findByGoogleId(googleId).getOrNull()
                ?: userRepository.findByEmail(email).getOrNull()
                ?: run {
                    val newUser = User(
                        id = UUID.randomUUID(),
                        email = email,
                        name = name,
                        picture = picture,
                        provider = "google",
                        googleId = googleId,
                        emailVerified = emailVerified
                    )
                    userRepository.create(newUser).getOrThrow()
                }

            // 4. Gerar token JWT da aplicação
            val token = jwtService.generateToken(user)

            // 5. Retornar resposta
            Output(
                token = token,
                user = UserData(
                    id = user.id.toString(),
                    email = user.email,
                    name = user.name,
                    picture = user.picture,
                    provider = user.provider
                )
            )
        }
    }

    data class Input(
        val credential: String
    )

    data class Output(
        val token: String,
        val user: UserData
    )

    data class UserData(
        val id: String,
        val email: String,
        val name: String,
        val picture: String?,
        val provider: String
    )
}

