package no.nav.pawproxy.oppfolging

import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.pawproxy.testsupport.AzureFunctions.medAzure
import no.nav.pawproxy.testsupport.TestApplicationExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


@ExtendWith(TestApplicationExtension::class)
internal class VeilarboppfolgingRouteTest(private val testApplicationEngine: TestApplicationEngine) {

    @Test
    fun `GET-request mot veilarboppfolging skal gi 200`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarboppfolging/test") {
                medAzure()
            }.apply {
                Assertions.assertEquals(HttpStatusCode.OK, this.response.status())
            }
        }
    }

    @Test
    fun `request med token scopet til annen tjeneste gir 401`() {
        with(testApplicationEngine) {
            handleRequest(HttpMethod.Get, "/veilarboppfolging/test") {
                medAzure(audience = "ikke-paw-proxy")
                addHeader(HttpHeaders.ContentType, "application/json")
            }.apply {
                Assertions.assertEquals(HttpStatusCode.Unauthorized, this.response.status())
            }
        }
    }
}