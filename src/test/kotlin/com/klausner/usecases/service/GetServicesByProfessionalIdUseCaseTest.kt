package com.klausner.usecases.service

import com.klausner.domains.Service
import com.klausner.repositories.service.IServiceRepository
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetServicesByProfessionalIdUseCaseTest {
    private val repository = mockk<IServiceRepository>()
    private val useCase = GetServicesByProfessionalIdUseCase(repository)

    private val professionalId = UUID.randomUUID()

    @Test
    fun `should return services for professional`() {
        val services =
            listOf(
                Service(
                    id = UUID.randomUUID(),
                    professionalId = professionalId,
                    name = "Corte",
                    description = null,
                    price = 5000,
                    durationInMinutes = 30,
                ),
                Service(
                    id = UUID.randomUUID(),
                    professionalId = professionalId,
                    name = "Barba",
                    description = null,
                    price = 3000,
                    durationInMinutes = 20,
                ),
            )
        every { repository.findByProfessionalId(professionalId) } returns Result.success(services)

        val result = useCase.execute(GetServicesByProfessionalIdUseCase.Input(professionalId = professionalId))

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrThrow().services.size)
    }

    @Test
    fun `should return empty list when professional has no services`() {
        every { repository.findByProfessionalId(professionalId) } returns Result.success(emptyList())

        val result = useCase.execute(GetServicesByProfessionalIdUseCase.Input(professionalId = professionalId))

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().services.isEmpty())
    }

    @Test
    fun `should return failure when repository fails`() {
        every { repository.findByProfessionalId(professionalId) } returns Result.failure(RuntimeException("DB error"))

        val result = useCase.execute(GetServicesByProfessionalIdUseCase.Input(professionalId = professionalId))

        assertTrue(result.isFailure)
    }
}
