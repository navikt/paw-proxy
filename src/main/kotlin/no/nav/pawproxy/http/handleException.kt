package no.nav.pawproxy.http

import io.ktor.server.application.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.server.response.*
import no.nav.pawproxy.app.logger
import org.apache.http.ConnectionClosedException

suspend fun ApplicationCall.handleExceptionAndRespond(throwable: Throwable, appName: String, path: String) {
    when (throwable) {
        is SocketTimeoutException -> {
            logger.warn("Timeout mot $appName med path $path: ${throwable.message}")
            this.respond(status = HttpStatusCode.GatewayTimeout, message = throwable.message ?: "SocketTimeout mot $appName - ingen melding")
        }
        is ConnectionClosedException -> {
            logger.warn("Connection closed mot $appName med path $path: ${throwable.message}")
            this.respond(status = HttpStatusCode.GatewayTimeout, message = throwable.message ?: "SocketTimeout mot $appName - ingen melding")
        }
        is ResponseException -> {
            logger.warn("Feil mot $appName med path $path: ${throwable.message}")
            this.respondBytes(status = throwable.response.status, bytes = throwable.response.readBytes())
        }
        else -> {
            logger.warn("Ukjent feil mot $appName med path $path.", throwable)
            this.respond(status = HttpStatusCode.InternalServerError, message = throwable.message ?: "Ukjent feil mot $appName - ingen melding")
        }
    }
}