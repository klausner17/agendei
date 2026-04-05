package com.klausner.usecases.slot

import com.klausner.domains.Slot
import com.klausner.infraestructure.flatMap
import com.klausner.repositories.slot.ISlotRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.slot.CancelBookingUseCase.Input
import java.util.UUID

class CancelBookingUseCase(
    private val repository: ISlotRepository,
) : UseCase<Input, Unit> {

    override fun execute(input: Input): Result<Unit> =
        repository
            .find(input.slotId)
            .flatMap { slot ->
                if (slot.status != Slot.Status.BOOKED) {
                    Result.failure(IllegalStateException("Slot is not booked"))
                } else {
                    repository.update(
                        slot.copy(
                            status = Slot.Status.AVAILABLE,
                            customerName = null,
                            customerPhone = null,
                        ),
                    )
                }
            }
            .map { }

    data class Input(val slotId: UUID)
}
