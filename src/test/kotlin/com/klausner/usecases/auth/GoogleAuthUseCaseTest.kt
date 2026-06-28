package com.klausner.usecases.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.klausner.domains.User
import com.klausner.repositories.user.IUserRepository
import com.klausner.services.GoogleAuthService
import com.klausner.services.JwtService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoogleAuthUseCaseTest {
    private val googleAuthService = mockk<GoogleAuthService>()
    private val jwtService = mockk<JwtService>()
    private val userRepository = mockk<IUserRepository>()
    private val useCase = GoogleAuthUseCase(googleAuthService, jwtService, userRepository)

    private val existingUser =
        User(
            id = UUID.randomUUID(),
            email = "ana@email.com",
            name = "Ana Lima",
            googleId = "google-123",
        )

    private fun mockPayload(
        googleId: String = "google-123",
        email: String = "ana@email.com",
        name: String = "Ana Lima",
        picture: String? = null,
        emailVerified: Boolean = true,
    ): GoogleIdToken.Payload {
        val payload = mockk<GoogleIdToken.Payload>()
        every { payload.subject } returns googleId
        every { payload.email } returns email
        every { payload["name"] } returns name
        every { payload["picture"] } returns picture
        every { payload.emailVerified } returns emailVerified
        return payload
    }

    @Test
    fun `deve autenticar usuario existente encontrado pelo googleId`() {
        every { googleAuthService.verifyIdToken("valid-token") } returns mockPayload()
        every { userRepository.findByGoogleId("google-123") } returns Result.success(existingUser)
        every { jwtService.generateToken(existingUser) } returns "jwt-token"

        val result = useCase.execute(GoogleAuthUseCase.Input(credential = "valid-token"))

        assertTrue(result.isSuccess)
        assertEquals("jwt-token", result.getOrThrow().token)
        assertEquals("ana@email.com", result.getOrThrow().user.email)
        verify(exactly = 0) { userRepository.findByEmail(any()) }
        verify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun `deve autenticar usuario existente encontrado pelo email quando nao encontrado pelo googleId`() {
        every { googleAuthService.verifyIdToken("valid-token") } returns mockPayload()
        every { userRepository.findByGoogleId("google-123") } returns Result.success(null)
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(existingUser)
        every { jwtService.generateToken(existingUser) } returns "jwt-token"

        val result = useCase.execute(GoogleAuthUseCase.Input(credential = "valid-token"))

        assertTrue(result.isSuccess)
        verify(exactly = 1) { userRepository.findByEmail("ana@email.com") }
        verify(exactly = 0) { userRepository.create(any()) }
    }

    @Test
    fun `deve criar novo usuario quando nao encontrado em nenhuma busca`() {
        every { googleAuthService.verifyIdToken("valid-token") } returns mockPayload()
        every { userRepository.findByGoogleId("google-123") } returns Result.success(null)
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(null)
        every { userRepository.create(any()) } returns Result.success(existingUser)
        every { jwtService.generateToken(existingUser) } returns "jwt-token"

        val result = useCase.execute(GoogleAuthUseCase.Input(credential = "valid-token"))

        assertTrue(result.isSuccess)
        verify(exactly = 1) { userRepository.create(any()) }
    }

    @Test
    fun `deve retornar falha quando credencial google e invalida`() {
        every { googleAuthService.verifyIdToken("invalid-token") } returns null

        val result = useCase.execute(GoogleAuthUseCase.Input(credential = "invalid-token"))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `deve retornar falha quando criacao de usuario falha`() {
        every { googleAuthService.verifyIdToken("valid-token") } returns mockPayload()
        every { userRepository.findByGoogleId("google-123") } returns Result.success(null)
        every { userRepository.findByEmail("ana@email.com") } returns Result.success(null)
        every { userRepository.create(any()) } returns Result.failure(RuntimeException("DB error"))

        val result = useCase.execute(GoogleAuthUseCase.Input(credential = "valid-token"))

        assertTrue(result.isFailure)
    }
}
