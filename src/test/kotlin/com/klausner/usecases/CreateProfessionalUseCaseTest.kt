package com.klausner.usecases

import com.klausner.domains.valueobjects.Phone
import com.klausner.helpers.buildProfessional
import com.klausner.infraestructure.success
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.usecases.professional.CreateProfessionalUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test

class CreateProfessionalUseCaseTest {

    private val IProfessionalRepository = mockk<IProfessionalRepository>()

    @Test
    fun `given an input when use case then create a professional`() {
        // given
        val input = buildInput()

        val professionalCreated = buildProfessional()

        coEvery { IProfessionalRepository.create(any()) } returns professionalCreated.success()

        // when
        val result = CreateProfessionalUseCase(IProfessionalRepository).execute(input)

        // then
        assert(result.isSuccess)
    }

    private fun buildInput() = CreateProfessionalUseCase.Input(
        name = "John Due",
        email = "johndue@example.com",
        phone = Phone("+55", "11", "999999999", true),
        password = "somebcryptpassword",
        bio = "love john due",
        instagram = "johndue",
        facebook = "johndue",
        photo = "s3://johndue.jpg",
    )
}
