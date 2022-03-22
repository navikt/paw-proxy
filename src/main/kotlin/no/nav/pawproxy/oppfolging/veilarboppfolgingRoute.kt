package no.nav.pawproxy.oppfolging

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.exceptionToStatusCode
import no.nav.pawproxy.http.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarboppfolging


fun Route.veilarboppfolgingRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarboppfolging{...}") {

        val veilarboppfolgingBaseUrl = "http://veilarboppfolging.pto.svc.nais.local"

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarboppfolging)
            Result.runCatching {
                httpClient.get<String>("$veilarboppfolgingBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    header("Nav-Consumer-Id", "arbeidssokerregistrering-veileder")
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarboppfolging med path $path: $it")
                    call.respondText(it)
                },
                onFailure = {
                    logger.warn("Feil mot veilarboppfolging med path $path: ${it.message}")
                    call.respond(exceptionToStatusCode(it), it.message ?: "Uventet feil")
                }
            )
        }
    }
}

