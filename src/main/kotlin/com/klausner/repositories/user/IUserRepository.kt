package com.klausner.repositories.user

import com.klausner.domains.User
import com.klausner.repositories.interfaces.BasicCrud

interface IUserRepository : BasicCrud<User> {
    fun findByEmail(email: String): Result<User?>
    fun findByGoogleId(googleId: String): Result<User?>
}

