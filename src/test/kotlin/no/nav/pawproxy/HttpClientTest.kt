package no.nav.pawproxy

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import kotlinx.coroutines.runBlocking
import no.nav.pawproxy.http.HttpClientBuilder
import no.nav.pawproxy.http.forwardPost
import no.nav.pawproxy.app.logger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


class HttpClientTest {

    private val test_json =
        "{\"sisteStilling\":{\"label\":\"Annen stilling\",\"styrk08\":\"-1\",\"konseptId\":-1},\"besvarelse\":{\"sisteStilling\":\"INGEN_SVAR\",\"utdanning\":\"INGEN_UTDANNING\",\"utdanningBestatt\":\"INGEN_SVAR\",\"utdanningGodkjent\":\"INGEN_SVAR\",\"dinSituasjon\":\"MISTET_JOBBEN\",\"helseHinder\":\"NEI\",\"andreForhold\":\"NEI\"},\"teksterForBesvarelse\":[{\"sporsmalId\":\"sisteStilling\",\"sporsmal\":\"Hva er din siste jobb?\",\"svar\":\"Annen stilling\"},{\"sporsmalId\":\"utdanning\",\"sporsmal\":\"Hva er din hÃ¸yeste fullfÃ¸rte utdanning?\",\"svar\":\"Ingen utdanning\"},{\"sporsmalId\":\"utdanningBestatt\",\"sporsmal\":\"Er utdanningen din bestÃ¥tt?\",\"svar\":\"Ikke aktuelt\"},{\"sporsmalId\":\"utdanningGodkjent\",\"sporsmal\":\"Er utdanningen din godkjent i Norge?\",\"svar\":\"Ikke aktuelt\"},{\"sporsmalId\":\"dinSituasjon\",\"sporsmal\":\"Velg den situasjonen som passer deg best\",\"svar\":\"Har mistet eller kommer til Ã¥ miste jobben\"},{\"sporsmalId\":\"helseHinder\",\"sporsmal\":\"Har du helseproblemer som hindrer deg i Ã¥ sÃ¸ke eller vÃ¦re i jobb?\",\"svar\":\"Nei\"},{\"sporsmalId\":\"andreForhold\",\"sporsmal\":\"Har du andre problemer med Ã¥ sÃ¸ke eller vÃ¦re i jobb?\",\"svar\":\"Nei\"}]}"

    val server = WireMockServer(wireMockConfig().dynamicPort())

    @BeforeEach
    fun setup() {
        server.start()
        configureFor("localhost", server.port())
    }

    @Test
    @Disabled
    fun `verifisere at httpClient gjør en post med json-body`() {

        stubFor(
            post("/api/posttest")
                .withRequestBody(
                    equalToJson("{\"sisteStilling\":{\"label\":\"Annen stilling\",\"styrk08\":\"-1\",\"konseptId\":-1},\"besvarelse\":{\"sisteStilling\":\"INGEN_SVAR\",\"utdanning\":\"INGEN_UTDANNING\",\"utdanningBestatt\":\"INGEN_SVAR\",\"utdanningGodkjent\":\"INGEN_SVAR\",\"dinSituasjon\":\"MISTET_JOBBEN\",\"helseHinder\":\"NEI\",\"andreForhold\":\"NEI\"},\"teksterForBesvarelse\":[{\"sporsmalId\":\"sisteStilling\",\"sporsmal\":\"Hva er din siste jobb?\",\"svar\":\"Annen stilling\"},{\"sporsmalId\":\"utdanning\",\"sporsmal\":\"Hva er din hÃ¸yeste fullfÃ¸rte utdanning?\",\"svar\":\"Ingen utdanning\"},{\"sporsmalId\":\"utdanningBestatt\",\"sporsmal\":\"Er utdanningen din bestÃ¥tt?\",\"svar\":\"Ikke aktuelt\"},{\"sporsmalId\":\"utdanningGodkjent\",\"sporsmal\":\"Er utdanningen din godkjent i Norge?\",\"svar\":\"Ikke aktuelt\"},{\"sporsmalId\":\"dinSituasjon\",\"sporsmal\":\"Velg den situasjonen som passer deg best\",\"svar\":\"Har mistet eller kommer til Ã¥ miste jobben\"},{\"sporsmalId\":\"helseHinder\",\"sporsmal\":\"Har du helseproblemer som hindrer deg i Ã¥ sÃ¸ke eller vÃ¦re i jobb?\",\"svar\":\"Nei\"},{\"sporsmalId\":\"andreForhold\",\"sporsmal\":\"Har du andre problemer med Ã¥ sÃ¸ke eller vÃ¦re i jobb?\",\"svar\":\"Nei\"}]}"))
                .willReturn(aResponse().withStatus(204))
        )

        val httpClient = HttpClientBuilder.build(false)

        runBlocking<Result<String>> {
            httpClient.forwardPost("${server.baseUrl()}/api/posttest") {
                body = test_json
            }
        }.fold(
            onSuccess = { logger.info("OK") },
            onFailure = { logger.error("Feil", it)}
        )
    }

}