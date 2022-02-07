package no.nav.pawproxy.registrering

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbregistrering


fun Route.veilarbregistrering(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "http://veilarbregistrering"

        get {
            val path = call.request.uri
            val accessToken: String = tokenService.getAccessToken(call, veilarbregistrering)

            val response = httpClient.get<String>("$veilarbregistreringBaseUrl$path") {
                header("Authorization", "Bearer $accessToken")
            }
            logger.info("Respons fra veilarbregistrering med path $path: $response")
            call.respondText(response)
        }
    }
}
