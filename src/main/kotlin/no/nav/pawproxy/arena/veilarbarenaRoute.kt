package no.nav.pawproxy.arena

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
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbarena


fun Route.veilarbarenaRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbarena{...}") {

        val veilarbarenaBaseUrl = requireProperty("VEILARBARENA_URL")

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbarena)
            Result.runCatching {
                httpClient.forwardGet<String>("$veilarbarenaBaseUrl$path") {
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
                    val exception = it as ResponseException
                    logger.warn("Feil mot veilarbarena med path $path: ${it.message}")
                    call.respondBytes(status = exception.response.status, bytes = exception.response.readBytes())
                }
            )
        }
    }
}

