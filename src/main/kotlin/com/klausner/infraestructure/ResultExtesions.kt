package com.klausner.infraestructure

import com.klausner.routes.ErrorResponse
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext


fun <T> T.success(): Result<T> = Result.success(this)
fun Throwable.failure(): Result<Any> = Result.failure(this)

fun <T> Result<Result<T>>.flatten(): Result<T> = this.fold(
    onSuccess = { it },
    onFailure = { Result.failure(it) }
)

suspend inline fun RoutingContext.foldAndRespond(result: Result<Any>) {
    result.fold(
        onSuccess = { call.respond(it) },
        onFailure = {
            call.respond(
                ErrorResponse(
                    message = "Internal server error",
                    status = 500,
                    timestamp = System.currentTimeMillis(),
                    debugMessage = it.message ?: "Internal server error"
                )
            )
        }
    )
}
