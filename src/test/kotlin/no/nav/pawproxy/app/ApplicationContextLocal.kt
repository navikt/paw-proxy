package no.nav.pawproxy.app

import io.mockk.every
import io.mockk.mockk
import no.nav.common.token_client.client.AzureAdMachineToMachineTokenClient
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.common.token_client.client.TokenXOnBehalfOfTokenClient
import no.nav.pawproxy.health.HealthService
import no.nav.pawproxy.http.HttpClientBuilder
import no.nav.pawproxy.token.TokenService

class ApplicationContextLocal {

    val externalHttpClient = HttpClientBuilder.build()
    val internalHttpClient = HttpClientBuilder.build(false)

    val healthService = mockk<HealthService>(relaxed = true)

    private val tokenXClientMock = mockk<TokenXOnBehalfOfTokenClient>(relaxed = true)
    private val azureAdClientMock = mockAzureAdClient()
    private val azureAdM2MClientMock = mockAzureAdM2MClient()

    val tokenService = TokenService(tokenXClientMock, azureAdClientMock, azureAdM2MClientMock)

    private fun mockAzureAdClient(): AzureAdOnBehalfOfTokenClient {
        val azureAdClientMock = mockk<AzureAdOnBehalfOfTokenClient>(relaxed = true)
        every { azureAdClientMock.exchangeOnBehalfOfToken(any(), any()) } returnsArgument 1
        return azureAdClientMock
    }

    private fun mockAzureAdM2MClient(): AzureAdMachineToMachineTokenClient {
        val azureAdClientMock = mockk<AzureAdMachineToMachineTokenClient>(relaxed = true)
        every { azureAdClientMock.createMachineToMachineToken(any()) } returnsArgument 1
        return azureAdClientMock
    }
}