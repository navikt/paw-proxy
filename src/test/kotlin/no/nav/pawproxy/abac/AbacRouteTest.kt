package no.nav.pawproxy.abac

import com.github.tomakehurst.wiremock.WireMockServer
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.pawproxy.testsupport.TestApplicationExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApplicationExtension::class)
class AbacRouteTest(
    private val testApplicationEngine: TestApplicationEngine,
    private val wireMockServer: WireMockServer
) {

    @BeforeEach
    fun setup() {
        wireMockServer.start()
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `POST-request skal respondere med riktig statuskode fra abac i en success`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Post, "/abac") {
                addHeader(HttpHeaders.ContentType, "application/xacml+json")
                addHeader(HttpHeaders.Authorization, "Basic auth")

                setBody(this::class.java.getResource("/xacmlrequest-harTilgangTilEnhet.json").readText())
            }.apply {
                Assertions.assertEquals(HttpStatusCode.OK, this.response.status())
            }
        }
    }
}
