package com.klausner.domains

import com.klausner.domains.valueobjects.Phone
import java.util.UUID

data class Customer(
    val id: UUID,
    val name: String,
    val phone: Phone,
    val password: String? = null,
    val email: String? = null,
) : Domain
