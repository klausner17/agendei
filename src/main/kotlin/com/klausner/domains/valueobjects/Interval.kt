package com.klausner.domains.valueobjects

import com.klausner.domains.ValueObject
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.DayOfWeek
import java.time.LocalDateTime

@Serializable
data class Interval(
    @Contextual val startTime: LocalDateTime,
    @Contextual val endTime: LocalDateTime,
    val daysOfWeek: List<DayOfWeek>? = null,
) : ValueObject
