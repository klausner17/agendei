package com.klausner.routes

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingContext
import java.util.UUID

fun RoutingContext.principalUserId(): UUID = UUID.fromString(call.principal<JWTPrincipal>()!!.payload.subject)
