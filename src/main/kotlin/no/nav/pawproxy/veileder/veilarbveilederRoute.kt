package no.nav.pawproxy.veileder

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
import no.nav.pawproxy.oauth2.veilarbveileder


fun Route.veilarbveilederRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbveileder{...}") {

        val veilarbveilederBaseUrl = requireProperty("VEILARBVEILEDER_URL")

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbveileder)
            Result.runCatching {
                httpClient.forwardGet<String>("$veilarbveilederBaseUrl$path") {
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
                    call.handleExceptionAndRespond(it, "veilarbveileder", path)
                }
            )
        }
    }
}

