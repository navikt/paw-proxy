package no.nav.pawproxy.registrering

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.forwardPost
import no.nav.pawproxy.http.handleExceptionAndRespond
import no.nav.pawproxy.token.TokenService
import no.nav.pawproxy.token.veilarbregistrering


fun Route.veilarbregistreringRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = requireProperty("VEILARBREGISTRERING_URL")

        get {
            logger.info("veilarbregistrering:GET")

            val path = call.request.uri

            Result.runCatching {
                val accessToken: String = tokenService.getAccessToken(call, veilarbregistrering)
                httpClient.forwardGet<String>("$veilarbregistreringBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    if (path.contains("migrering")) {
                        timeout { socketTimeoutMillis = 120000L }
                    }
                    call.callId?.let { header("Nav-Call-Id", it) }
                    call.request.header("Nav-Consumer-Id")?.let { header("Nav-Consumer-Id", it) }
                    call.request.header("x-token")?.let { header("x-token", it) }
                }
            }.fold(
                onSuccess = {
                    call.respondBytes(bytes = it.readBytes(), status = it.status)
                },
                onFailure = {
                    call.handleExceptionAndRespond(it, "veilarbregistrering", path)
                }
            )
        }

        post {
            logger.info("veilarbregistrering:POST")

            val path = call.request.uri
            val accessToken: String = tokenService.getAccessToken(call, veilarbregistrering)

            val bodyFraFrontend = call.receive<JsonNode>()

            Result.runCatching {
                httpClient.forwardPost<HttpResponse>("$veilarbregistreringBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    call.callId?.let { header("Nav-Call-Id", it) }
                    call.request.header("Nav-Consumer-Id")?.let { header("Nav-Consumer-Id", it) }
                    call.request.header("x-token")?.let { header("x-token", it) }
                    setBody(bodyFraFrontend)
                }
            }.fold(
                onSuccess = {
                    call.respondBytes(bytes = it.readBytes(), status = it.status)
                },
                onFailure = {
                    call.handleExceptionAndRespond(it, "veilarbregistrering", path)
                }
            )
        }
    }
}
