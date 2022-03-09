package no.nav.pawproxy.person

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.exceptionToStatusCode
import no.nav.pawproxy.app.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbperson


fun Route.veilarbperson(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbperson{...}") {

        val veilarbpersonBaseUrl = "http://veilarbperson.pto.svc.nais.local"

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbperson)
            Result.runCatching {
                httpClient.get<String>("$veilarbpersonBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    header("Nav-Consumer-Id", call.request.header("Nav-Consumer-Id"))
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarbperson med path $path: $it")
                    call.respondText(it)
                },
                onFailure = {
                    logger.warn("Feil mot veilarbperson med path $path: ${it.message}")
                    call.respond(exceptionToStatusCode(it), it.message ?: "Uventet feil")
                }
            )
        }
    }
}

