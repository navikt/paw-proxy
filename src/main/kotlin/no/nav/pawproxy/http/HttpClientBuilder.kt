package no.nav.pawproxy.http

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import no.nav.pawproxy.app.requireProperty

object HttpClientBuilder {

    fun build(setProxy: Boolean = true): HttpClient {
        return HttpClient(Apache) {
            install(HttpTimeout)

            Charsets {
                // Allow using `UTF_8`.
                register(Charsets.UTF_8)

                // Specify Charset to send request(if no charset in request headers).
                sendCharset = Charsets.UTF_8

                // Specify Charset to receiveee response(if no charset in response headers).
                responseCharsetFallback = Charsets.UTF_8
            }

            install(ContentNegotiation) {
                jackson()
            }

            if (setProxy) {
                engine { this.proxy = ProxyBuilder.http(requireProperty("HTTP_PROXY")) }
            }
        }
    }

}
