package no.nav.pawproxy.veileder

import com.github.tomakehurst.wiremock.WireMockServer
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.pawproxy.testsupport.TestApplicationExtension
import no.nav.pawproxy.testsupport.azure.AzureFunctions.medAzure
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(TestApplicationExtension::class)
internal class VeilarbveilederRouteTest(private val testApplicationEngine: TestApplicationEngine, private val wireMockServer: WireMockServer) {

    @BeforeEach
    fun setup() {
        wireMockServer.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `GET-request mot veilarbveileder skal gi 200`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbveileder/test") {
                medAzure()
            }.apply {
                Assertions.assertEquals(HttpStatusCode.OK, this.response.status())
                Assertions.assertTrue(this.response.content?.contains("test: 123") ?: false)
            }
        }
    }

    @Test
    fun `request med token scopet til annen tjeneste gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbveileder/test") {
                medAzure(audience = "ikke-paw-proxy")
                addHeader(HttpHeaders.ContentType, "application/json")
            }.apply {
                Assertions.assertEquals(HttpStatusCode.Unauthorized, this.response.status())
            }
        }
    }
}