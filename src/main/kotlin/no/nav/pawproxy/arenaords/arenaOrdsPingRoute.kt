package no.nav.pawproxy.arenaords

import io.ktor.client.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.handleExceptionAndRespond

fun Route.arenaOrdsPingRoute(httpClient: HttpClient) {

    route("/arena/ping") {
        val arenaOrdsUrl = requireProperty("ARENA_ORDS_URL")

        get {
            val pingPath = "/arena/api/v1/test/ping"
            Result.runCatching {
                httpClient.forwardGet<HttpResponse>("$arenaOrdsUrl$pingPath") {}
            }.fold(
                onSuccess = {
                    call.respondBytes(status = it.status, bytes = it.readBytes())
                },
                onFailure = {
                    call.handleExceptionAndRespond(it, "ARENA_ORDS_PING", arenaOrdsUrl)
                }
            )
        }
    }
}