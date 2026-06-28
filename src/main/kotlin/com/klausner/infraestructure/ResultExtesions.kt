package com.klausner.infraestructure

import com.klausner.routes.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext

fun <T> T.success(): Result<T> = Result.success(this)

fun Throwable.failure(): Result<Any> = Result.failure(this)

fun <T> Result<Result<T>>.flatten(): Result<T> =
    this.fold(
        onSuccess = { it },
        onFailure = { Result.failure(it) },
    )

fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> =
    this.fold(
        onSuccess = transform,
        onFailure = { Result.failure(it) },
    )

suspend inline fun RoutingContext.foldAndRespond(result: Result<Any>) {
    result.fold(
        onSuccess = {
            if (it is List<*> && it.isEmpty()) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(it)
            }
        },
        onFailure = { exception ->
            val status = statusFor(exception)
            call.respond(
                status,
                ErrorResponse(
                    message = status.description,
                    status = status.value,
                    timestamp = System.currentTimeMillis(),
                    debugMessage = exception.message,
                ),
            )
        },
    )
}

fun statusFor(exception: Throwable): HttpStatusCode =
    when (exception) {
        is NoSuchElementException -> HttpStatusCode.NotFound
        is IllegalArgumentException -> HttpStatusCode.BadRequest
        is IllegalStateException -> HttpStatusCode.UnprocessableEntity
        else -> HttpStatusCode.InternalServerError
    }
