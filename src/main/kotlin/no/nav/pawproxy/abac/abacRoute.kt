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
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.app.requireClusterName
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.forwardPost
import no.nav.pawproxy.http.handleExceptionAndRespond


fun Route.abacRoute(httpClient: HttpClient) {

    route("/abac") {
        val abacUrl = requireProperty("ABAC_URL")

        get {
            if (requireClusterName().contains("prod")) {
                throw IllegalStateException("Proxy mot ABAC kun tilgjengelig i dev")
            }

            logger.info("Har nådd get-endepunktet i abac-route. Videre URL: $abacUrl")

            val authHeader = call.request.header("Authorization")
            if (authHeader == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "GET-kall til /abac uten Authorization-header"
                )
            }
            logger.info("Accept-encoding-header: ${call.request.header("Accept-Encoding")}")
            Result.runCatching {
                httpClient.forwardGet<String>(abacUrl) {
                    header("Authorization", authHeader)
                    header("Accept-Encoding", call.request.header("Accept-Encoding"))
                    header("Accept", "*/*")
                }
            }.fold(
                onSuccess = {
                    logger.info("Kall til ABAC OK: $it")
                    call.respondBytes(status = it.status, bytes = it.readBytes())
                },
                onFailure = {
                    logger.warn("Kall til ABAC feilet:", it)
                    call.handleExceptionAndRespond(it, "ABAC", abacUrl)
                }
            )
        }

        post {
            if (requireClusterName().contains("prod")) {
                throw IllegalStateException("Proxy mot ABAC kun tilgjengelig i dev")
            }

            logger.info("Har nådd post-endepunktet i abac-route")

            val authHeader = call.request.header("Authorization") ?: call.respond(
                status = HttpStatusCode.Unauthorized,
                message = "POST-Kall til /abac uten Authorization-header"
            )
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