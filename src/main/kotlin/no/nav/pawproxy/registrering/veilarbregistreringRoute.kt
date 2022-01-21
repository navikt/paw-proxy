package no.nav.pawproxy.registrering

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.*
import no.nav.pawproxy.oauth2.AadOboService
import no.nav.pawproxy.oauth2.veilarbregistrering


fun Route.veilarbregistrering(httpClient: HttpClient, aadOboService: AadOboService) {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "https://veilarbregistrering.dev.intern.nav.no"

        get {
            val path = call.request.uri
            logger.info("Kall til veilarbregistrering med path: $path")

            val accessToken: String = aadOboService.getAccessToken(call, veilarbregistrering)

            logger.info("Hurra! ${accessToken}")

            val response = httpClient.get<String>("$veilarbregistreringBaseUrl$path")
            logger.info("Respons fra veilarbregistrering: $response")
            call.respondText("Hallo veilarbregistrering")
        }
    }
}


