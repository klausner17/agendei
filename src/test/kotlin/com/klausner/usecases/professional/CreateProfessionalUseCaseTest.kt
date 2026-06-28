package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateProfessionalUseCaseTest {
    private val repository = mockk<IProfessionalRepository>()
    private val useCase = CreateProfessionalUseCase(repository)

    private val userId = UUID.randomUUID()

    @Test
    fun `should create professional successfully`() {
        // given
        val professional = Professional(id = UUID.randomUUID(), userId = userId, name = "Ana Lima")
        every { repository.create(any()) } returns Result.success(professional)

        // when
        val result = useCase.execute(CreateProfessionalUseCase.Input(userId = userId, name = "Ana Lima"))

        // then
        assertTrue(result.isSuccess)
        assertEquals("Ana Lima", result.getOrThrow().name)
        verify(exactly = 1) { repository.create(any()) }
    }

    @Test
    fun `should return failure when repository fails`() {
        // given
        every { repository.create(any()) } returns Result.failure(RuntimeException("DB error"))

        // when
        val result = useCase.execute(CreateProfessionalUseCase.Input(userId = userId, name = "Ana Lima"))

        // then
        assertTrue(result.isFailure)
    }
}
