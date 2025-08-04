package com.klausner.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import kotlinx.serialization.Serializable

fun StatusPagesConfig.config() {
    exception<Throwable> { call, cause ->
        call.respond(
            message = ErrorResponse(
                message = "Internal server error",
                status = HttpStatusCode.InternalServerError.value,
                timestamp = System.currentTimeMillis(),
                debugMessage = cause.message
            ),
            status = HttpStatusCode.InternalServerError
        )
    }
}

@Serializable
data class ErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: Long,
    val debugMessage: String? = null
)
