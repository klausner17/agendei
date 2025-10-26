package com.klausner.routes

import com.klausner.infraestructure.foldAndRespond
import com.klausner.repositories.professional.ProfessionalRepository
import com.klausner.repositories.service.ServiceRepository
import com.klausner.usecases.professional.CreateProfessionalUseCase
import com.klausner.usecases.professional.GetProfessionalUseCase
import com.klausner.usecases.service.CreateServiceUseCase
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.professionalRoutes() {
    val professionalRepository = ProfessionalRepository()
    val serviceRepository = ServiceRepository()
    val createProfessionalUseCase = CreateProfessionalUseCase(professionalRepository)
    val getProfessionalUseCase = GetProfessionalUseCase(professionalRepository)
    val createServiceUseCase = CreateServiceUseCase(serviceRepository)

    route("/professionals") {
        post {
            val professional = call.receive<CreateProfessionalUseCase.Input>()
            foldAndRespond(createProfessionalUseCase.execute(professional))
        }
        route("/{id}/") {
            get {
                val id = UUID.fromString(call.parameters["id"]!!)
                foldAndRespond(getProfessionalUseCase.execute(GetProfessionalUseCase.Input(id)))
            }
            route("/services") {
                post {
                    val service = call.receive<CreateServiceUseCase.Input>()
                    foldAndRespond(createServiceUseCase.execute(service))
                }
            }
        }
    }
}
