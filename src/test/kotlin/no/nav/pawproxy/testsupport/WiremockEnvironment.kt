package no.nav.pawproxy.testsupport

import no.nav.pawproxy.oppfolging.stubVeilarboppfolging
import no.nav.pawproxy.person.stubVeilarbperson
import no.nav.pawproxy.registrering.stubVeilarbregistrering
import no.nav.pawproxy.veileder.stubVeilarbveileder

internal class WiremockEnvironment(
    wireMockPort: Int = 8082
) {

    internal val wireMockServer = WireMockBuilder()
        .withPort(wireMockPort)
        .withAzureSupport()
        .build()
        .stubVeilarbregistrering()
        .stubVeilarboppfolging()
        .stubVeilarbperson()
        .stubVeilarbveileder()

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}