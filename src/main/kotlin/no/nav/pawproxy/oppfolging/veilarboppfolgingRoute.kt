package no.nav.pawproxy.oppfolging

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.features.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.handleExceptionAndRespond
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
                    call.handleExceptionAndRespond(it, "veilarboppfolging", path)
                }
            )
        }
    }
}

