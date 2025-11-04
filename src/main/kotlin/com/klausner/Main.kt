package com.klausner

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.klausner.routes.config
import com.klausner.routes.professionalRoutes
import com.klausner.routes.whatsappRoutes
import io.ktor.serialization.jackson.jackson
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
            jackson {
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                propertyNamingStrategy = PropertyNamingStrategies.LowerCamelCaseStrategy()
            }
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
