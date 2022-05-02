package no.nav.pawproxy.veileder

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.ktor.http.*

private val veilarbveilederTestPath = "/mock-veilarbveileder"

private fun WireMockServer.stubVeilarbveilederGet(): WireMockServer {
    stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$veilarbveilederTestPath.*"))
            .withHeader(HttpHeaders.Authorization, WireMock.containing("Bearer "))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
            )
    )
    return this
}

internal fun WireMockServer.veilarbveilederUrl(): String = baseUrl() + veilarbveilederTestPath
internal fun WireMockServer.stubVeilarbveileder(): WireMockServer = this.stubVeilarbveilederGet()