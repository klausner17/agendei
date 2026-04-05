package com.klausner.routes

import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.professional.CreateProfessionalUseCase
import com.klausner.usecases.professional.GetAllProfessionalsUseCase
import com.klausner.usecases.professional.GetProfessionalUseCase
import com.klausner.usecases.service.CreateServiceUseCase
import com.klausner.usecases.service.GetServicesByProfessionalIdUseCase
import com.klausner.usecases.slot.BookSlotUseCase
import com.klausner.usecases.slot.CancelBookingUseCase
import com.klausner.usecases.slot.CreateSlotUseCase
import com.klausner.usecases.slot.DeleteSlotUseCase
import com.klausner.usecases.slot.GetSlotsByProfessionalIdUseCase
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
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
    val createSlotUseCase: CreateSlotUseCase by getKoin().inject()
    val getSlotsByProfessionalIdUseCase: GetSlotsByProfessionalIdUseCase by getKoin().inject()
    val deleteSlotUseCase: DeleteSlotUseCase by getKoin().inject()
    val bookSlotUseCase: BookSlotUseCase by getKoin().inject()
    val cancelBookingUseCase: CancelBookingUseCase by getKoin().inject()

    route("/professionals") {
        get {
            foldAndRespond(getAllProfessionalsUseCase.execute())
        }
        post {
            val professional = call.receive<CreateProfessionalUseCase.Input>()
            foldAndRespond(createProfessionalUseCase.execute(professional))
        }
        route("/{id}") {
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
            route("/slots") {
                get {
                    val id = UUID.fromString(call.parameters["id"]!!)
                    foldAndRespond(getSlotsByProfessionalIdUseCase.execute(GetSlotsByProfessionalIdUseCase.Input(id)))
                }
                post {
                    val id = UUID.fromString(call.parameters["id"]!!)
                    val request = call.receive<CreateSlotRequest>()
                    val input = CreateSlotUseCase.Input(
                        professionalId = id,
                        serviceId = request.serviceId,
                        startTime = request.startTime,
                        endTime = request.endTime,
                        recurrenceWeeks = request.recurrenceWeeks,
                    )
                    foldAndRespond(createSlotUseCase.execute(input))
                }
                route("/{slotId}") {
                    delete {
                        val slotId = UUID.fromString(call.parameters["slotId"]!!)
                        foldAndRespond(deleteSlotUseCase.execute(DeleteSlotUseCase.Input(slotId)))
                    }
                    patch("/book") {
                        val slotId = UUID.fromString(call.parameters["slotId"]!!)
                        val request = call.receive<BookSlotRequest>()
                        val input = BookSlotUseCase.Input(
                            slotId = slotId,
                            serviceId = request.serviceId,
                            customerName = request.customerName,
                            customerPhone = request.customerPhone,
                        )
                        foldAndRespond(bookSlotUseCase.execute(input))
                    }
                    patch("/cancel") {
                        val slotId = UUID.fromString(call.parameters["slotId"]!!)
                        foldAndRespond(cancelBookingUseCase.execute(CancelBookingUseCase.Input(slotId)))
                    }
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

data class CreateSlotRequest(
    val serviceId: UUID? = null,
    val startTime: java.time.LocalDateTime,
    val endTime: java.time.LocalDateTime,
    val recurrenceWeeks: Int? = null,
)

data class BookSlotRequest(
    val serviceId: UUID? = null,
    val customerName: String,
    val customerPhone: String? = null,
)
