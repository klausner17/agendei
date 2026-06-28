package com.klausner.usecases.slot

import com.klausner.domains.Professional
import com.klausner.domains.Slot
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.slot.ISlotRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateSlotUseCaseTest {
    private val slotRepository = mockk<ISlotRepository>()
    private val professionalRepository = mockk<IProfessionalRepository>()
    private val useCase = CreateSlotUseCase(slotRepository, professionalRepository)

    private val userId = UUID.randomUUID()
    private val professionalId = UUID.randomUUID()
    private val startTime = LocalDateTime.of(2026, 7, 1, 9, 0)
    private val endTime = LocalDateTime.of(2026, 7, 1, 10, 0)
    private val professional = Professional(id = professionalId, userId = userId, name = "Dr. Ana")

    private fun slot(
        start: LocalDateTime = startTime,
        end: LocalDateTime = endTime,
    ) = Slot(professionalId = professionalId, startTime = start, endTime = end)

    private fun input(recurrenceWeeks: Int? = null) =
        CreateSlotUseCase.Input(
            professionalId = professionalId,
            requesterId = userId,
            startTime = startTime,
            endTime = endTime,
            recurrenceWeeks = recurrenceWeeks,
        )

    @Test
    fun `should create single slot when recurrence is not provided`() {
        // given
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.create(any()) } returns Result.success(slot())

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        verify(exactly = 1) { slotRepository.create(any()) }
        verify(exactly = 0) { slotRepository.createAll(any()) }
    }

    @Test
    fun `should create single slot when recurrence is 1`() {
        // given
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.create(any()) } returns Result.success(slot())

        // when
        val result = useCase.execute(input(recurrenceWeeks = 1))

        // then
        assertTrue(result.isSuccess)
        verify(exactly = 1) { slotRepository.create(any()) }
    }

    @Test
    fun `should create multiple slots with weekly recurrence`() {
        // given
        val slots =
            listOf(
                slot(startTime, endTime),
                slot(startTime.plusWeeks(1), endTime.plusWeeks(1)),
                slot(startTime.plusWeeks(2), endTime.plusWeeks(2)),
            )
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.createAll(any()) } returns Result.success(slots)

        // when
        val result = useCase.execute(input(recurrenceWeeks = 3))

        // then
        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().size)
        verify(exactly = 1) { slotRepository.createAll(any()) }
        verify(exactly = 0) { slotRepository.create(any()) }
    }

    @Test
    fun `should return failure when repository fails`() {
        // given
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.create(any()) } returns Result.failure(RuntimeException("DB error"))

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isFailure)
    }

    @Test
    fun `should return 403 when user is not the professional owner`() {
        // given
        val outroUserId = UUID.randomUUID()
        every { professionalRepository.find(professionalId) } returns Result.success(professional)

        // when
        val result =
            useCase.execute(
                CreateSlotUseCase.Input(
                    professionalId = professionalId,
                    requesterId = outroUserId,
                    startTime = startTime,
                    endTime = endTime,
                ),
            )

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        verify(exactly = 0) { slotRepository.create(any()) }
    }
}
