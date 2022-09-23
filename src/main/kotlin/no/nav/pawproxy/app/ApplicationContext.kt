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

    private val tokenXClient = TokenXTokenClientBuilder.builder().withNaisDefaults().buildOnBehalfOfTokenClient()
    private val azureAdOBOClient = AzureAdTokenClientBuilder.builder().withNaisDefaults().buildOnBehalfOfTokenClient()
    private val azureAdM2MClient = AzureAdTokenClientBuilder.builder().withNaisDefaults().buildMachineToMachineTokenClient()
    val tokenService = TokenService(tokenXClient, azureAdOBOClient, azureAdM2MClient)
}
