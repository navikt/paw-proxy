package no.nav.pawproxy.arena

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.pawproxy.testsupport.TestApplicationExtension
import no.nav.pawproxy.testsupport.azure.AzureFunctions.medAzure
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApplicationExtension::class)
internal class VeilarbarenaRouteTest(private val testApplicationEngine: TestApplicationEngine) {

    @Test
    fun `GET-request mot veilarbarena skal gi 200`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbarena/test") {
                medAzure()
            }.apply {
                Assertions.assertEquals(HttpStatusCode.OK, this.response.status())
            }
        }
    }

    @Test
    fun `GET-request mot veilarbarena uten autentisering skal gi 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbarena/test") {}.apply {
                Assertions.assertEquals(HttpStatusCode.Unauthorized, this.response.status())
            }
        }
    }

}