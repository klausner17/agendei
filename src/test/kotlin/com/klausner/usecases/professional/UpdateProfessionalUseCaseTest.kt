package com.klausner.usecases.professional

import com.klausner.domains.Professional
import com.klausner.repositories.professional.IProfessionalRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateProfessionalUseCaseTest {
    private val repository = mockk<IProfessionalRepository>()
    private val useCase = UpdateProfessionalUseCase(repository)

    private val existingId = UUID.randomUUID()
    private val existing = Professional(id = existingId, name = "Nome Antigo", bio = "Bio Antiga")

    @Test
    fun `deve atualizar campos informados mantendo os demais`() {
        val updated = existing.copy(name = "Nome Novo")
        val captured = slot<Professional>()
        every { repository.find(existingId) } returns Result.success(existing)
        every { repository.update(capture(captured)) } returns Result.success(updated)

        val result =
            useCase.execute(
                UpdateProfessionalUseCase.Input(id = existingId, name = "Nome Novo"),
            )

        assertTrue(result.isSuccess)
        assertEquals("Nome Novo", captured.captured.name)
        assertEquals("Bio Antiga", captured.captured.bio)
    }

    @Test
    fun `deve manter campos existentes quando input tem campos nulos`() {
        val captured = slot<Professional>()
        every { repository.find(existingId) } returns Result.success(existing)
        every { repository.update(capture(captured)) } returns Result.success(existing)

        useCase.execute(UpdateProfessionalUseCase.Input(id = existingId))

        assertEquals("Nome Antigo", captured.captured.name)
        assertEquals("Bio Antiga", captured.captured.bio)
    }

    @Test
    fun `deve retornar falha quando profissional nao existe`() {
        every { repository.find(existingId) } returns Result.failure(NoSuchElementException("not found"))

        val result = useCase.execute(UpdateProfessionalUseCase.Input(id = existingId, name = "Novo"))

        assertTrue(result.isFailure)
    }

    @Test
    fun `deve retornar falha quando update falha`() {
        every { repository.find(existingId) } returns Result.success(existing)
        every { repository.update(any()) } returns Result.failure(RuntimeException("DB error"))

        val result = useCase.execute(UpdateProfessionalUseCase.Input(id = existingId, name = "Novo"))

        assertTrue(result.isFailure)
    }
}
