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
import no.nav.pawproxy.http.forwardPostWithCustomContentType
import no.nav.pawproxy.http.handleExceptionAndRespond


fun Route.abacRoute(httpClient: HttpClient) {

    route("/abac") {
        val abacUrl = requireProperty("ABAC_URL")

        get {
            if (requireClusterName().contains("prod")) {
                throw IllegalStateException("Proxy mot ABAC kun tilgjengelig i dev")
            }

            val authHeader = call.request.header("Authorization")
            if (authHeader == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "GET-kall til /abac uten Authorization-header"
                )
            }
            Result.runCatching {
                httpClient.forwardGet<String>(abacUrl) {
                    header("Authorization", authHeader)
                    header("Accept-Encoding", call.request.header("Accept-Encoding"))
                    header("Accept", "*/*")
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

        post {
            if (requireClusterName().contains("prod")) {
                throw IllegalStateException("Proxy mot ABAC kun tilgjengelig i dev")
            }

            val authHeader = call.request.header("Authorization")
            if (authHeader == null) {
                call.respond(
                    status = HttpStatusCode.Unauthorized,
                    message = "POST-kall til /abac uten Authorization-header"
                )
            }

            logger.info("/abac : POST")

            val body = call.receive<String>()

            logger.info("request headers: ${call.request.headers.names()} ")

            logger.info("/abac : POST - body received: $body")

            logger.info("/abac : POST - content-type ${call.request.header("Content-Type")}")

            Result.runCatching {
                httpClient.forwardPostWithCustomContentType(abacUrl) {
                    header("Authorization", authHeader)
                    header("Content-Type", call.request.header("Content-Type"))
                    header("Accept-Encoding", call.request.header("Accept-Encoding"))
                    header("Accept", "*/*")
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