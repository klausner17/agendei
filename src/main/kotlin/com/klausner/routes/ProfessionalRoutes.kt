package com.klausner.routes

import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.professional.CreateProfessionalUseCase
import com.klausner.usecases.professional.GetAllProfessionalsUseCase
import com.klausner.usecases.professional.GetProfessionalUseCase
import com.klausner.usecases.service.CreateServiceUseCase
import com.klausner.usecases.service.GetServicesByProfessionalIdUseCase
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.java.KoinJavaComponent.getKoin
import java.util.UUID

fun Route.professionalRoutes() {
    val createProfessionalUseCase: CreateProfessionalUseCase by getKoin().inject()
    val getProfessionalUseCase: GetProfessionalUseCase by getKoin().inject()
    val getAllProfessionalsUseCase: GetAllProfessionalsUseCase by getKoin().inject()
    val getMyServicesUseCase: GetServicesByProfessionalIdUseCase by getKoin().inject()
    val createServiceUseCase: CreateServiceUseCase by getKoin().inject()

    route("/professionals") {
        get {
            foldAndRespond(getAllProfessionalsUseCase.execute())
        }
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
                get {
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val result = getMyServicesUseCase.execute(GetServicesByProfessionalIdUseCase.Input(id))
                    foldAndRespond(result)
                }
                post {
                    val professionalId = UUID.fromString(call.parameters["id"]!!)
                    val createServiceRequest = call.receive<CreateServiceRequest>()
                    val input =
                        CreateServiceUseCase.Input(
                            name = createServiceRequest.name,
                            professionalId = professionalId,
                            description = createServiceRequest.description,
                            price = createServiceRequest.price,
                            durationInMinutes = createServiceRequest.durationInMinutes,
                        )
                    foldAndRespond(createServiceUseCase.execute(input))
                }
            }
        }
    }
}

data class CreateServiceRequest(
    val name: String,
    val description: String?,
    val price: Int,
    val durationInMinutes: Int,
)
