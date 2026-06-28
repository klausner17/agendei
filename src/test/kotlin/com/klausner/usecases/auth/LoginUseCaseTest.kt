package com.klausner.usecases.auth

import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LoginUseCaseTest {
    private val userRepository = mockk<IUserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val jwtService = mockk<JwtService>()
    private val useCase = LoginUseCase(userRepository, passwordHasher, jwtService)

    private val user =
        User(id = UUID.randomUUID(), email = "ana@email.com", name = "Ana", passwordHash = "stored-hash")

    @Test
    fun `should login successfully`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(user)
        every { passwordHasher.verify("password123", "stored-hash") } returns true
        every { jwtService.generateToken(user) } returns "jwt-token"

        // when
        val result = useCase.execute(LoginUseCase.Input("ana@email.com", "password123"))

        // then
        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrThrow().token)
    }

    @Test
    fun `should fail when user does not exist`() {
        // given
        every { userRepository.findByEmail("ghost@email.com") } returns Result.success(null)

        // when
        val result = useCase.execute(LoginUseCase.Input("ghost@email.com", "password123"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }

    @Test
    fun `should fail when password is wrong`() {
        // given
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(user)
        every { passwordHasher.verify("wrong", "stored-hash") } returns false

        // when
        val result = useCase.execute(LoginUseCase.Input("ana@email.com", "wrong"))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
    }
}
