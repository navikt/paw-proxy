package no.nav.pawproxy.oppfolging

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.ktor.http.*

private val veilarboppfolgingTestPath = "/mock-veilarboppfolging"

private fun WireMockServer.stubVeilarboppfolgingGet(): WireMockServer {
    stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$veilarboppfolgingTestPath.*"))
            .withHeader(HttpHeaders.Authorization, WireMock.containing("Bearer "))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
            )
    )
    return this
}

internal fun WireMockServer.veilarboppfolgingUrl(): String = baseUrl() + veilarboppfolgingTestPath
internal fun WireMockServer.stubVeilarboppfolging(): WireMockServer = this.stubVeilarboppfolgingGet()