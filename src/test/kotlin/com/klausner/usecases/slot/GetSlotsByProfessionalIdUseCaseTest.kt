package com.klausner.usecases.slot

import com.klausner.domains.Professional
import com.klausner.domains.Slot
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.slot.ISlotRepository
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetSlotsByProfessionalIdUseCaseTest {
    private val slotRepository = mockk<ISlotRepository>()
    private val professionalRepository = mockk<IProfessionalRepository>()
    private val useCase = GetSlotsByProfessionalIdUseCase(slotRepository, professionalRepository)

    private val userId = UUID.randomUUID()
    private val professionalId = UUID.randomUUID()
    private val professional = Professional(id = professionalId, userId = userId, name = "Dr. Ana")

    private fun slot(start: Int) =
        Slot(
            professionalId = professionalId,
            startTime = LocalDateTime.of(2026, 7, 1, start, 0),
            endTime = LocalDateTime.of(2026, 7, 1, start + 1, 0),
        )

    private fun input() = GetSlotsByProfessionalIdUseCase.Input(professionalId = professionalId, requesterId = userId)

    @Test
    fun `deve retornar slots do profissional`() {
        // given
        val slots = listOf(slot(9), slot(10), slot(11))
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.findByProfessionalId(professionalId) } returns Result.success(slots)

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().size)
        assertEquals(Slot.Status.AVAILABLE.name, result.getOrThrow()[0].status)
    }

    @Test
    fun `deve retornar lista vazia quando profissional nao tem slots`() {
        // given
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.findByProfessionalId(professionalId) } returns Result.success(emptyList())

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `deve retornar falha quando repositorio falha`() {
        // given
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.findByProfessionalId(professionalId) } returns
            Result.failure(RuntimeException("DB error"))

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isFailure)
    }

    @Test
    fun `deve retornar 403 quando usuario nao e dono do profissional`() {
        // given
        val outroUserId = UUID.randomUUID()
        every { professionalRepository.find(professionalId) } returns Result.success(professional)

        // when
        val result =
            useCase.execute(
                GetSlotsByProfessionalIdUseCase.Input(professionalId = professionalId, requesterId = outroUserId),
            )

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
