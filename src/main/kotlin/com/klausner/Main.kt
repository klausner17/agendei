package com.klausner

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.klausner.infraestructure.DatabaseMigration
import com.klausner.infraestructure.mainModule
import com.klausner.routes.config
import com.klausner.routes.loginRoutes
import com.klausner.routes.professionalRoutes
import com.klausner.routes.whatsappRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.koin.core.context.startKoin

fun main() {
    embeddedServer(factory = Netty, 8080) {
        val koinApp =
            startKoin {
                modules(mainModule)
            }

        // Executar migrations
        val database = koinApp.koin.get<Database>()
        DatabaseMigration.runMigrations(database)
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                propertyNamingStrategy = PropertyNamingStrategies.LowerCamelCaseStrategy()
            }
        }
        install(CORS) {
            allowHost("localhost:3001")
            allowHost("localhost:3000")
            allowMethod(HttpMethod.Get)
            allowMethod(HttpMethod.Post)
            allowMethod(HttpMethod.Put)
            allowMethod(HttpMethod.Delete)
            allowMethod(HttpMethod.Patch)
            allowMethod(HttpMethod.Options)
            allowHeader(HttpHeaders.ContentType)
            allowHeader(HttpHeaders.Authorization)
        }
        install(StatusPages) { config() }
        install(Authentication) { config() }

        routing {
            loginRoutes()
            authenticate("jwt-auth") {
                route("/api/v1") {
                    professionalRoutes()
                    whatsappRoutes()
                }
            }
        }
    }.start(wait = true)
}
