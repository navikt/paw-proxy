package no.nav.pawproxy.app

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.features.*

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

            install(JsonFeature) {
                serializer = JacksonSerializer {
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    setSerializationInclusion(JsonInclude.Include.NON_NULL)
                }
            }

            if (setProxy) {
                engine { this.proxy = ProxyBuilder.http(requireProperty("HTTP_PROXY")) }
            }
        }
    }

}
