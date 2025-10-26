package com.klausner

import com.klausner.infraestructure.json
import com.klausner.routes.config
import com.klausner.routes.professionalRoutes
import com.klausner.routes.whatsappRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun main() {
    embeddedServer(factory = Netty, 8080) {
        install(ContentNegotiation) {
            json(json)
        }
        install(StatusPages) {
            config()
        }
        routing {
            get("/hello") {
                call.respond("Hello World!")
            }
            route("/api/v1") {
                professionalRoutes()
                whatsappRoutes()
            }
        }
    }.start(wait = true)
}
