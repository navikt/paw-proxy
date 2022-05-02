package no.nav.pawproxy.registrering

import com.github.tomakehurst.wiremock.WireMockServer
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.pawproxy.testsupport.TestApplicationExtension
import no.nav.pawproxy.testsupport.azure.AzureFunctions.medAzure
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApplicationExtension::class)
internal class VeilarbregistreringRouteTest(private val testApplicationEngine: TestApplicationEngine, private val wireMockServer: WireMockServer) {

    @BeforeEach
    fun setup() {
        wireMockServer.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `GET-request uten token skal gi 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbregistrering/test") {}.apply {
                assertEquals(HttpStatusCode.Unauthorized, this.response.status())
            }
        }
    }

    @Test
    fun `GET-request med token skal gi 200`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbregistrering/test") {
                medAzure()
            }.apply {
                assertEquals(HttpStatusCode.OK, this.response.status())
            }
        }
    }

    @Test
    fun `api-et skal respondere med riktig statuskode fra veilarbregistrering i en success`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, "/veilarbregistrering/test") {
                medAzure()
                addHeader(HttpHeaders.ContentType, "application/json")
                setBody("{}")
            }.apply {
                assertEquals(HttpStatusCode.NoContent, this.response.status())
            }
        }
    }

    @Test
    fun `api-et skal respondere med riktig statuskode fra veilarbregistrering i en failure`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, "/veilarbregistrering/test") {
                medAzure()
                setBody("{}")
            }.apply {
                assertEquals(HttpStatusCode.UnsupportedMediaType, this.response.status())
            }
        }
    }

}