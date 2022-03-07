package no.nav.pawproxy.registrering

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.exceptionToStatusCode
import no.nav.pawproxy.app.forwardPost
import no.nav.pawproxy.app.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbregistrering


fun Route.veilarbregistrering(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "http://veilarbregistrering"

        get {
            val path = call.request.uri
            logger.info("Fikk inn GET-kall til veilarbregistrering med path: $path")
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
            logger.info("Fikk inn POST-kall til veilarbregistrering med path: $path")
            val accessToken: String = tokenService.getAccessToken(call, veilarbregistrering)

            val bodyFraFrontend = call.receive<JsonNode>()
            logger.info("Body: $bodyFraFrontend")
            logger.info("Headers: ${call.request.headers.entries().map { "${it.key}: ${it.value}" }}")

            Result.runCatching {
                httpClient.forwardPost<String>("$veilarbregistreringBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    body = bodyFraFrontend
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarbregistrering med path $path: $it")
                    call.respondText(it.toString())
                },
                onFailure = {
                    logger.warn("Feil mot veilarbregistrering med path $path: ${it.message}")
                    call.respond(exceptionToStatusCode(it), it.message ?: "Uventet feil")
                }
            )
        }
    }
}
