package no.nav.pawproxy.registrering

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.routing.*
import no.nav.pawproxy.app.logger


fun Route.veilarbregistrering() {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "https://veilarbregistrering.dev.intern.nav.no"

        get {
            val path = call.request.uri.removePrefix("/veilarbregistrering")
            logger.info("Kall til veilarbregistrering med path: $path")
        }
    }
}