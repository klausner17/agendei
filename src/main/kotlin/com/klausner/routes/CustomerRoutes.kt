package com.klausner.routes

import com.klausner.infraestructure.foldAndRespond
import com.klausner.usecases.customer.CreateCustomerUseCase
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.java.KoinJavaComponent.getKoin

fun Route.customerRoutes() {
    val createCustomerUseCase: CreateCustomerUseCase by getKoin().inject()

    route("/customers") {
        post {
            val input = call.receive<CreateCustomerUseCase.Input>()
            foldAndRespond(createCustomerUseCase.execute(input))
        }
    }
}
