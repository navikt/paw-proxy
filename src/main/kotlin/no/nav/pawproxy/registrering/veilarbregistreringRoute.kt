package no.nav.pawproxy.registrering

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.client
import no.nav.pawproxy.app.get
import no.nav.pawproxy.app.logger
import java.net.URI


fun Route.veilarbregistrering() {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "https://veilarbregistrering.dev.intern.nav.no"

        get {
            val path = call.request.uri.removePrefix("/veilarbregistrering")
            logger.info("Kall til veilarbregistrering med path: $path")
            call.respondText("Hallo veilarbregistrering")
            // TODO: 1. veksle inn token, 2. send kall videre til veilarbregistrering

            val response = client.get<String>("$veilarbregistreringBaseUrl$path")
            logger.info("Respons fra veilarbregistrering: $response")
        }
    }
}