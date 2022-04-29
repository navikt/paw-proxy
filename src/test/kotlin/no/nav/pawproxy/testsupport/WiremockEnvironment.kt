package no.nav.pawproxy.testsupport

import no.nav.pawproxy.registrering.stubVeilarbregistrering

internal class WiremockEnvironment(
    wireMockPort: Int = 8082
) {

    internal val wireMockServer = WireMockBuilder()
        .withPort(wireMockPort)
        .withAzureSupport()
        .build()
        .stubVeilarbregistrering()

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}