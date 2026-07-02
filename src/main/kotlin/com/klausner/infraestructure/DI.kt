package com.klausner.infraestructure

import com.klausner.repositories.customer.CustomerRepository
import com.klausner.repositories.customer.ICustomerRepository
import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.professional.ProfessionalRepository
import com.klausner.repositories.schedule.IScheduleRepository
import com.klausner.repositories.schedule.ScheduleRepository
import com.klausner.repositories.service.IServiceRepository
import com.klausner.repositories.service.ServiceRepository
import com.klausner.services.JwtService
import com.klausner.services.PasswordHasher
import com.klausner.usecases.auth.LoginUseCase
import com.klausner.usecases.auth.RegisterProfessionalUseCase
import com.klausner.usecases.customer.CreateCustomerUseCase
import com.klausner.usecases.professional.DeleteProfessionalUseCase
import com.klausner.usecases.professional.GetAllProfessionalsUseCase
import com.klausner.usecases.professional.GetProfessionalUseCase
import com.klausner.usecases.professional.UpdateProfessionalSlotUseCase
import com.klausner.usecases.professional.UpdateProfessionalUseCase
import com.klausner.usecases.schedule.CreateScheduleUseCase
import com.klausner.usecases.schedule.GetSchedulesByProfessionalUseCase
import com.klausner.usecases.service.CreateServiceUseCase
import com.klausner.usecases.service.DeleteServiceUseCase
import com.klausner.usecases.service.GetServicesByProfessionalIdUseCase
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule =
    module {
        single { objectMapper }
        single { Database.connect(DatabaseConfig.jdbcUrl, DatabaseConfig.DRIVER) }

        // services
        single {
            JwtService(
                secret = System.getenv("JWT_SECRET") ?: "your-super-secret-jwt-key-change-in-production",
            )
        }
        single { PasswordHasher() }

        // repositories
        single { ProfessionalRepository(get()) } bind IProfessionalRepository::class
        single { ServiceRepository(get()) } bind IServiceRepository::class
        single { CustomerRepository(get()) } bind ICustomerRepository::class
        single { ScheduleRepository(get()) } bind IScheduleRepository::class

        // use cases
        single { LoginUseCase(get(), get(), get()) }
        single { RegisterProfessionalUseCase(get(), get(), get()) }
        single { CreateCustomerUseCase(get()) }
        single { CreateScheduleUseCase(get(), get(), get()) }
        single { GetSchedulesByProfessionalUseCase(get(), get()) }
        single { GetProfessionalUseCase(get()) }
        single { GetAllProfessionalsUseCase(get()) }
        single { UpdateProfessionalUseCase(get()) }
        single { DeleteProfessionalUseCase(get()) }
        single { UpdateProfessionalSlotUseCase(get()) }
        single { GetServicesByProfessionalIdUseCase(get()) }
        single { CreateServiceUseCase(get()) }
        single { DeleteServiceUseCase(get()) }
    }
