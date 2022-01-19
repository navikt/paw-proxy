package no.nav.pawproxy.app

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.*

object HttpClientBuilder {

    fun build(): HttpClient {
        return HttpClient(Apache) {
            install(HttpTimeout)
        }
    }

}
