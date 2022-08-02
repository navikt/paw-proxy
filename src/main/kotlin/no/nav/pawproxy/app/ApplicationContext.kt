package no.nav.pawproxy.app

import no.nav.common.token_client.builder.AzureAdTokenClientBuilder
import no.nav.common.token_client.builder.TokenXTokenClientBuilder
import no.nav.pawproxy.health.HealthService
import no.nav.pawproxy.http.HttpClientBuilder
import no.nav.pawproxy.token.TokenService

class ApplicationContext {

    val externalHttpClient = HttpClientBuilder.build()
    val internalHttpClient = HttpClientBuilder.build(false)

    val healthService = HealthService(this)

    private val tokenXClient = TokenXTokenClientBuilder.builder().buildOnBehalfOfTokenClient()
    private val azureAdClient = AzureAdTokenClientBuilder.builder().buildOnBehalfOfTokenClient()
    val tokenService = TokenService(tokenXClient, azureAdClient)
}
