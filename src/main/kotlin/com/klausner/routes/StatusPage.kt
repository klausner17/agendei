package com.klausner.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.response.respond
import org.slf4j.LoggerFactory

fun StatusPagesConfig.config() {
    exception<Throwable> { call, cause ->
        logger.error("Error handling request", cause)
        call.respond(
            message =
                ErrorResponse(
                    message = "Internal server error",
                    status = HttpStatusCode.InternalServerError.value,
                    timestamp = System.currentTimeMillis(),
                    debugMessage = cause.message,
                ),
            status = HttpStatusCode.InternalServerError,
        )
    }
}

data class ErrorResponse(
    val message: String,
    val status: Int,
    val timestamp: Long,
    val debugMessage: String? = null,
)

private val logger = LoggerFactory.getLogger("StatusPage")
