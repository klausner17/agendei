package com.klausner.usecases.service

import com.klausner.repositories.service.IServiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteServiceUseCaseTest {
    private val repository = mockk<IServiceRepository>()
    private val useCase = DeleteServiceUseCase(repository)

    @Test
    fun `should delete service successfully`() {
        val id = UUID.randomUUID()
        every { repository.delete(id) } returns Result.success(Unit)

        val result = useCase.execute(DeleteServiceUseCase.Input(id = id))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().success)
        verify(exactly = 1) { repository.delete(id) }
    }

    @Test
    fun `should return failure when repository fails`() {
        val id = UUID.randomUUID()
        every { repository.delete(id) } returns Result.failure(RuntimeException("not found"))

        val result = useCase.execute(DeleteServiceUseCase.Input(id = id))

        assertTrue(result.isFailure)
    }
}
