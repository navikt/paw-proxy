package no.nav.pawproxy.hello

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.logger


fun Route.helloApi() {

    route("/api/test{...}") {
        get {
            logger.info("Fikk inn GET-kall til api/test")
            call.respondText("""{"ping": "GET"}""", ContentType.Application.Json)
        }

        post {
            logger.info("Fikk inn POST-kall til api/test")
            call.respond(HttpStatusCode.NoContent)
        }
    }
}
