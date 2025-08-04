package com.klausner.domains

import com.klausner.domains.valueobjects.Address
import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Phone
import java.util.UUID

data class Establishment(
    val id: UUID,
    val name: String,
    val description: String?,
    val phone: Phone,
    val address: Address,
    val instagram: String?,
    val facebook: String?,
    val whatsapp: String?,
    val logo: String,
    val businessHours: List<Interval>
) : Domain
