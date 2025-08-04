package com.klausner.usecases

fun interface UseCase<I, O> {

    fun execute(input: I): Result<O>
}

fun interface UseCaseWithoutOutput<O> {

    fun execute(): Result<O>
}
