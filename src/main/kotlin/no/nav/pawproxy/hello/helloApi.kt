package no.nav.pawproxy.hello

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.logger


fun Route.helloApi() {

    get("/is-authenticated") {
        logger.info("User is authenticated")
        call.respondText("""{ "isAuthenticated": true }""", ContentType.Application.Json)
    }
}
