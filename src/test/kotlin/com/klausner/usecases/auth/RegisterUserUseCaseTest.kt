package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegisterUserUseCaseTest {
    private val userRepository = mockk<IUserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val jwtService = mockk<JwtService>()
    private val useCase = RegisterUserUseCase(userRepository, passwordHasher, jwtService)

    private val user =
        User(id = UUID.randomUUID(), email = "ana@email.com", name = "Ana", passwordHash = "hashed")

    @Test
    fun `should register user successfully`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(null)
        every { passwordHasher.hash("password123") } returns "hashed"
        every { userRepository.create(any()) } returns Result.success(user)
        every { jwtService.generateToken(user) } returns "jwt-token"

        // when
        val result = useCase.execute(RegisterUserUseCase.Input("ana@email.com", "Ana", "password123"))

        // then
        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrThrow().token)
        assertEquals("ana@email.com", result.getOrThrow().user.email)
        verify(exactly = 1) { userRepository.create(any()) }
    }

    @Test
    fun `should fail when email already registered`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(user)

        // when
        val result = useCase.execute(RegisterUserUseCase.Input("ana@email.com", "Ana", "password123"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun `should fail when password is shorter than 8 characters`() {
        // when
        val result = useCase.execute(RegisterUserUseCase.Input("ana@email.com", "Ana", "short"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        verify(exactly = 0) { userRepository.create(any()) }
    }
}
