package no.nav.pawproxy.arenaords

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.contentType
import no.nav.pawproxy.app.requireProperty
import no.nav.pawproxy.http.forwardGet
import no.nav.pawproxy.http.forwardPostWithCustomContentType
import no.nav.pawproxy.http.handleExceptionAndRespond

fun Route.arenaOrdsTokenRoute(httpClient: HttpClient) {

    route("/arena/token") {
        val arenaOrdsUrl = requireProperty("ARENA_ORDS_TOKEN_URL")

        post {
            val path = "/arena/api/oauth/token"
            Result.runCatching {
                val body = call.receive<String>()
                httpClient.forwardPostWithCustomContentType("$arenaOrdsUrl$path") {
                    call.request.header("Downstream-Authorization")?.let { header("Authorization", it) }
                    call.request.header("Cache-Control")?.let { header("Cache-Control", it) }
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(body)
                }
            }.fold(
                onSuccess = {
                    call.respondBytes(status = it.status, bytes = it.readBytes())
                },
                onFailure = {
                    call.handleExceptionAndRespond(it, "ARENA_ORDS_TOKEN", arenaOrdsUrl)
                }
            )
        }
    }
}