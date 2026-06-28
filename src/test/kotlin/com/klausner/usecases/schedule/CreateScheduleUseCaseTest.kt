package com.klausner.usecases.schedule

import com.klausner.domains.Customer
import com.klausner.domains.Professional
import com.klausner.domains.Schedule
import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Phone
import com.klausner.repositories.customer.ICustomerRepository
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.schedule.IScheduleRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class CreateScheduleUseCaseTest {
    private val scheduleRepository = mockk<IScheduleRepository>()
    private val customerRepository = mockk<ICustomerRepository>()
    private val professionalRepository = mockk<IProfessionalRepository>()
    private val useCase = CreateScheduleUseCase(scheduleRepository, customerRepository, professionalRepository)

    private val professionalId = UUID.randomUUID()
    private val customerId = UUID.randomUUID()
    private val startTime = LocalDateTime.of(2026, 7, 1, 9, 0)
    private val endTime = LocalDateTime.of(2026, 7, 1, 10, 0)

    private val customer =
        Customer(
            id = customerId,
            name = "João",
            phone = Phone(countryCode = "55", areaCode = "11", number = "999999999", isWhatsApp = true),
        )

    private val professional =
        Professional(
            id = professionalId,
            name = "Dr. Ana",
            phone = Phone(countryCode = "55", areaCode = "11", number = "988887777", isWhatsApp = true),
            slots = emptyList(),
        )

    @Test
    fun `deve criar agendamento com sucesso`() {
        // given
        val schedule =
            Schedule(
                id = UUID.randomUUID(),
                professionalId = professionalId,
                customerId = customerId,
                observation = "primeira consulta",
                interval = Interval(startTime = startTime, endTime = endTime),
            )
        every { customerRepository.find(customerId) } returns Result.success(customer)
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { scheduleRepository.create(any()) } returns Result.success(schedule)

        // when
        val input =
            CreateScheduleUseCase.Input(
                professionalId = professionalId,
                customerId = customerId,
                startTime = startTime,
                endTime = endTime,
                observation = "primeira consulta",
            )
        val result = useCase.execute(input)

        // then
        assertTrue(result.isSuccess)
        val output = result.getOrThrow()
        assertEquals(professionalId, output.professionalId)
        assertEquals(customerId, output.customerId)
        assertEquals("primeira consulta", output.observation)
        assertEquals(Schedule.Status.CREATED.name, output.status)
        verify(exactly = 1) { scheduleRepository.create(any()) }
    }

    @Test
    fun `deve retornar falha quando cliente nao existe`() {
        // given
        every { customerRepository.find(customerId) } returns
            Result.failure(NoSuchElementException("Customer not found"))

        // when
        val input =
            CreateScheduleUseCase.Input(
                professionalId = professionalId,
                customerId = customerId,
                startTime = startTime,
                endTime = endTime,
            )
        val result = useCase.execute(input)

        // then
        assertTrue(result.isFailure)
        verify(exactly = 0) { scheduleRepository.create(any()) }
    }

    @Test
    fun `deve retornar falha quando profissional nao existe`() {
        // given
        every { customerRepository.find(customerId) } returns Result.success(customer)
        every {
            professionalRepository.find(professionalId)
        } returns Result.failure(NoSuchElementException("Professional not found"))

        // when
        val input =
            CreateScheduleUseCase.Input(
                professionalId = professionalId,
                customerId = customerId,
                startTime = startTime,
                endTime = endTime,
            )
        val result = useCase.execute(input)

        // then
        assertTrue(result.isFailure)
        verify(exactly = 0) { scheduleRepository.create(any()) }
    }

    @Test
    fun `deve criar agendamento sem observacao`() {
        // given
        val schedule =
            Schedule(
                id = UUID.randomUUID(),
                professionalId = professionalId,
                customerId = customerId,
                observation = "",
                interval = Interval(startTime = startTime, endTime = endTime),
            )
        every { customerRepository.find(customerId) } returns Result.success(customer)
        every { professionalRepository.find(professionalId) } returns Result.success(professional)
        every { scheduleRepository.create(any()) } returns Result.success(schedule)

        // when
        val input =
            CreateScheduleUseCase.Input(
                professionalId = professionalId,
                customerId = customerId,
                startTime = startTime,
                endTime = endTime,
            )
        val result = useCase.execute(input)

        // then
        assertTrue(result.isSuccess)
        assertEquals("", result.getOrThrow().observation)
    }
}
