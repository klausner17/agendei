package com.klausner.routes

import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.schedule.CreateScheduleUseCase
import com.klausner.usecases.schedule.GetSchedulesByProfessionalUseCase
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.java.KoinJavaComponent.getKoin
import java.util.UUID

fun Route.scheduleRoutes() {
    val createScheduleUseCase: CreateScheduleUseCase by getKoin().inject()
    val getSchedulesByProfessionalUseCase: GetSchedulesByProfessionalUseCase by getKoin().inject()

    route("/professionals/{id}/schedules") {
        get {
            val professionalId = UUID.fromString(call.parameters["id"]!!)
            val input =
                GetSchedulesByProfessionalUseCase.Input(
                    professionalId = professionalId,
                    requesterId = principalUserId(),
                )
            foldAndRespond(getSchedulesByProfessionalUseCase.execute(input))
        }
        post {
            val professionalId = UUID.fromString(call.parameters["id"]!!)
            val request = call.receive<CreateScheduleRequest>()
            val input =
                CreateScheduleUseCase.Input(
                    professionalId = professionalId,
                    requesterId = principalUserId(),
                    customerId = request.customerId,
                    startTime = request.startTime,
                    endTime = request.endTime,
                    observation = request.observation,
                )
            foldAndRespond(createScheduleUseCase.execute(input))
        }
    }
}

data class CreateScheduleRequest(
    val customerId: UUID,
    val startTime: java.time.LocalDateTime,
    val endTime: java.time.LocalDateTime,
    val observation: String? = null,
)
