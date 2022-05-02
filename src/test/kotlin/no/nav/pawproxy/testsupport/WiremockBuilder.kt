package no.nav.pawproxy.testsupport

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

class WireMockBuilder {

    private companion object {
        private const val AZURE_V2_TOKEN_TRANSFORMER = "azure-v2-token"
    }

    private val config = WireMockConfiguration.options()
    private var port: Int? = null
    private var serverFunction : ((wireMockServer: WireMockServer) -> Unit)? = null
    private var configFunction : ((wireMockConfiguration: WireMockConfiguration) -> Unit)? = null
    private var withAzureSupport = false

    fun withPort(port: Int) : WireMockBuilder {
        this.port = port
        return this
    }

    fun withAzureSupport() : WireMockBuilder {
        val azureV2 = AzureTokenResponseTransformer(name = AZURE_V2_TOKEN_TRANSFORMER, issuer = Azure.V2_0.getIssuer())
        config.extensions(azureV2)
        withAzureSupport = true
        return this
    }

    private fun addAzureStubs(server: WireMockServer) {
        WireMock.stubFor(WireMock.post(WireMock
            .urlPathMatching(".*${Paths.AZURE_V2_TOKEN_PATH}.*"))
            .willReturn(WireMock.aResponse().withTransformers(AZURE_V2_TOKEN_TRANSFORMER)))

        WireMockAzureStubs.stubJwks(path = Paths.AZURE_V2_JWKS_PATH, jwkSet = Azure.V2_0.getPublicJwk())
        WireMockAzureStubs.stubWellKnown(
            path = Paths.AZURE_V2_WELL_KNOWN_PATH,
            response = AzureWellKnown.response(
                issuer = Azure.V2_0.getIssuer(),
                jwksUri = server.getAzureV2JwksUrl(),
                tokenEndpoint = server.getAzureV2TokenUrl(),
                authorizationEndpoint = server.getAzureV2AuthorizationUrl()
            )
        )
    }

    fun build() : WireMockServer {
        if (port == null) config.dynamicPort()
        else config.port(port!!)

        configFunction?.invoke(config)
        val server = WireMockServer(config)
        serverFunction?.invoke(server)

        server.start()
        WireMock.configureFor(server.port())

        if (withAzureSupport) addAzureStubs(server)

        return server
    }
}
internal object Paths {
    private const val AZURE_V2_PATH = "/azure/v2.0"
    const val AZURE_V2_TOKEN_PATH = "$AZURE_V2_PATH/token"
    const val AZURE_V2_WELL_KNOWN_PATH = "$AZURE_V2_PATH/.well-known/openid-configuration"
    const val AZURE_V2_JWKS_PATH = "$AZURE_V2_PATH/jwks"
    const val AZURE_V2_AUTHORIZATION_PATH = "$AZURE_V2_PATH/authorize"
}

fun WireMockServer.getAzureV2WellKnownUrl() = baseUrl() + Paths.AZURE_V2_WELL_KNOWN_PATH
fun WireMockServer.getAzureV2TokenUrl() = baseUrl() + Paths.AZURE_V2_TOKEN_PATH
fun WireMockServer.getAzureV2JwksUrl() = baseUrl() + Paths.AZURE_V2_JWKS_PATH
fun WireMockServer.getAzureV2AuthorizationUrl() = baseUrl() + Paths.AZURE_V2_AUTHORIZATION_PATH