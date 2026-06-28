package com.klausner.usecases.slot

import com.klausner.domains.Slot
import com.klausner.repositories.slot.ISlotRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BookSlotUseCaseTest {
    private val repository = mockk<ISlotRepository>()
    private val useCase = BookSlotUseCase(repository)

    private val slotId = UUID.randomUUID()
    private val professionalId = UUID.randomUUID()
    private val availableSlot = Slot(
        id = slotId,
        professionalId = professionalId,
        startTime = LocalDateTime.of(2026, 7, 1, 9, 0),
        endTime = LocalDateTime.of(2026, 7, 1, 10, 0),
        status = Slot.Status.AVAILABLE,
    )

    @Test
    fun `deve agendar slot disponivel`() {
        val captured = slot<Slot>()
        every { repository.find(slotId) } returns Result.success(availableSlot)
        every { repository.update(capture(captured)) } returns Result.success(
            availableSlot.copy(status = Slot.Status.BOOKED, customerName = "João"),
        )

        val result = useCase.execute(
            BookSlotUseCase.Input(slotId = slotId, customerName = "João"),
        )

        assertTrue(result.isSuccess)
        assertEquals(Slot.Status.BOOKED, captured.captured.status)
        assertEquals("João", captured.captured.customerName)
    }

    @Test
    fun `deve retornar falha quando slot nao esta disponivel`() {
        val bookedSlot = availableSlot.copy(status = Slot.Status.BOOKED)
        every { repository.find(slotId) } returns Result.success(bookedSlot)

        val result = useCase.execute(
            BookSlotUseCase.Input(slotId = slotId, customerName = "João"),
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `deve salvar serviceId e telefone quando informados`() {
        val serviceId = UUID.randomUUID()
        val captured = slot<Slot>()
        every { repository.find(slotId) } returns Result.success(availableSlot)
        every { repository.update(capture(captured)) } returns Result.success(
            availableSlot.copy(status = Slot.Status.BOOKED),
        )

        useCase.execute(
            BookSlotUseCase.Input(
                slotId = slotId,
                serviceId = serviceId,
                customerName = "João",
                customerPhone = "11999999999",
            ),
        )

        assertEquals(serviceId, captured.captured.serviceId)
        assertEquals("11999999999", captured.captured.customerPhone)
    }

    @Test
    fun `deve retornar falha quando slot nao existe`() {
        every { repository.find(slotId) } returns Result.failure(NoSuchElementException("not found"))

        val result = useCase.execute(
            BookSlotUseCase.Input(slotId = slotId, customerName = "João"),
        )

        assertTrue(result.isFailure)
    }
}
