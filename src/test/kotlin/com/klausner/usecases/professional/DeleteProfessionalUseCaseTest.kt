package com.klausner.usecases.professional

import com.klausner.repositories.professional.IProfessionalRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertTrue

class DeleteProfessionalUseCaseTest {
    private val repository = mockk<IProfessionalRepository>()
    private val useCase = DeleteProfessionalUseCase(repository)

    @Test
    fun `should delete professional successfully`() {
        val id = UUID.randomUUID()
        every { repository.delete(id) } returns Result.success(Unit)

        val result = useCase.execute(id)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { repository.delete(id) }
    }

    @Test
    fun `should return failure when repository fails to delete`() {
        val id = UUID.randomUUID()
        every { repository.delete(id) } returns Result.failure(RuntimeException("not found"))

        val result = useCase.execute(id)

        assertTrue(result.isFailure)
    }
}
