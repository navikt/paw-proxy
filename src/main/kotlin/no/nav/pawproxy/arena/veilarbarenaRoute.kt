package no.nav.pawproxy.arena

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbarena


fun Route.veilarbarena(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbarena{...}") {

        val veilarbarenaBaseUrl = "http://veilarbarena.pto.svc.nais.local"

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbarena)
            Result.runCatching {
                httpClient.get<String>("$veilarbarenaBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    header("Nav-Consumer-Id", "veilarbregistrering")
                }
            }.fold(
                onSuccess = {
                    call.respondText(it)
                        .also { response -> logger.info("Respons fra veilarbarena med path $path: $response") }
                },
                onFailure = {
                    call.respond(exceptionToStatusCode(it), it.message ?: "Uventet feil")
                }
            )
        }
    }
}

fun exceptionToStatusCode(e: Throwable): HttpStatusCode =
    when (e) {
        is RedirectResponseException -> e.response.status
        is ClientRequestException -> e.response.status
        is ServerResponseException -> e.response.status
        else -> HttpStatusCode.InternalServerError
    }
