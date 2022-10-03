package no.nav.pawproxy.abac

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.ktor.http.*

private val abacTestPath = "/mock-abac"

private fun WireMockServer.stubAbacPost(): WireMockServer {
    stubFor(
        WireMock.post(WireMock.urlPathMatching(".*$abacTestPath.*"))
            .withHeader(HttpHeaders.ContentType, WireMock.equalTo("application/xacml+json; charset=UTF-8"))
            .withHeader(HttpHeaders.Authorization, matching(".*"))
            //.withRequestBody(equalTo(this::class.java.getResource("/xacmlrequest-harTilgangTilEnhet.json").readText()))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
            )
    )
    return this
}

internal fun WireMockServer.abacUrl(): String = baseUrl() + abacTestPath
internal fun WireMockServer.stubAbac() = this
    .stubAbacPost()
