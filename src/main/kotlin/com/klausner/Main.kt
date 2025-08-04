package com.klausner

import com.klausner.infraestructure.serializers.LocalDateTimeSerializer
import com.klausner.infraestructure.serializers.UUIDSerializer
import com.klausner.routes.professionalRoutes
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.oauth
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sessions.Sessions
import io.ktor.server.sessions.cookie
import kotlinx.serialization.Serializable
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
        install(Sessions) {
            cookie<UserSession>("user_session")
        }
        install(Authentication) {
            oauth("auth-oauth-google") {
                urlProvider = { "http://localhost:8080" }
                providerLookup = {
                    OAuthServerSettings.OAuth2ServerSettings(
                        name = "google",
                        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = "clientId", // todo env
                        clientSecret = "clientSecret", // todo env
                        defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile"),
                        extraAuthParameters = listOf("access_type" to "offline"),
                        onStateCreated = { call, state ->
                            call.request.queryParameters["redirectUrl"]?.let { redirects[state] = it }
                        },
                    )
                }
                client = httpClient
            }
        }
        routing {
            route("/api/v1") {
                professionalRoutes()
            }
        }
    }.start(wait = true)
}

@Serializable
data class UserSession(val state: String, val token: String)
