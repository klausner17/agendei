package com.klausner.usecases.auth

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {
    private val professionalRepository = mockk<IProfessionalRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val jwtService = mockk<JwtService>()
    private val useCase = LoginUseCase(professionalRepository, passwordHasher, jwtService)

    private val professional =
        Professional(id = UUID.randomUUID(), name = "Ana", email = "ana@email.com", password = "stored-hash")

    @Test
    fun `should login successfully`() {
        // given
        every { professionalRepository.findByEmail("ana@email.com") } returns Result.success(professional)
        every { passwordHasher.verify("password123", "stored-hash") } returns true
        every { jwtService.generateToken(professional) } returns "jwt-token"

        // when
        val result = useCase.execute(LoginUseCase.Input("ana@email.com", "password123"))

        // then
        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrThrow().token)
    }

    @Test
    fun `should fail when professional does not exist`() {
        // given
        every { professionalRepository.findByEmail("ghost@email.com") } returns Result.success(null)

        // when
        val result = useCase.execute(LoginUseCase.Input("ghost@email.com", "password123"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }

    @Test
    fun `should fail when password is wrong`() {
        // given
        every { professionalRepository.findByEmail("ana@email.com") } returns Result.success(professional)
        every { passwordHasher.verify("wrong", "stored-hash") } returns false

        // when
        val result = useCase.execute(LoginUseCase.Input("ana@email.com", "wrong"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }

    @Test
    fun `should fail when professional has no password set`() {
        // given
        every { professionalRepository.findByEmail("ana@email.com") } returns
            Result.success(professional.copy(password = null))

        // when
        val result = useCase.execute(LoginUseCase.Input("ana@email.com", "password123"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }
}
