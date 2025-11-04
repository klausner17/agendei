package com.klausner.domains.valueobjects

import com.klausner.domains.ValueObject

enum class Currency(val symbol: String, val divisor: Int) { BRL("R$", 100) }

enum class MoneyType { FIXED, RANGED }

abstract class Money(
    val currency: Currency,
    val type: MoneyType,
) : ValueObject {

    companion object {

        fun fromType(type: MoneyType, amount: Int, min: Int = 0, max: Int = 0): Money {
            return when (type) {
                MoneyType.FIXED -> MoneyFixed(amount)
                MoneyType.RANGED -> MoneyRanged(min, max)
            }
        }
    }
}

data class MoneyRanged(val min: Int, val max: Int) :
    Money(currency = Currency.BRL, type = MoneyType.RANGED) {

    override fun toString(): String = with(currency) {
        return "$symbol ${min / divisor} - $symbol ${max / divisor}"
    }
}

data class MoneyFixed(val amount: Int) :
    Money(currency = Currency.BRL, type = MoneyType.FIXED) {

    override fun toString(): String = with(currency) {
        return "$symbol ${amount / divisor}"
    }
}
