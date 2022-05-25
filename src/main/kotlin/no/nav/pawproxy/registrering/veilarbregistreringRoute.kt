package no.nav.pawproxy.registrering

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.http.forwardPost
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbregistrering


fun Route.veilarbregistreringRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = requireProperty("VEILARBREGISTRERING_URL")

        get {
            val path = call.request.uri
            val accessToken: String = tokenService.getAccessToken(call, veilarbregistrering)

            Result.runCatching {
                httpClient.forwardGet<String>("$veilarbregistreringBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    call.callId?.let {
                        header("Nav-Call-Id", it)
                    }
                    call.request.header("Nav-Consumer-Id")?.let {
                        header("Nav-Consumer-Id", it)
                    }
                }
            }.fold(
                onSuccess = {
                    call.respondText(it)
                },
                onFailure = {
                    val exception = it as ResponseException
                    logger.warn("Feil mot veilarbregistrering med path $path: ${it.message}")
                    call.respondBytes(status = exception.response.status, bytes = exception.response.readBytes())
                }
            )
        }

        post {
            val path = call.request.uri
            val accessToken: String = tokenService.getAccessToken(call, veilarbregistrering)

            val bodyFraFrontend = call.receive<JsonNode>()

            Result.runCatching {
                httpClient.forwardPost<HttpResponse>("$veilarbregistreringBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    call.callId?.let {
                        header("Nav-Call-Id", it)
                    }
                    call.request.header("Nav-Consumer-Id")?.let {
                        header("Nav-Consumer-Id", it)
                    }
                    body = bodyFraFrontend
                }
            }.fold(
                onSuccess = {
                    call.respondBytes(bytes = it.readBytes(), status = it.status)
                },
                onFailure = {
                    val exception = it as ResponseException
                    logger.warn("Feil mot veilarbregistrering med path $path: ${it.message}")
                    call.respondBytes(status = exception.response.status, bytes = exception.response.readBytes())
                }
            )
        }
    }
}
