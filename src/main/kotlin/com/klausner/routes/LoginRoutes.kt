package com.klausner.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.routing.Route

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
}
