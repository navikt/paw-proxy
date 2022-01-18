package no.nav.pawproxy.app

import no.nav.pawproxy.health.HealthService

class ApplicationContext {

    val httpClient = HttpClientBuilder.build()

    val healthService = HealthService(this)
    

}
