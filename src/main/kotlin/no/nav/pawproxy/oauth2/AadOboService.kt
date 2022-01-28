package no.nav.pawproxy.oauth2

import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTParser
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.statement.*
import io.ktor.request.*
import io.ktor.response.*
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.app.requireClusterName
import no.nav.pawproxy.app.requireNamespace
import no.nav.pawproxy.app.requireProperty
import no.nav.security.token.support.client.core.ClientAuthenticationProperties
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.OAuth2GrantType
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.core.oauth2.OnBehalfOfTokenClient
import no.nav.security.token.support.core.utils.JwtTokenUtil
import java.net.URI
import java.util.*

class AadOboService(private val httpClient: HttpClient) {
    private val tokenEndpointUrl: URI = URI.create(requireProperty("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"))
    private val discoveryUrl: URI = URI.create(requireProperty("AZURE_APP_WELL_KNOWN_URL"))
    private val onBehalfOfTokenClient = OnBehalfOfTokenClient(DefaultOAuth2HttpClient(httpClient))

    fun getAccessToken(call: ApplicationCall, api: DownstreamApi): String {
        val accessToken = call.request.authorization()?.substring("Bearer ".length) ?: throw IllegalStateException("Forventet access token!")
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
        return accessTokenService.getAccessToken(clientProperties).accessToken ?: throw IllegalStateException("Did not get access token")
    }
}

data class DownstreamApi(val cluster: String, val namespace: String, val appName: String)

val veilarbregistrering = DownstreamApi(requireClusterName(), requireNamespace(), "veilarbregistrering")
val veilarbperson = DownstreamApi(requireClusterName(), requireNamespace(), "veilarbperson")
