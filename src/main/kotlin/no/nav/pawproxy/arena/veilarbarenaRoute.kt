package no.nav.pawproxy.arena

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.exceptionToStatusCode
import no.nav.pawproxy.http.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbarena


fun Route.veilarbarenaRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbarena{...}") {

        val veilarbarenaBaseUrl = "http://veilarbarena.pto.svc.nais.local"

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbarena)
            Result.runCatching {
                httpClient.get<String>("$veilarbarenaBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    header("Nav-Consumer-Id", "veilarbregistrering")
                    call.callId?.let {
                        header("Nav-Call-Id", it)
                    }
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarbarena med path $path: $it")
                    call.respondText(it)
                },
                onFailure = {
                    logger.warn("Feil mot veilarbarena med path $path: ${it.message}")
                    call.respond(exceptionToStatusCode(it), it.message ?: "Uventet feil")
                }
            )
        }
    }
}

