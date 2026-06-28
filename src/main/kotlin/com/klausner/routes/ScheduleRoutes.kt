package com.klausner.routes

import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.schedule.CreateScheduleUseCase
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.java.KoinJavaComponent.getKoin

fun Route.scheduleRoutes() {
    val createScheduleUseCase: CreateScheduleUseCase by getKoin().inject()

    route("/schedules") {
        post {
            val input = call.receive<CreateScheduleUseCase.Input>()
            foldAndRespond(createScheduleUseCase.execute(input))
        }
    }
}
