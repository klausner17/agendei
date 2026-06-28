package com.klausner.usecases.service

import com.klausner.domains.Service
import com.klausner.repositories.service.IServiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateServiceUseCaseTest {
    private val repository = mockk<IServiceRepository>()
    private val useCase = CreateServiceUseCase(repository)

    private val professionalId = UUID.randomUUID()

    @Test
    fun `deve criar servico com sucesso`() {
        val service =
            Service(
                id = UUID.randomUUID(),
                professionalId = professionalId,
                name = "Corte",
                description = "Corte masculino",
                price = 5000,
                durationInMinutes = 30,
            )
        every { repository.create(any()) } returns Result.success(service)

        val result =
            useCase.execute(
                CreateServiceUseCase.Input(
                    name = "Corte",
                    professionalId = professionalId,
                    description = "Corte masculino",
                    price = 5000,
                    durationInMinutes = 30,
                ),
            )

        assertTrue(result.isSuccess)
        assertEquals("Corte", result.getOrThrow().name)
        assertEquals(professionalId.toString(), result.getOrThrow().professionalId)
        verify(exactly = 1) { repository.create(any()) }
    }

    @Test
    fun `deve retornar falha quando repositorio falha`() {
        every { repository.create(any()) } returns Result.failure(RuntimeException("DB error"))

        val result =
            useCase.execute(
                CreateServiceUseCase.Input(
                    name = "Corte",
                    professionalId = professionalId,
                    description = null,
                    price = 5000,
                    durationInMinutes = 30,
                ),
            )

        assertTrue(result.isFailure)
    }
}
