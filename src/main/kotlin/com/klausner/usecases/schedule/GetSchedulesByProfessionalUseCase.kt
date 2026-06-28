package com.klausner.usecases.schedule

import com.klausner.domains.Professional
import com.klausner.domains.Schedule
import com.klausner.infraestructure.flatMap
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.schedule.IScheduleRepository
import com.klausner.usecases.UseCase
import java.time.LocalDateTime
import java.util.UUID

class GetSchedulesByProfessionalUseCase(
    private val scheduleRepository: IScheduleRepository,
    private val professionalRepository: IProfessionalRepository,
) : UseCase<GetSchedulesByProfessionalUseCase.Input, List<GetSchedulesByProfessionalUseCase.Output>> {
    override fun execute(input: Input): Result<List<Output>> =
        professionalRepository.find(input.professionalId)
            .flatMap { professional -> checkOwnership(professional, input.requesterId) }
            .flatMap { scheduleRepository.findByProfessionalId(input.professionalId) }
            .map { schedules -> schedules.map(::domainToOutput) }

    private fun checkOwnership(
        professional: Professional,
        requesterId: UUID,
    ): Result<Professional> =
        if (professional.userId == requesterId) {
            Result.success(professional)
        } else {
            Result.failure(SecurityException("Access denied"))
        }

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
