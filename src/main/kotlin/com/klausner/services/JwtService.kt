package com.klausner.services

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.klausner.domains.User
import java.util.Date

class JwtService(
    secret: String,
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(user: User): String =
        JWT
            .create()
            .withSubject(user.id.toString())
            .withClaim("email", user.email)
            .withClaim("name", user.name)
            .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 dias
            .sign(algorithm)

    fun verifyToken(token: String): String? =
        try {
            val verifier = JWT.require(algorithm).build()
            val decodedJWT = verifier.verify(token)
            decodedJWT.subject
        } catch (e: Exception) {
            null
        }
}
