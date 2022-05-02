package no.nav.pawproxy.person

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.pawproxy.testsupport.TestApplicationExtension
import no.nav.pawproxy.testsupport.azure.AzureFunctions.medAzure
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(TestApplicationExtension::class)
internal class VeilarbpersonRouteTest(private val testApplicationEngine: TestApplicationEngine) {

    @Test
    fun `GET-request mot veilarbperson skal gi 200`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarbperson/test") {
                medAzure()
            }.apply {
                Assertions.assertEquals(HttpStatusCode.OK, this.response.status())
            }
        }
    }


    @Test
    fun `request med token fra en feil audience gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "veilarbperson/test") {
                medAzure(audience = "ikke-paw-proxy")
            }.apply {
                Assertions.assertEquals(HttpStatusCode.Unauthorized, this.response.status())
            }
        }
    }
}