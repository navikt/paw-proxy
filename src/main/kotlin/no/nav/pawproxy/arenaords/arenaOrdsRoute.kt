package no.nav.pawproxy.arenaords

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.header
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.handleExceptionAndRespond

fun Route.arenaOrdsRoute(httpClient: HttpClient) {

    route("/arena/api{...}") {
        val arenaOrdsUrl = requireProperty("ARENA_ORDS_URL")

        get {
            val path = call.request.uri
            Result.runCatching {
                httpClient.forwardGet<HttpResponse>("$arenaOrdsUrl$path") {
                    call.request.header("Downstream-Authorization")?.let { header("Authorization", it) }
                }
            }.fold(
                onSuccess = {
                    call.respondBytes(status = it.status, bytes = it.readBytes())
                },
                onFailure = {
                    call.handleExceptionAndRespond(it, "ARENA_ORDS", arenaOrdsUrl)
                }
            )
        }
    }
}