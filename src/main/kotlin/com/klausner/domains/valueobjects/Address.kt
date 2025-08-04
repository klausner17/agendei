package com.klausner.domains.valueobjects

import com.klausner.domains.ValueObject
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val street: String?,
    val number: String?,
    val complement: String?,
    val neighborhood: String?,
    val city: String,
    val state: String,
    val country: String,
    val zipCode: String
) : ValueObject {

    data class PossibleAddress(
        val street: String?,
        val number: String?,
        val complement: String?,
        val neighborhood: String?,
        val city: String?,
        val state: String?,
        val country: String?,
        val zipCode: String?
    ) {

        fun toAddressOrNull() = takeIf { city != null && state != null && country != null && zipCode != null }
            ?.let { Address(street, number, complement, neighborhood, city!!, state!!, country!!, zipCode!!) }
    }

}
