package com.klausner.usecases.customer

import com.klausner.domains.Customer
import com.klausner.domains.valueobjects.Phone
import com.klausner.repositories.customer.ICustomerRepository
import com.klausner.usecases.UseCase
import java.util.UUID

class CreateCustomerUseCase(
    private val customerRepository: ICustomerRepository,
) : UseCase<CreateCustomerUseCase.Input, CreateCustomerUseCase.Output> {
    override fun execute(input: Input): Result<Output> =
        customerRepository
            .create(inputToDomain(input))
            .map(::domainToOutput)

    private fun inputToDomain(input: Input) =
        Customer(
            id = UUID.randomUUID(),
            name = input.name,
            phone =
                Phone(
                    countryCode = input.countryCode,
                    areaCode = input.areaCode,
                    number = input.phoneNumber,
                    isWhatsApp = input.isWhatsApp,
                ),
            email = input.email,
        )

    private fun domainToOutput(customer: Customer) =
        Output(
            id = customer.id,
            name = customer.name,
            phone = customer.phone,
            email = customer.email,
        )

    data class Input(
        val name: String,
        val countryCode: String,
        val areaCode: String,
        val phoneNumber: String,
        val isWhatsApp: Boolean = false,
        val email: String? = null,
    )

    data class Output(
        val id: UUID,
        val name: String,
        val phone: Phone,
        val email: String?,
    )
}
