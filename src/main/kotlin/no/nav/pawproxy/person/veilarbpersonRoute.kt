package no.nav.pawproxy.person

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.handleExceptionAndRespond
import no.nav.pawproxy.token.TokenService
import no.nav.pawproxy.token.veilarbperson


fun Route.veilarbpersonRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbperson{...}") {

        val veilarbpersonBaseUrl = requireProperty("VEILARBPERSON_URL")

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbperson)
            Result.runCatching {
                httpClient.forwardGet<String>("$veilarbpersonBaseUrl$path") {
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
                    call.respondBytes(bytes = it.readBytes(), status = it.status)
                },
                onFailure = {
                    call.handleExceptionAndRespond(it, "veilarbperson", path)
                }
            )
        }
    }
}

