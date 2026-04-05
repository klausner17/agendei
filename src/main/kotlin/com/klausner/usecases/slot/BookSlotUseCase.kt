package com.klausner.usecases.slot

import com.klausner.domains.Slot
import com.klausner.infraestructure.flatMap
import com.klausner.repositories.slot.ISlotRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.slot.BookSlotUseCase.Input
import com.klausner.usecases.slot.BookSlotUseCase.Output
import java.time.LocalDateTime
import java.util.UUID

class BookSlotUseCase(
    private val repository: ISlotRepository,
) : UseCase<Input, Output> {

    override fun execute(input: Input): Result<Output> =
        repository
            .find(input.slotId)
            .flatMap { slot ->
                if (slot.status != Slot.Status.AVAILABLE) {
                    Result.failure(IllegalStateException("Slot is not available"))
                } else {
                    repository.update(
                        slot.copy(
                            status = Slot.Status.BOOKED,
                            serviceId = input.serviceId ?: slot.serviceId,
                            customerName = input.customerName,
                            customerPhone = input.customerPhone,
                        ),
                    )
                }
            }
            .map { domainToOutput(it) }

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
        val slotId: UUID,
        val serviceId: UUID? = null,
        val customerName: String,
        val customerPhone: String? = null,
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
