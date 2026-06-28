package com.klausner.usecases.slot

import com.klausner.domains.Slot
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
    private val repository = mockk<ISlotRepository>()
    private val useCase = CreateSlotUseCase(repository)

    private val professionalId = UUID.randomUUID()
    private val startTime = LocalDateTime.of(2026, 7, 1, 9, 0)
    private val endTime = LocalDateTime.of(2026, 7, 1, 10, 0)

    private fun slot(
        start: LocalDateTime = startTime,
        end: LocalDateTime = endTime,
    ) = Slot(professionalId = professionalId, startTime = start, endTime = end)

    @Test
    fun `deve criar slot unico quando recorrencia nao informada`() {
        every { repository.create(any()) } returns Result.success(slot())

        val result =
            useCase.execute(
                CreateSlotUseCase.Input(
                    professionalId = professionalId,
                    startTime = startTime,
                    endTime = endTime,
                ),
            )

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrThrow().size)
        verify(exactly = 1) { repository.create(any()) }
        verify(exactly = 0) { repository.createAll(any()) }
    }

    @Test
    fun `deve criar slot unico quando recorrencia igual a 1`() {
        every { repository.create(any()) } returns Result.success(slot())

        val result =
            useCase.execute(
                CreateSlotUseCase.Input(
                    professionalId = professionalId,
                    startTime = startTime,
                    endTime = endTime,
                    recurrenceWeeks = 1,
                ),
            )

        assertTrue(result.isSuccess)
        verify(exactly = 1) { repository.create(any()) }
    }

    @Test
    fun `deve criar multiplos slots com recorrencia semanal`() {
        val slots =
            listOf(
                slot(startTime, endTime),
                slot(startTime.plusWeeks(1), endTime.plusWeeks(1)),
                slot(startTime.plusWeeks(2), endTime.plusWeeks(2)),
            )
        every { repository.createAll(any()) } returns Result.success(slots)

        val result =
            useCase.execute(
                CreateSlotUseCase.Input(
                    professionalId = professionalId,
                    startTime = startTime,
                    endTime = endTime,
                    recurrenceWeeks = 3,
                ),
            )

        assertTrue(result.isSuccess)
        assertEquals(3, result.getOrThrow().size)
        verify(exactly = 1) { repository.createAll(any()) }
        verify(exactly = 0) { repository.create(any()) }
    }

    @Test
    fun `deve retornar falha quando repositorio falha`() {
        every { repository.create(any()) } returns Result.failure(RuntimeException("DB error"))

        val result =
            useCase.execute(
                CreateSlotUseCase.Input(
                    professionalId = professionalId,
                    startTime = startTime,
                    endTime = endTime,
                ),
            )

        assertTrue(result.isFailure)
    }
}
