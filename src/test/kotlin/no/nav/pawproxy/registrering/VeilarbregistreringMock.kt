package no.nav.pawproxy.registrering

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import io.ktor.http.*

private val veilarbregistreringTestPath = "/mock-veilarbregistrering"

private fun WireMockServer.stubVeilarbregistreringPost(): WireMockServer {
    stubFor(
        WireMock.post(WireMock.urlPathMatching(".*$veilarbregistreringTestPath.*"))
            .withHeader(HttpHeaders.ContentType, WireMock.equalTo("application/json"))
            .withHeader(HttpHeaders.Authorization, WireMock.containing("Bearer "))
            .withRequestBody(equalToJson("{\"dato\": \"2022-06-24\"}"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(204)
            )
    )
    return this
}

private fun WireMockServer.stubVeilarbregistreringGet(): WireMockServer {
    stubFor(
        WireMock.get(WireMock.urlPathMatching(".*$veilarbregistreringTestPath.*"))
            .withHeader(HttpHeaders.Authorization, WireMock.containing("Bearer "))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
            )
    )
    return this
}

internal fun WireMockServer.veilarbregistreringUrl(): String = baseUrl() + veilarbregistreringTestPath
internal fun WireMockServer.stubVeilarbregistrering() = this
    .stubVeilarbregistreringPost()
    .stubVeilarbregistreringGet()
