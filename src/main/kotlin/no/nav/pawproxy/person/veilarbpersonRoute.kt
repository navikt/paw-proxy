package no.nav.pawproxy.person

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.http.get
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbperson


fun Route.veilarbpersonRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbperson{...}") {

        val veilarbpersonBaseUrl = "http://veilarbperson.pto.svc.nais.local"

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbperson)
            Result.runCatching {
                httpClient.get<String>("$veilarbpersonBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    header("Nav-Consumer-Id", call.request.header("Nav-Consumer-Id"))
                    call.callId?.let {
                        header("Nav-Call-Id", it)
                    }
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarbperson med path $path: $it")
                    call.respondText(it)
                },
                onFailure = {
                    val exception = it as ResponseException
                    logger.warn("Feil mot veilarbperson med path $path: ${it.message}")
                    call.respondBytes(status = exception.response.status, bytes = exception.response.readBytes())
                }
            )
        }
    }
}

