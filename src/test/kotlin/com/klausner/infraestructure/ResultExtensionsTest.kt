package com.klausner.infraestructure

import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultExtensionsTest {
    @Test
    fun `deve mapear NoSuchElementException para 404`() {
        // given
        val exception = NoSuchElementException("not found")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.NotFound, status)
    }

    @Test
    fun `deve mapear IllegalArgumentException para 400`() {
        // given
        val exception = IllegalArgumentException("invalid input")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.BadRequest, status)
    }

    @Test
    fun `deve mapear IllegalStateException para 422`() {
        // given
        val exception = IllegalStateException("slot not available")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.UnprocessableEntity, status)
    }

    @Test
    fun `deve mapear excecao generica para 500`() {
        // given
        val exception = RuntimeException("unexpected error")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.InternalServerError, status)
    }
}
