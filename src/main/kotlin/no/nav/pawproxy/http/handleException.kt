package no.nav.pawproxy.http

import io.ktor.application.*
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.response.*
import no.nav.pawproxy.app.logger
import org.apache.http.ConnectionClosedException

suspend fun ApplicationCall.handleExceptionAndRespond(throwable: Throwable, appName: String, path: String) {
    when (throwable) {
        is SocketTimeoutException -> {
            logger.warn("Timeout mot $appName med path $path: ${throwable.message}")
            this.respond(status = HttpStatusCode.GatewayTimeout, message = throwable.message ?: "SocketTimeout mot $appName - ingen melding")
        }
        is ConnectionClosedException -> {
            this.respond(status = HttpStatusCode.GatewayTimeout, message = throwable.message ?: "SocketTimeout mot $appName - ingen melding")
        }
        else -> {
            val exception = throwable as ResponseException
            logger.warn("Feil mot $appName med path $path: ${exception.message}")
            this.respondBytes(status = exception.response.status, bytes = exception.response.readBytes())
        }
    }
}