package com.klausner.domains

import java.util.UUID

data class Service(
    val id: UUID,
    val professionalId: UUID,
    val description: String,
    val price: Int,
) : Domain
