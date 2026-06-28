package com.klausner.usecases.slot

import com.klausner.domains.Slot
import com.klausner.repositories.slot.ISlotRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CancelBookingUseCaseTest {
    private val repository = mockk<ISlotRepository>()
    private val useCase = CancelBookingUseCase(repository)

    private val slotId = UUID.randomUUID()
    private val bookedSlot =
        Slot(
            id = slotId,
            professionalId = UUID.randomUUID(),
            startTime = LocalDateTime.of(2026, 7, 1, 9, 0),
            endTime = LocalDateTime.of(2026, 7, 1, 10, 0),
            status = Slot.Status.BOOKED,
            customerName = "João",
            customerPhone = "11999999999",
        )

    @Test
    fun `should cancel booking and clear customer data`() {
        val captured = slot<Slot>()
        every { repository.find(slotId) } returns Result.success(bookedSlot)
        every { repository.update(capture(captured)) } returns
            Result.success(
                bookedSlot.copy(status = Slot.Status.AVAILABLE, customerName = null, customerPhone = null),
            )

        val result = useCase.execute(CancelBookingUseCase.Input(slotId = slotId))

        assertTrue(result.isSuccess)
        assertTrue(captured.captured.status == Slot.Status.AVAILABLE)
        assertNull(captured.captured.customerName)
        assertNull(captured.captured.customerPhone)
    }

    @Test
    fun `should return failure when slot is not booked`() {
        val availableSlot = bookedSlot.copy(status = Slot.Status.AVAILABLE)
        every { repository.find(slotId) } returns Result.success(availableSlot)

        val result = useCase.execute(CancelBookingUseCase.Input(slotId = slotId))

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `should return failure when slot does not exist`() {
        every { repository.find(slotId) } returns Result.failure(NoSuchElementException("not found"))

        val result = useCase.execute(CancelBookingUseCase.Input(slotId = slotId))

        assertTrue(result.isFailure)
    }
}
