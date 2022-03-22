package no.nav.pawproxy.registrering

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.statement.HttpResponse
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.exceptionToStatusCode
import no.nav.pawproxy.http.forwardPost
import no.nav.pawproxy.http.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbregistrering


fun Route.veilarbregistreringRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "http://veilarbregistrering"

        get {
            val path = call.request.uri
            val accessToken: String = tokenService.getAccessToken(call, veilarbregistrering)

            Result.runCatching {
                httpClient.get<String>("$veilarbregistreringBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarbregistrering med path $path: $it")
                    call.respondText(it)
                },
                onFailure = {
                    logger.warn("Feil mot veilarbregistrering med path $path: ${it.message}")
                    call.respond(exceptionToStatusCode(it), it.message ?: "Uventet feil")
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
                    body = bodyFraFrontend
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarbregistrering med path $path: $it")
                    call.respond(it.status, it.readBytes().toString())
                },
                onFailure = {
                    logger.warn("Feil mot veilarbregistrering med path $path: ${it.message}")
                    call.respond(exceptionToStatusCode(it), it.message ?: "Uventet feil")
                }
            )
        }
    }
}
