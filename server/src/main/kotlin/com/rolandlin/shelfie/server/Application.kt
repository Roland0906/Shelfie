package com.rolandlin.shelfie.server

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable

/** Hosting platforms pass the port in `PORT`; 8080 is for local runs. */
fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

/** Kept apart from [main] so tests can run the same setup in memory with `testApplication`. */
fun Application.module() {
    install(ContentNegotiation) { json() }
    install(CallLogging)

    routing {
        get("/health") { call.respond(HealthResponse(status = "ok")) }
    }
}

@Serializable
data class HealthResponse(val status: String)
