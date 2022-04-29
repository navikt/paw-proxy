package no.nav.pawproxy.testsupport

internal class WiremockEnvironment(
    wireMockPort: Int = 8082
) {

    internal val wireMockServer = WireMockBuilder()
        .withPort(wireMockPort)
        .withAzureSupport()
        .build()

    internal fun start() = this

    internal fun stop() {
        wireMockServer.stop()
    }
}