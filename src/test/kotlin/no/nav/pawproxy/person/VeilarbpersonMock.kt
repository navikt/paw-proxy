package no.nav.pawproxy.person

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.ktor.http.*

private val veilarbpersonTestPath = "/mock-veilarbperson"

private fun WireMockServer.stubVeilarbpersonGet(): WireMockServer {
    stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$veilarbpersonTestPath.*"))
            .withHeader(HttpHeaders.Authorization, WireMock.containing("Bearer "))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
            )
    )
    return this
}

internal fun WireMockServer.veilarbpersonUrl(): String = baseUrl() + veilarbpersonTestPath
internal fun WireMockServer.stubVeilarbperson(): WireMockServer = this.stubVeilarbpersonGet()