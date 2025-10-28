package com.klausner.database.columns

import com.klausner.domains.valueobjects.Interval
import kotlinx.serialization.Serializable

@Serializable
data class Slot(
    val startTime: String,
    val endTime: String,
    val daysOfWeek: List<String>? = null,
) {

    companion object {

        fun fromDomain(interval: Interval): Slot {
            return Slot(
                startTime = interval.startTime.toString(),
                endTime = interval.endTime.toString(),
                daysOfWeek = interval.daysOfWeek?.map { it.toString() }
            )
        }
    }
}
