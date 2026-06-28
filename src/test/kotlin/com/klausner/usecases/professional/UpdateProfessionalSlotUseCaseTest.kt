package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.domains.valueobjects.Interval
import com.klausner.repositories.professional.IProfessionalRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

class UpdateProfessionalSlotUseCaseTest {
    private val repository = mockk<IProfessionalRepository>()
    private val useCase = UpdateProfessionalSlotUseCase(repository)

    private val professionalId = UUID.randomUUID()
    private val professional = Professional(id = professionalId, name = "Ana Lima")
    private val slot = Interval(startTime = LocalDateTime.of(2026, 7, 1, 9, 0), endTime = LocalDateTime.of(2026, 7, 1, 10, 0))

    @Test
    fun `deve atualizar slots do profissional com sucesso`() {
        every { repository.find(professionalId) } returns Result.success(professional)
        every { repository.update(any()) } returns Result.success(professional)

        val result = useCase.execute(
            UpdateProfessionalSlotUseCase.Input(professionalId = professionalId, slot = listOf(slot)),
        )

        assertTrue(result.isSuccess)
        verify(exactly = 1) { repository.find(professionalId) }
        verify(exactly = 1) { repository.update(any()) }
    }

    @Test
    fun `deve retornar falha quando profissional nao existe`() {
        every { repository.find(professionalId) } returns Result.failure(NoSuchElementException("not found"))

        val result = useCase.execute(
            UpdateProfessionalSlotUseCase.Input(professionalId = professionalId, slot = listOf(slot)),
        )

        assertTrue(result.isFailure)
    }
}
