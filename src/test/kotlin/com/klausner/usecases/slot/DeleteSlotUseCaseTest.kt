package com.klausner.usecases.slot

import com.klausner.repositories.slot.ISlotRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteSlotUseCaseTest {
    private val repository = mockk<ISlotRepository>()
    private val useCase = DeleteSlotUseCase(repository)

    @Test
    fun `deve deletar slot com sucesso`() {
        val id = UUID.randomUUID()
        every { repository.delete(id) } returns Result.success(Unit)

        val result = useCase.execute(DeleteSlotUseCase.Input(slotId = id))

        assertTrue(result.isSuccess)
        verify(exactly = 1) { repository.delete(id) }
    }

    @Test
    fun `deve retornar falha quando repositorio falha`() {
        val id = UUID.randomUUID()
        every { repository.delete(id) } returns Result.failure(RuntimeException("not found"))

        val result = useCase.execute(DeleteSlotUseCase.Input(slotId = id))

        assertTrue(result.isFailure)
    }
}
