package no.nav.pawproxy.registrering

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.pawproxy.testsupport.AzureFunctions.medAzure
import no.nav.pawproxy.testsupport.TestApplicationExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TestApplicationExtension::class)
class VeilarbregistreringRouteTest(private val testApplicationEngine: TestApplicationEngine) {

    @Test
    fun `request uten token skal gi 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbregistrering") {}.apply {
                assertEquals(HttpStatusCode.Unauthorized, this.response.status())
            }
        }
    }

    @Test
    fun `request med token skal gi 200`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbregistrering") {
                medAzure()
            }.apply {
                assertEquals(HttpStatusCode.OK, this.response.status())
            }
        }
    }

    @Test
    fun `api-et skal respondere med riktig statuskode fra veilarbregistrering i en success`() {

    }

    @Test
    fun `api-et skal respondere med riktig statuskode fra veilarbregistrering i en failure`() {

    }

}