package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.domains.valueobjects.Interval
import com.klausner.repositories.professional.IProfessionalRepository
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.mockk.slot as captureSlot

class UpdateProfessionalSlotUseCaseTest {
    private val repository = mockk<IProfessionalRepository>()
    private val useCase = UpdateProfessionalSlotUseCase(repository)

    private val professionalId = UUID.randomUUID()
    private val professional = Professional(id = professionalId, name = "Ana Lima")
    private val newSlot =
        Interval(startTime = LocalDateTime.of(2026, 7, 1, 9, 0), endTime = LocalDateTime.of(2026, 7, 1, 10, 0))

    @Test
    fun `should replace professional slots with provided slots`() {
        val captured = captureSlot<Professional>()
        every { repository.find(professionalId) } returns Result.success(professional)
        every { repository.update(capture(captured)) } returns Result.success(professional)

        val result =
            useCase.execute(
                UpdateProfessionalSlotUseCase.Input(professionalId = professionalId, slot = listOf(newSlot)),
            )

        assertTrue(result.isSuccess)
        assertEquals(listOf(newSlot), captured.captured.slots)
    }

    @Test
    fun `should return failure when professional does not exist`() {
        every { repository.find(professionalId) } returns Result.failure(NoSuchElementException("not found"))

        val result =
            useCase.execute(
                UpdateProfessionalSlotUseCase.Input(professionalId = professionalId, slot = listOf(newSlot)),
            )

        assertTrue(result.isFailure)
    }
}
