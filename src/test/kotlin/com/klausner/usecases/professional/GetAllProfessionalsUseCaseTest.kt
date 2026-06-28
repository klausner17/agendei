package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllProfessionalsUseCaseTest {
    private val repository = mockk<IProfessionalRepository>()
    private val useCase = GetAllProfessionalsUseCase(repository)

    @Test
    fun `should return list of professionals`() {
        val professionals =
            listOf(
                Professional(id = UUID.randomUUID(), name = "Ana Lima"),
                Professional(id = UUID.randomUUID(), name = "Carlos Silva"),
            )
        every { repository.findAll() } returns Result.success(professionals)

        val result = useCase.execute()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().professionals.size)
        assertEquals("Ana Lima", result.getOrThrow().professionals[0].name)
    }

    @Test
    fun `should return empty list when there are no professionals`() {
        every { repository.findAll() } returns Result.success(emptyList())

        val result = useCase.execute()

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().professionals.isEmpty())
    }

    @Test
    fun `should return failure when repository fails`() {
        every { repository.findAll() } returns Result.failure(RuntimeException("DB error"))

        val result = useCase.execute()

        assertTrue(result.isFailure)
    }
}
