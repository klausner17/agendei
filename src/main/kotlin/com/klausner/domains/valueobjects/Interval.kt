package com.klausner.domains.valueobjects

import com.klausner.domains.Service
import com.klausner.domains.ValueObject
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
data class Interval(
    @Contextual val startTime: LocalDateTime,
    @Contextual val endTime: LocalDateTime,
    val daysOfWeek: List<DayOfWeek>? = null,
    val service: Service? = null
) : ValueObject
