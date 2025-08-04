package com.klausner.domains.valueobjects

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

class PhoneTest {

    @Test
    fun `given a valid phone string when create a phone object then return a phone object`() {
        val phone = Phone.fromString("5511999999999", false)

        assertEquals("55", phone.countryCode)
        assertEquals("11", phone.areaCode)
        assertEquals("999999999", phone.number)
        assertFalse(phone.isWhatsApp)
    }

    @Test
    fun `given a phone object when call toFormattedString then return a formatted string`() {
        val phone = Phone("55", "11", "999999999", false)

        assertEquals("+55 (11) 999999999", phone.toFormattedString())
    }

    @Test
    fun `given a phone object when call toSimpleFormattedString then return a simple formatted string`() {
        val phone = Phone("55", "11", "999999999", false)

        assertEquals("(11) 999999999", phone.toSimpleFormattedString())
    }
}
