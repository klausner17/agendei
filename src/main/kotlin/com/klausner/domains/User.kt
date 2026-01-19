package com.klausner.domains

import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val name: String,
    val picture: String? = null,
    val provider: String = "google",
    val googleId: String,
    val emailVerified: Boolean = false
) : Domain

