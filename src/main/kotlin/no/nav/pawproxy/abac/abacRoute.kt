package no.nav.pawproxy.abac

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import no.nav.pawproxy.app.requireClusterName
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardPost
import no.nav.pawproxy.http.handleExceptionAndRespond


fun Route.abacRoute(httpClient: HttpClient) {

    route("/abac{...}") {
        val abacUrl = requireProperty("ABAC_URL")

        post {
            if (requireClusterName().contains("prod")) {
                throw IllegalStateException("Proxy mot ABAC kun tilgjengelig i dev")
            }

            val authHeader = call.request.header("Authorization") ?: call.respond(status = HttpStatusCode.Unauthorized, message = "Kall til /abac uten Authorization-header")
            val body = call.receive<JsonNode>()

            Result.runCatching {
                httpClient.forwardPost<HttpResponse>(abacUrl) {
                    header("Authorization", authHeader)
                    setBody(body)
                }
            }.fold(
                onSuccess = {
                    call.respondBytes(status = it.status, bytes = it.readBytes())
                },
                onFailure = {
                    call.handleExceptionAndRespond(it, "ABAC", abacUrl)
                }
            )
        }
    }

}