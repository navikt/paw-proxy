package no.nav.pawproxy.arena

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.ktor.http.*

private val veilarbarenaTestPath = "/mock-veilarbarena"

private fun WireMockServer.stubVeilarbarenaGet(): WireMockServer {
    stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$veilarbarenaTestPath.*"))
            .withHeader(HttpHeaders.Authorization, WireMock.containing("Bearer "))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
            )
    )
    return this
}

internal fun WireMockServer.veilarbarenaUrl(): String = baseUrl() + veilarbarenaTestPath
internal fun WireMockServer.stubVeilarbarena(): WireMockServer = this.stubVeilarbarenaGet()