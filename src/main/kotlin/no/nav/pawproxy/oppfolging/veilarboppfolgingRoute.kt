package no.nav.pawproxy.oppfolging

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
import no.nav.pawproxy.oauth2.veilarboppfolging


fun Route.veilarboppfolgingRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarboppfolging{...}") {

        val veilarboppfolgingBaseUrl = requireProperty("VEILARBOPPFOLGING_URL")

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarboppfolging)
            Result.runCatching {
                httpClient.forwardGet<String>("$veilarboppfolgingBaseUrl$path") {
                    header("Authorization", "Bearer $accessToken")
                    header("Nav-Consumer-Id", "arbeidssokerregistrering-veileder")
                    call.callId?.let {
                        header("Nav-Call-Id", it)
                    }
                }
            }.fold(
                onSuccess = {
                    logger.info("Respons fra veilarboppfolging med path $path: $it")
                    call.respondText(it)
                },
                onFailure = {
                    val exception = it as ResponseException
                    logger.warn("Feil mot veilarboppfolging med path $path: ${it.message}")
                    call.respondBytes(status = exception.response.status, bytes = exception.response.readBytes())
                }
            )
        }
    }
}

