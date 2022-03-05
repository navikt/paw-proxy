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
