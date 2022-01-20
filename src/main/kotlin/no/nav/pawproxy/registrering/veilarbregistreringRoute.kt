package no.nav.pawproxy.registrering

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger("no.nav.pawproxy.registrering.veilarbregistreringRoute")


fun Route.veilarbregistrering() {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "https://veilarbregistrering.dev.intern.nav.no"

        get {
            val path = call.request.uri.removePrefix("/veilarbregistrering")
            LOG.info("Kall til veilarbregistrering med path: $path")
            call.respondText("Hallo veilarbregistrering")
            // TODO: 1. veksle inn token, 2. send kall videre til veilarbregistrering
        }
    }
}