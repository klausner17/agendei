package com.klausner.usecases.customer

import com.klausner.domains.Customer
import com.klausner.domains.valueobjects.Phone
import com.klausner.repositories.customer.ICustomerRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class CreateCustomerUseCaseTest {
    private val customerRepository = mockk<ICustomerRepository>()
    private val useCase = CreateCustomerUseCase(customerRepository)

    @Test
    fun `deve criar cliente com sucesso`() {
        // given
        val customerId = UUID.randomUUID()
        val customer =
            Customer(
                id = customerId,
                name = "João Silva",
                phone =
                    Phone(
                        countryCode = "55",
                        areaCode = "11",
                        number = "999999999",
                        isWhatsApp = true,
                    ),
                email = "joao@example.com",
            )
        every { customerRepository.create(any()) } returns Result.success(customer)

        // when
        val input =
            CreateCustomerUseCase.Input(
                name = "João Silva",
                countryCode = "55",
                areaCode = "11",
                phoneNumber = "999999999",
                isWhatsApp = true,
                email = "joao@example.com",
            )
        val result = useCase.execute(input)

        // then
        assertTrue(result.isSuccess)
        val output = result.getOrThrow()
        assertEquals("João Silva", output.name)
        assertEquals("joao@example.com", output.email)
        verify(exactly = 1) { customerRepository.create(any()) }
    }

    @Test
    fun `deve criar cliente sem email`() {
        // given
        val customer =
            Customer(
                id = UUID.randomUUID(),
                name = "Maria",
                phone = Phone(countryCode = "55", areaCode = "21", number = "988887777", isWhatsApp = false),
                email = null,
            )
        every { customerRepository.create(any()) } returns Result.success(customer)

        // when
        val input =
            CreateCustomerUseCase.Input(
                name = "Maria",
                countryCode = "55",
                areaCode = "21",
                phoneNumber = "988887777",
            )
        val result = useCase.execute(input)

        // then
        assertTrue(result.isSuccess)
        assertEquals(null, result.getOrThrow().email)
    }

    @Test
    fun `deve retornar falha quando repositorio falha`() {
        // given
        every { customerRepository.create(any()) } returns Result.failure(RuntimeException("DB error"))

        // when
        val input =
            CreateCustomerUseCase.Input(
                name = "Teste",
                countryCode = "55",
                areaCode = "11",
                phoneNumber = "999999999",
            )
        val result = useCase.execute(input)

        // then
        assertTrue(result.isFailure)
    }
}
