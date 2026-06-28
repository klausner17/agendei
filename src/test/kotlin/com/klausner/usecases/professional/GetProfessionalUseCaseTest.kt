package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetProfessionalUseCaseTest {
    private val repository = mockk<IProfessionalRepository>()
    private val useCase = GetProfessionalUseCase(repository)

    @Test
    fun `deve retornar profissional pelo id`() {
        val id = UUID.randomUUID()
        val professional = Professional(id = id, name = "Ana Lima", bio = "Cabeleireira")
        every { repository.find(id) } returns Result.success(professional)

        val result = useCase.execute(GetProfessionalUseCase.Input(professionalId = id))

        assertTrue(result.isSuccess)
        val output = result.getOrThrow()
        assertEquals(id, output.id)
        assertEquals("Ana Lima", output.name)
        assertEquals("Cabeleireira", output.bio)
    }

    @Test
    fun `deve retornar falha quando profissional nao existe`() {
        val id = UUID.randomUUID()
        every { repository.find(id) } returns Result.failure(NoSuchElementException("not found"))

        val result = useCase.execute(GetProfessionalUseCase.Input(professionalId = id))

        assertTrue(result.isFailure)
    }
}
