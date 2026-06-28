package com.klausner.infraestructure

import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultExtensionsTest {
    @Test
    fun `should map NoSuchElementException to 404`() {
        // given
        val exception = NoSuchElementException("not found")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.NotFound, status)
    }

    @Test
    fun `should map IllegalArgumentException to 400`() {
        // given
        val exception = IllegalArgumentException("invalid input")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.BadRequest, status)
    }

    @Test
    fun `should map IllegalStateException to 422`() {
        // given
        val exception = IllegalStateException("slot not available")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.UnprocessableEntity, status)
    }

    @Test
    fun `should map generic exception to 500`() {
        // given
        val exception = RuntimeException("unexpected error")

        // when
        val status = statusFor(exception)

        // then
        assertEquals(HttpStatusCode.InternalServerError, status)
    }
}
