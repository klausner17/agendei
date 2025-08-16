package com.klausner.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.JsonObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.whatsappRoutes() {
    route("/whatsapp") {

        // Verificação do webhook (GET)
        get {
            val mode = call.request.queryParameters["hub.mode"]
            val token = call.request.queryParameters["hub.verify_token"]
            val challenge = call.request.queryParameters["hub.challenge"]

            val verifyToken = System.getenv("WHATSAPP_VERIFY_TOKEN")

            if (mode == "subscribe" && token == verifyToken && challenge != null) {
                logger.info("Token verified")
                call.respondText(challenge)
            } else {
                call.respond(HttpStatusCode.Forbidden)
            }
        }

        // Eventos (POST)
        post {
            val body = call.receiveText()
            logger.info("Evento recebido: $body")
            call.respond(HttpStatusCode.OK)
        }
    }
}

val logger: Logger = LoggerFactory.getLogger("WhatsAppRoutes")
