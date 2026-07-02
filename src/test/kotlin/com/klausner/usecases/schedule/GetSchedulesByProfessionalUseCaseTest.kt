package com.klausner.usecases.schedule

import com.klausner.domains.Professional
import com.klausner.domains.Schedule
import com.klausner.domains.valueobjects.Interval
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.schedule.IScheduleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class GetSchedulesByProfessionalUseCaseTest {
    private val scheduleRepository = mockk<IScheduleRepository>()
    private val professionalRepository = mockk<IProfessionalRepository>()
    private val useCase = GetSchedulesByProfessionalUseCase(scheduleRepository, professionalRepository)

    private val professionalId = UUID.randomUUID()
    private val professional = Professional(id = professionalId, name = "Dr. Ana")

    private fun schedule() =
        Schedule(
            id = UUID.randomUUID(),
            professionalId = professionalId,
            customerId = UUID.randomUUID(),
            observation = "consulta",
            interval =
                Interval(
                    startTime = LocalDateTime.of(2026, 7, 1, 9, 0),
                    endTime = LocalDateTime.of(2026, 7, 1, 10, 0),
                ),
        )

    private fun input() =
        GetSchedulesByProfessionalUseCase.Input(
            professionalId = professionalId,
            requesterId = professionalId,
        )

    @Test
    fun `should return schedules for professional`() {
        // given
        val schedules = listOf(schedule(), schedule())
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { scheduleRepository.findByProfessionalId(professionalId) } returns Result.success(schedules)

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().size)
        verify(exactly = 1) { scheduleRepository.findByProfessionalId(professionalId) }
    }

    @Test
    fun `should return empty list when there are no schedules`() {
        // given
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { scheduleRepository.findByProfessionalId(professionalId) } returns Result.success(emptyList())

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().isEmpty())
    }

    @Test
    fun `should return 403 when user is not the professional owner`() {
        // given
        val outroUserId = UUID.randomUUID()
        every { professionalRepository.find(professionalId) } returns Result.success(professional)

        // when
        val result =
            useCase.execute(
                GetSchedulesByProfessionalUseCase.Input(
                    professionalId = professionalId,
                    requesterId = outroUserId,
                ),
            )

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        verify(exactly = 0) { scheduleRepository.findByProfessionalId(any()) }
    }

    @Test
    fun `should return failure when professional not found`() {
        // given
        every { professionalRepository.find(professionalId) } returns
            Result.failure(NoSuchElementException("Professional not found"))

        // when
        val result = useCase.execute(input())

        // then
        assertTrue(result.isFailure)
        verify(exactly = 0) { scheduleRepository.findByProfessionalId(any()) }
    }
}
