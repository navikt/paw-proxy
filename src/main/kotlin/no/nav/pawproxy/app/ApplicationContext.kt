package no.nav.pawproxy.app

import no.nav.pawproxy.health.HealthService
import no.nav.pawproxy.oauth2.AadOboService

class ApplicationContext {

    val externalHttpClient = HttpClientBuilder.build()
    val internalHttpClient = HttpClientBuilder.build(false)

    val healthService = HealthService(this)

    val aadOboService = AadOboService(externalHttpClient)

}
