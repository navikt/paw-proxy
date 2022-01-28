package no.nav.pawproxy.person

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.get
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.AadOboService

fun Route.veilarbperson(httpClient: HttpClient, aadOboService: AadOboService) {

    route("/veilarbperson{...}") {

        val veilarbpersonBaseUrl = "http://veilarbperson"

        get {
            val path = call.request.uri
            val accessToken: String = aadOboService.getAccessToken(call, no.nav.pawproxy.oauth2.veilarbperson)

            val response = httpClient.get<String>("$veilarbpersonBaseUrl$path") {
                header("Authorization", "Bearer $accessToken")
            }
            logger.info("Respons fra veilarbperson med path $path: $response")
            call.respondText(response)
        }
    }
}