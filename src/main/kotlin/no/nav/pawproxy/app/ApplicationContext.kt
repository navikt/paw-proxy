package no.nav.pawproxy.app

import no.nav.pawproxy.health.HealthService
import no.nav.pawproxy.oauth2.AadOboService

class ApplicationContext {

    val httpClient = HttpClientBuilder.build()

    val healthService = HealthService(this)

    val aadOboService = AadOboService(httpClient)
    

}
