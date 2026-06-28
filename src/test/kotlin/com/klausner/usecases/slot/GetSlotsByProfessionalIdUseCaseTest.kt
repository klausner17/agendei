package com.klausner.usecases.slot

import com.klausner.domains.Slot
import com.klausner.repositories.slot.ISlotRepository
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSlotsByProfessionalIdUseCaseTest {
    private val repository = mockk<ISlotRepository>()
    private val useCase = GetSlotsByProfessionalIdUseCase(repository)

    private val professionalId = UUID.randomUUID()

    private fun slot(start: Int) = Slot(
        professionalId = professionalId,
        startTime = LocalDateTime.of(2026, 7, 1, start, 0),
        endTime = LocalDateTime.of(2026, 7, 1, start + 1, 0),
    )

    @Test
    fun `deve retornar slots do profissional`() {
        val slots = listOf(slot(9), slot(10), slot(11))
        every { repository.findByProfessionalId(professionalId) } returns Result.success(slots)

        val result = useCase.execute(GetSlotsByProfessionalIdUseCase.Input(professionalId = professionalId))

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().size)
        assertEquals(Slot.Status.AVAILABLE.name, result.getOrThrow()[0].status)
    }

    @Test
    fun `deve retornar lista vazia quando profissional nao tem slots`() {
        every { repository.findByProfessionalId(professionalId) } returns Result.success(emptyList())

        val result = useCase.execute(GetSlotsByProfessionalIdUseCase.Input(professionalId = professionalId))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `deve retornar falha quando repositorio falha`() {
        every { repository.findByProfessionalId(professionalId) } returns Result.failure(RuntimeException("DB error"))

        val result = useCase.execute(GetSlotsByProfessionalIdUseCase.Input(professionalId = professionalId))

        assertTrue(result.isFailure)
    }
}
