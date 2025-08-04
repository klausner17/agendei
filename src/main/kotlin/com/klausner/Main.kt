package com.klausner

import com.klausner.infraestructure.serializers.LocalDateTimeSerializer
import com.klausner.infraestructure.serializers.UUIDSerializer
import com.klausner.routes.professionalRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

fun main() {
    embeddedServer(factory = Netty, 8080) {
        install(ContentNegotiation) {
            json(
                Json {
                    serializersModule = SerializersModule {
                        contextual(LocalDateTimeSerializer)
                        contextual(UUIDSerializer)
                    }
                }
            )
        }
        routing {
            get("/hello") {
                call.respond("Hello World!")
            }
            route("/api/v1") {
                professionalRoutes()
            }
        }
    }.start(wait = true)
}
