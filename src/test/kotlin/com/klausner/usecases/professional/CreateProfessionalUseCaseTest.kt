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

    @Test
    fun `deve criar profissional com sucesso`() {
        val professional = Professional(id = UUID.randomUUID(), name = "Ana Lima")
        every { repository.create(any()) } returns Result.success(professional)

        val result = useCase.execute(CreateProfessionalUseCase.Input(name = "Ana Lima"))

        assertTrue(result.isSuccess)
        assertEquals("Ana Lima", result.getOrThrow().name)
        verify(exactly = 1) { repository.create(any()) }
    }

    @Test
    fun `deve retornar falha quando repositorio falha`() {
        every { repository.create(any()) } returns Result.failure(RuntimeException("DB error"))

        val result = useCase.execute(CreateProfessionalUseCase.Input(name = "Ana Lima"))

        assertTrue(result.isFailure)
    }
}
