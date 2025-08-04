package com.klausner.repositories.interfaces

import java.util.UUID

fun interface Creator<T> {

    fun create(obj: T): Result<T>
}

fun interface Deleter {

    fun delete(id: UUID): Result<Unit>
}

fun interface Finder<T> {

    fun find(id: UUID): Result<T>
}

fun interface Updater<T> {

    fun update(obj: T): Result<T>
}

interface BasicCrud<T> : Creator<T>, Deleter, Finder<T>, Updater<T>
