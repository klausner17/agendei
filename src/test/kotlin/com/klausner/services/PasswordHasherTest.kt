package com.klausner.services

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PasswordHasherTest {
    private val passwordHasher = PasswordHasher()

    @Test
    fun `should produce a hash different from the plain password`() {
        // when
        val hash = passwordHasher.hash("my-secret-password")

        // then
        assertNotEquals("my-secret-password", hash)
    }

    @Test
    fun `should verify a correct password`() {
        // given
        val hash = passwordHasher.hash("my-secret-password")

        // when
        val matches = passwordHasher.verify("my-secret-password", hash)

        // then
        assertTrue(matches)
    }

    @Test
    fun `should reject a wrong password`() {
        // given
        val hash = passwordHasher.hash("my-secret-password")

        // when
        val matches = passwordHasher.verify("wrong-password", hash)

        // then
        assertFalse(matches)
    }

    @Test
    fun `should produce different hashes for the same password`() {
        // when
        val first = passwordHasher.hash("my-secret-password")
        val second = passwordHasher.hash("my-secret-password")

        // then
        assertNotEquals(first, second)
    }

    @Test
    fun `should return false for a malformed stored hash`() {
        // when / then
        assertFalse(passwordHasher.verify("my-secret-password", "not-a-hash"))
        assertFalse(passwordHasher.verify("my-secret-password", "pbkdf2_sha256\$210000\$!!!\$@@@"))
    }
}
