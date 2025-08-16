package com.klausner.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.whatsappRoutes() {
    route("/whatsapp") {
        post("/webhook") {
            val request = call.receive<JsonObject>()
            logger.info(request.toString())
            call.respond(HttpStatusCode.OK)
        }
    }
}

val logger: Logger = LoggerFactory.getLogger("WhatsAppRoutes")
