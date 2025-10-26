package com.klausner.domains

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Service(
    @Contextual val id: UUID,
    @Contextual val professionalId: UUID,
    val description: String,
    @Contextual val price: Int,
) : Domain
