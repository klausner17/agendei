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
import kotlin.test.assertTrue

class DeleteSlotUseCaseTest {
    private val slotRepository = mockk<ISlotRepository>()
    private val professionalRepository = mockk<IProfessionalRepository>()
    private val useCase = DeleteSlotUseCase(slotRepository, professionalRepository)

    private val userId = UUID.randomUUID()
    private val professionalId = UUID.randomUUID()
    private val slotId = UUID.randomUUID()
    private val professional = Professional(id = professionalId, userId = userId, name = "Dr. Ana")

    private val slot =
        Slot(
            id = slotId,
            professionalId = professionalId,
            startTime = LocalDateTime.of(2026, 7, 1, 9, 0),
            endTime = LocalDateTime.of(2026, 7, 1, 10, 0),
        )

    @Test
    fun `should delete slot successfully`() {
        // given
        every { slotRepository.find(slotId) } returns Result.success(slot)
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { slotRepository.delete(slotId) } returns Result.success(Unit)

        // when
        val result = useCase.execute(DeleteSlotUseCase.Input(slotId = slotId, requesterId = userId))

        // then
        assertTrue(result.isSuccess)
        verify(exactly = 1) { slotRepository.delete(slotId) }
    }

    @Test
    fun `should return failure when slot not found`() {
        // given
        every { slotRepository.find(slotId) } returns Result.failure(NoSuchElementException("Slot not found"))

        // when
        val result = useCase.execute(DeleteSlotUseCase.Input(slotId = slotId, requesterId = userId))

        // then
        assertTrue(result.isFailure)
        verify(exactly = 0) { slotRepository.delete(any()) }
    }

    @Test
    fun `should return 403 when user is not the slot owner`() {
        // given
        val outroUserId = UUID.randomUUID()
        every { slotRepository.find(slotId) } returns Result.success(slot)
        every { professionalRepository.find(professionalId) } returns Result.success(professional)

        // when
        val result = useCase.execute(DeleteSlotUseCase.Input(slotId = slotId, requesterId = outroUserId))

        // then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is SecurityException)
        verify(exactly = 0) { slotRepository.delete(any()) }
    }
}
