package com.klausner.domains.valueobjects

import com.klausner.domains.ValueObject
import kotlinx.serialization.Serializable

@Serializable
data class Phone(
    val countryCode: String,
    val areaCode: String,
    val number: String,
    val isWhatsApp: Boolean
) : ValueObject {

    fun toFormattedString(): String {
        return "+$countryCode ($areaCode) $number"
    }

    fun toSimpleFormattedString(): String {
        return "($areaCode) $number"
    }

    companion object {

        fun fromString(phone: String, isWhatsApp: Boolean): Phone {
            return Phone(
                countryCode = phone.substring(0, 2),
                areaCode = phone.substring(2, 4),
                number = phone.substring(4, phone.length),
                isWhatsApp = isWhatsApp
            )
        }
    }
}
