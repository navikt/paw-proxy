package no.nav.pawproxy.arena

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.get
import no.nav.pawproxy.app.isDevelopment
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.oauth2.TokenService
import no.nav.pawproxy.oauth2.veilarbarena


fun Route.veilarbarena(httpClient: HttpClient, tokenService: TokenService) {

    route("/veilarbarena{...}") {

        val veilarbarenaBaseUrl =
            if (isDevelopment()) "https://veilarbarena-q1.nais.preprod.local" else "https://veilarbarena.nais.adeo.no"

        get {
            val path = call.request.uri

            val accessToken: String = tokenService.getAccessToken(call, veilarbarena)
            logger.info("Proxying request to url '$veilarbarenaBaseUrl$path'")
            val response = httpClient.get<String>("$veilarbarenaBaseUrl$path") {
                header("Authorization", "Bearer $accessToken")
                header("Nav-Consumer-Id", "veilarbregistrering")
            }
            logger.info("Respons fra veilarbarena med path $path: $response")
            call.respondText(response)
        }
    }
}
