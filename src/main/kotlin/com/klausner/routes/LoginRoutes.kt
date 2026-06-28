package com.klausner.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.auth.LoginUseCase
import com.klausner.usecases.auth.RegisterUserUseCase
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.java.KoinJavaComponent.getKoin

fun AuthenticationConfig.config() {
    val jwtSecret = System.getenv("JWT_SECRET") ?: "your-super-secret-jwt-key-change-in-production"

    jwt("jwt-auth") {
        realm = "agendei"
        verifier(
            JWT
                .require(Algorithm.HMAC256(jwtSecret))
                .build(),
        )
        validate { credential ->
            if (credential.payload.subject == null) {
                null
            } else {
                JWTPrincipal(credential.payload)
            }
        }
    }
}

fun Route.loginRoutes() {
    val registerUserUseCase: RegisterUserUseCase by getKoin().inject()
    val loginUseCase: LoginUseCase by getKoin().inject()

    post("/api/v1/auth/register") {
        val request = call.receive<RegisterRequest>()
        val input = RegisterUserUseCase.Input(request.email, request.name, request.password)
        foldAndRespond(registerUserUseCase.execute(input))
    }

    post("/api/v1/auth/login") {
        val request = call.receive<LoginRequest>()
        val input = LoginUseCase.Input(request.email, request.password)
        foldAndRespond(loginUseCase.execute(input))
    }
}

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String,
)

data class LoginRequest(
    val email: String,
    val password: String,
)
