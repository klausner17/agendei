package com.klausner.infraestructure

import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.professional.ProfessionalRepository
import com.klausner.repositories.service.IServiceRepository
import com.klausner.repositories.service.ServiceRepository
import com.klausner.repositories.user.IUserRepository
import com.klausner.repositories.user.UserRepository
import com.klausner.services.GoogleAuthService
import com.klausner.services.JwtService
import com.klausner.usecases.auth.GoogleAuthUseCase
import com.klausner.usecases.professional.CreateProfessionalUseCase
import com.klausner.usecases.professional.DeleteProfessionalUseCase
import com.klausner.usecases.professional.GetAllProfessionalsUseCase
import com.klausner.usecases.professional.GetProfessionalUseCase
import com.klausner.usecases.professional.UpdateProfessionalSlotUseCase
import com.klausner.usecases.professional.UpdateProfessionalUseCase
import com.klausner.usecases.service.GetServicesByProfessionalIdUseCase
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    single { objectMapper }
    single { Database.connect("jdbc:sqlite:./data.db", "org.sqlite.JDBC") }

    // services
    single {
        GoogleAuthService(
            clientId = "47481749184-lv7j2hj1v1id47jsfq8c3sf77rthjf3g.apps.googleusercontent.com"
        )
    }
    single {
        JwtService(
            secret = System.getenv("JWT_SECRET") ?: "your-super-secret-jwt-key-change-in-production"
        )
    }

    // repositories
    single { ProfessionalRepository(get()) } bind IProfessionalRepository::class
    single { ServiceRepository(get()) } bind IServiceRepository::class
    single { UserRepository(get()) } bind IUserRepository::class

    // use cases
    single { CreateProfessionalUseCase(get()) }
    single { GetProfessionalUseCase(get()) }
    single { GetAllProfessionalsUseCase(get()) }
    single { UpdateProfessionalUseCase(get()) }
    single { DeleteProfessionalUseCase(get()) }
    single { UpdateProfessionalSlotUseCase(get()) }
    single { GetServicesByProfessionalIdUseCase(get()) }
    single { GoogleAuthUseCase(get(), get(), get()) }
}
