package com.klausner.usecases.slot

import com.klausner.domains.Slot
import com.klausner.repositories.slot.ISlotRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.slot.CreateSlotUseCase.Input
import com.klausner.usecases.slot.CreateSlotUseCase.Output
import java.time.LocalDateTime
import java.util.UUID

class CreateSlotUseCase(
    private val repository: ISlotRepository,
) : UseCase<Input, List<Output>> {

    override fun execute(input: Input): Result<List<Output>> {
        val weeks = input.recurrenceWeeks ?: 1
        val slots = (0 until weeks).map { week ->
            Slot(
                professionalId = input.professionalId,
                serviceId = input.serviceId,
                startTime = input.startTime.plusWeeks(week.toLong()),
                endTime = input.endTime.plusWeeks(week.toLong()),
            )
        }

        return if (slots.size == 1) {
            repository.create(slots.first()).map { listOf(domainToOutput(it)) }
        } else {
            repository.createAll(slots).map { it.map(::domainToOutput) }
        }
    }

    private fun domainToOutput(slot: Slot) =
        Output(
            id = slot.id,
            professionalId = slot.professionalId,
            serviceId = slot.serviceId,
            startTime = slot.startTime,
            endTime = slot.endTime,
            status = slot.status.name,
            customerName = slot.customerName,
            customerPhone = slot.customerPhone,
        )

    data class Input(
        val professionalId: UUID,
        val serviceId: UUID? = null,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val recurrenceWeeks: Int? = null,
    )

    data class Output(
        val id: UUID,
        val professionalId: UUID,
        val serviceId: UUID?,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val status: String,
        val customerName: String?,
        val customerPhone: String?,
    )
}
