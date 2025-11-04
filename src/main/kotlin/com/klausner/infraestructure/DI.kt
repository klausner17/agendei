package com.klausner.infraestructure

import com.klausner.repositories.professional.IProfessionalRepository
import com.klausner.repositories.professional.ProfessionalRepository
import com.klausner.repositories.service.IServiceRepository
import com.klausner.repositories.service.ServiceRepository
import com.klausner.usecases.professional.CreateProfessionalUseCase
import com.klausner.usecases.professional.DeleteProfessionalUseCase
import com.klausner.usecases.professional.GetProfessionalUseCase
import com.klausner.usecases.professional.UpdateProfessionalSlotUseCase
import com.klausner.usecases.professional.UpdateProfessionalUseCase
import org.jetbrains.exposed.sql.Database
import org.koin.dsl.bind
import org.koin.dsl.module

val mainModule = module {
    single { objectMapper }
    single { Database.connect("jdbc:sqlite:./data.db", "org.sqlite.JDBC") }

    // repositories
    single { ProfessionalRepository(get()) } bind IProfessionalRepository::class
    single { ServiceRepository(get()) } bind IServiceRepository::class

    // use cases
    single { CreateProfessionalUseCase(get()) }
    single { GetProfessionalUseCase(get()) }
    single { UpdateProfessionalUseCase(get()) }
    single { DeleteProfessionalUseCase(get()) }
    single { UpdateProfessionalSlotUseCase(get()) }
}
