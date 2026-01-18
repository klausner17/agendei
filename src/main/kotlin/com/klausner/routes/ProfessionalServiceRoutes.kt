package com.klausner.routes

import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.service.CreateServiceUseCase
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.java.KoinJavaComponent.getKoin

fun Route.professionalServiceRoutes() {
    val createServiceUseCase: CreateServiceUseCase by getKoin().inject()
    route("/professionals/{professionalId}") {
        route("/services") {
            post {
                val service = call.receive<CreateServiceUseCase.Input>()
                foldAndRespond(createServiceUseCase.execute(service))
            }
        }
    }
}
