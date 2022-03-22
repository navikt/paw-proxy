package no.nav.pawproxy.app

import no.nav.pawproxy.health.HealthService
import no.nav.pawproxy.http.HttpClientBuilder
import no.nav.pawproxy.oauth2.TokenService

class ApplicationContext {

    val externalHttpClient = HttpClientBuilder.build()
    val internalHttpClient = HttpClientBuilder.build(false)

    val healthService = HealthService(this)

    val tokenService = TokenService(externalHttpClient)

}
