package com.klausner.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.auth.GoogleAuthUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.receive
import io.ktor.server.response.respond
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
    val googleAuthUseCase: GoogleAuthUseCase by getKoin().inject()

    post("/api/auth/google") {
        try {
            val request = call.receive<GoogleAuthRequest>()

            if (request.credential.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("message" to "Credential é obrigatório"))
                return@post
            }

            val input = GoogleAuthUseCase.Input(credential = request.credential)
            foldAndRespond(googleAuthUseCase.execute(input))
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.Unauthorized,
                mapOf("message" to "Falha na autenticação com Google: ${e.message}"),
            )
        }
    }
}

data class GoogleAuthRequest(
    val credential: String,
)
