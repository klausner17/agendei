package com.klausner.domains.valueobjects

import com.klausner.domains.Service
import com.klausner.domains.ValueObject
import java.time.DayOfWeek
import java.time.LocalDateTime

data class Interval(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val daysOfWeek: List<DayOfWeek>? = null,
    val service: Service? = null
) : ValueObject
