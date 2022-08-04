package no.nav.pawproxy.arena

import io.ktor.server.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.handleExceptionAndRespond
import no.nav.pawproxy.token.TokenService
import no.nav.pawproxy.token.veilarbarena


fun Route.veilarbarenaRoute(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbarena{...}") {

        val veilarbarenaBaseUrl = requireProperty("VEILARBARENA_URL")

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbarena)
            Result.runCatching {
                httpClient.forwardGet<String>("$veilarbarenaBaseUrl$path") {
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
                    call.handleExceptionAndRespond(it, "veilarbarena", path)
                }
            )
        }
    }
}

