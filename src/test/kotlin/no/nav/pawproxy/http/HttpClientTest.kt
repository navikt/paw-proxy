package no.nav.pawproxy.http

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HttpClientTest {

    private val server = WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort())

    @BeforeEach
    fun setup() {
        server.start()
        WireMock.configureFor("localhost", server.port())
    }

    @Test
    fun `verifiser at httpClient gjør en post med json-body`() {

        WireMock.stubFor(
            WireMock.post("/api/posttest")
                .willReturn(WireMock.aResponse().withStatus(204))
        )

        val httpClient = HttpClientBuilder.build(false)

        runBlocking {
            val respons = httpClient.forwardPost<HttpResponse>("${server.baseUrl()}/api/posttest") {
                body = readFile()
            }
            Assertions.assertEquals(HttpStatusCode.NoContent, respons.status)
        }
    }

    private fun readFile(): String {
        val bytes = this::class.java.classLoader.getResourceAsStream("test_registrering.json")?.readAllBytes()
            ?: throw RuntimeException("Klarte ikke lese json-fil")
        return String(bytes)
    }

}