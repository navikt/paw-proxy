package no.nav.pawproxy.oauth2

import com.nimbusds.jwt.JWTParser
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.request.*
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.app.requireClusterName
import no.nav.pawproxy.app.requireNamespace
import no.nav.pawproxy.app.requireProperty
import no.nav.security.token.support.client.core.ClientAuthenticationProperties
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.OAuth2GrantType
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.core.oauth2.OnBehalfOfTokenClient
import java.net.URI
import java.util.*

/**
 * TokenService har som oppgave å hente ut token for videre kall bakover i verdikjeden. Hvis riktig token allerede
 * eksisterer, benyttes dette - hvis ikke veksles dette inn med et OBO-token (On behalf of).
 */
class TokenService(private val httpClient: HttpClient) {
    private val tokenEndpointUrl: URI = URI.create(requireProperty("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"))
    private val discoveryUrl: URI = URI.create(requireProperty("AZURE_APP_WELL_KNOWN_URL"))
    private val onBehalfOfTokenClient = OnBehalfOfTokenClient(DefaultOAuth2HttpClient(httpClient))

    /**
     * Henter token for utgående API-kall
     *
     * Forventer å finne token i `Downstream-Authorization`-header. Hvis den ikke finnes, benytter den
     * OBO-klienten (On behalf of) for å veksle inn token.
     */
    fun getAccessToken(incommingCall: ApplicationCall, outgoingApi: DownstreamApi): String {
        logger.info("Henter token for ${outgoingApi.appName}")
        return incommingCall.request.header("Downstream-Authorization")?.let { return it.removePrefix("Bearer ") }
            ?: performGrantRequest(incommingCall, outgoingApi)
    }

    private fun performGrantRequest(call: ApplicationCall, api: DownstreamApi): String {
        val accessToken = call.request.authorization()?.substring("Bearer ".length)
            ?: throw IllegalStateException("Forventet access token!")
        logger.info("Now Exchanging token with claims: ${JWTParser.parse(accessToken).jwtClaimsSet}")

        val accessTokenService =
            OAuth2AccessTokenService({ Optional.of(accessToken) }, onBehalfOfTokenClient, null, null)

        val clientProperties = ClientProperties(
            tokenEndpointUrl,
            discoveryUrl,
            OAuth2GrantType.JWT_BEARER,
            listOf("api://${api.cluster}.${api.namespace}.${api.appName}/.default"),
            ClientAuthenticationProperties(
                requireProperty("AZURE_APP_CLIENT_ID"),
                ClientAuthenticationMethod.CLIENT_SECRET_POST,
                requireProperty("AZURE_APP_CLIENT_SECRET"),
                null
            ),
            null,
            ClientProperties.TokenExchangeProperties(
                requireProperty("AZURE_APP_CLIENT_ID"),
                null
            )
        )
        return accessTokenService.getAccessToken(clientProperties).accessToken
            ?: throw IllegalStateException("Did not get access token")
    }
}


data class DownstreamApi(val cluster: String, val namespace: String, val appName: String)

val veilarbregistrering = DownstreamApi(requireClusterName(), requireNamespace(), "veilarbregistrering")
val veilarbarena = DownstreamApi(requireClusterName(), "pto", "veilarbarena")
val veilarboppfolging = DownstreamApi(requireClusterName(), "pto", "veilarboppfolging")
val veilarbperson = DownstreamApi(requireClusterName(), "pto", "veilarbperson")
val veilarbveileder = DownstreamApi(requireClusterName(), "pto", "veilarbveileder")
