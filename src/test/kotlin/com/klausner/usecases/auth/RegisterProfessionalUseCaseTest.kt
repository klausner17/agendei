package com.klausner.usecases.auth

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterProfessionalUseCaseTest {
    private val professionalRepository = mockk<IProfessionalRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val jwtService = mockk<JwtService>()
    private val useCase = RegisterProfessionalUseCase(professionalRepository, passwordHasher, jwtService)

    @Test
    fun `should register professional successfully`() {
        // given
        val created = slot<Professional>()
        every { professionalRepository.findByEmail("ana@email.com") } returns Result.success(null)
        every { passwordHasher.hash("password123") } returns "hashed"
        every { professionalRepository.create(capture(created)) } answers { Result.success(created.captured) }
        every { jwtService.generateToken(any()) } returns "jwt-token"

        // when
        val result = useCase.execute(RegisterProfessionalUseCase.Input("ana@email.com", "Ana", "password123"))

        // then
        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrThrow().token)
        assertEquals("hashed", created.captured.password)
        assertEquals("Ana", created.captured.name)
        verify(exactly = 1) { professionalRepository.create(any()) }
    }

    @Test
    fun `should fail when email already registered`() {
        // given
        val existing = Professional(name = "Ana", email = "ana@email.com", password = "hashed")
        every { professionalRepository.findByEmail("ana@email.com") } returns Result.success(existing)

        // when
        val result = useCase.execute(RegisterProfessionalUseCase.Input("ana@email.com", "Ana", "password123"))

        // then
        assertTrue(result.isFailure)
        verify(exactly = 0) { professionalRepository.create(any()) }
    }

    @Test
    fun `should fail when password is too short`() {
        // given
        every { professionalRepository.findByEmail(any()) } returns Result.success(null)

        // when
        val result = useCase.execute(RegisterProfessionalUseCase.Input("ana@email.com", "Ana", "short"))

        // then
        assertTrue(result.isFailure)
        verify(exactly = 0) { professionalRepository.create(any()) }
    }
}
