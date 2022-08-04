package no.nav.pawproxy.testsupport

import com.github.tomakehurst.wiremock.WireMockServer
import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.testing.*
import no.nav.pawproxy.app.localModule
import no.nav.pawproxy.app.module
import no.nav.pawproxy.arena.veilarbarenaUrl
import no.nav.pawproxy.oppfolging.veilarboppfolgingUrl
import no.nav.pawproxy.person.veilarbpersonUrl
import no.nav.pawproxy.registrering.veilarbregistreringUrl
import no.nav.pawproxy.testsupport.azure.Azure
import no.nav.pawproxy.veileder.veilarbveilederUrl
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import java.util.concurrent.TimeUnit

internal class TestApplicationExtension: ParameterResolver {

    private fun mockEnv() {
        System.setProperty("CORS_ALLOWED_ORIGINS", "localhost")
        System.setProperty("NAIS_CLUSTER_NAME", "dev-fss")
        System.setProperty("NAIS_NAMESPACE", "paw")
        System.setProperty("HTTP_PROXY", wiremockEnvironment.wireMockServer.baseUrl())

        System.setProperty("AZURE_APP_WELL_KNOWN_URL", wiremockEnvironment.wireMockServer.getAzureV2WellKnownUrl())
        System.setProperty("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT", wiremockEnvironment.wireMockServer.getAzureV2TokenUrl())
        System.setProperty("AZURE_APP_CLIENT_ID", "paw-proxy")
        System.setProperty("AZURE_APP_CLIENT_SECRET", "testapp")
        System.setProperty("AZURE_OPENID_CONFIG_ISSUER", Azure.V2_0.getIssuer())

        System.setProperty("TOKEN_X_WELL_KNOWN_URL", wiremockEnvironment.wireMockServer.baseUrl() + "/tokenx")
        System.setProperty("TOKEN_X_CLIENT_ID", "paw-proxy")
        System.setProperty("TOKEN_X_PRIVATE_JWK", "test123")
        System.setProperty("TOKEN_X_TOKEN_ENDPOINT", wiremockEnvironment.wireMockServer.baseUrl() + "/tokenx/endpoint")
        System.setProperty("TOKEN_X_ISSUER", wiremockEnvironment.wireMockServer.baseUrl() + "/tokenx/issuer")

        System.setProperty("VEILARBREGISTRERING_URL", wiremockEnvironment.wireMockServer.veilarbregistreringUrl())
        System.setProperty("VEILARBOPPFOLGING_URL", wiremockEnvironment.wireMockServer.veilarboppfolgingUrl())
        System.setProperty("VEILARBPERSON_URL", wiremockEnvironment.wireMockServer.veilarbpersonUrl())
        System.setProperty("VEILARBARENA_URL", wiremockEnvironment.wireMockServer.veilarbarenaUrl())
        System.setProperty("VEILARBVEILEDER_URL", wiremockEnvironment.wireMockServer.veilarbveilederUrl())
    }

    private val testApplicationEngine = TestApplicationEngine(
        environment = createTestEnvironment {
            config = HoconApplicationConfig(ConfigFactory.load().withoutPath("ktor.application.modules"))
            module { localModule() }
        }
    )

    private val wiremockEnvironment = WiremockEnvironment()

    private val støttedeParametre = listOf(
        TestApplicationEngine::class.java,
        WireMockServer::class.java
    )

    init {
        wiremockEnvironment.start()
        mockEnv()
        testApplicationEngine.start(wait = true)
        Runtime.getRuntime().addShutdownHook(
            Thread {
                testApplicationEngine.stop(10, 60, TimeUnit.SECONDS)
                wiremockEnvironment.stop()
            }
        )
    }

    override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean {
        return støttedeParametre.contains(parameterContext.parameter.type)
    }

    override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
        return if (parameterContext.parameter.type == TestApplicationEngine::class.java) testApplicationEngine
            else wiremockEnvironment.wireMockServer
    }
}