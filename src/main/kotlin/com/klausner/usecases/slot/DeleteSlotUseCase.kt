package com.klausner.usecases.slot

import com.klausner.repositories.slot.ISlotRepository
import com.klausner.usecases.UseCase
import com.klausner.usecases.slot.DeleteSlotUseCase.Input
import java.util.UUID

class DeleteSlotUseCase(
    private val repository: ISlotRepository,
) : UseCase<Input, Unit> {

    override fun execute(input: Input): Result<Unit> =
        repository.delete(input.slotId)

    data class Input(val slotId: UUID)
}
