package no.nav.pawproxy.testsupport

import com.github.tomakehurst.wiremock.client.WireMock

internal object WireMockAzureStubs {

    internal fun stubWellKnown(
        path: String,
        response: String) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*$path.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withStatus(200)
                        .withBody(response)
                )
        )
    }

    internal fun stubJwks(
        path: String,
        jwkSet: String
    ) {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathMatching(".*$path.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withStatus(200)
                        .withBody(jwkSet)
                )
        )
    }
}