package com.klausner.domains

import com.klausner.domains.valueobjects.Address
import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Phone
import java.util.UUID

data class Professional(
    val id: UUID,
    val storeId: UUID? = null,
    val name: String,
    val bio: String? = null,
    val address: Address? = null,
    val email: String? = null,
    val password: String? = null,
    val phone: Phone? = null,
    val instagram: String? = null,
    val facebook: String? = null,
    val photo: String? = null,
    val workHours: List<Interval>? = null
) : Domain
