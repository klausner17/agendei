package com.klausner.domains

import com.klausner.domains.valueobjects.Interval
import com.klausner.domains.valueobjects.Money
import java.util.UUID

data class Service(
    val id: UUID,
    val professionalId: UUID,
    val description: String,
    val price: Money,
    val isFree: Boolean = false,
    val slots: List<Interval>? = null,
) : Domain

/*
id: uuid
id da loja: uuid
ids dos pretadores: List<uuid (fk)>
descrição: string
valor: string
slots: list<Intervalo>
 */
