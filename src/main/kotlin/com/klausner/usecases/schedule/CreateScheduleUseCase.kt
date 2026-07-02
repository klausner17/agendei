package com.klausner.usecases.schedule

import com.klausner.domains.Professional
import com.klausner.domains.Schedule
import com.klausner.domains.valueobjects.Interval
import com.klausner.infraestructure.flatMap
import com.klausner.repositories.customer.ICustomerRepository
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.schedule.IScheduleRepository
import com.klausner.usecases.UseCase
import java.time.LocalDateTime
import java.util.UUID

class CreateScheduleUseCase(
    private val scheduleRepository: IScheduleRepository,
    private val customerRepository: ICustomerRepository,
    private val professionalRepository: IProfessionalRepository,
) : UseCase<CreateScheduleUseCase.Input, CreateScheduleUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        professionalRepository.find(input.professionalId)
            .flatMap { professional -> checkOwnership(professional, input.requesterId) }
            .flatMap { customerRepository.find(input.customerId) }
            .flatMap { scheduleRepository.create(inputToDomain(input)) }
            .map(::domainToOutput)

    private fun checkOwnership(
        professional: Professional,
        requesterId: UUID,
    ): Result<Professional> =
        if (professional.id == requesterId) {
            Result.success(professional)
        } else {
            Result.failure(SecurityException("Access denied"))
        }

    private fun inputToDomain(input: Input) =
        Schedule(
            id = UUID.randomUUID(),
            professionalId = input.professionalId,
            customerId = input.customerId,
            observation = input.observation ?: "",
            interval =
                Interval(
                    startTime = input.startTime,
                    endTime = input.endTime,
                ),
        )

    private fun domainToOutput(schedule: Schedule) =
        Output(
            id = schedule.id,
            professionalId = schedule.professionalId,
            customerId = schedule.customerId,
            observation = schedule.observation,
            startTime = schedule.interval.startTime,
            endTime = schedule.interval.endTime,
            status = schedule.status.name,
        )

    data class Input(
        val professionalId: UUID,
        val requesterId: UUID,
        val customerId: UUID,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val observation: String? = null,
    )

    data class Output(
        val id: UUID,
        val professionalId: UUID,
        val customerId: UUID,
        val observation: String,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val status: String,
    )
}
