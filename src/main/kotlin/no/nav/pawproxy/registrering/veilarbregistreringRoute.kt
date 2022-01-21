package no.nav.pawproxy.registrering

import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.response.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.pawproxy.app.*
import no.nav.pawproxy.oauth2.DefaultOAuth2HttpClient
import no.nav.security.token.support.client.core.ClientAuthenticationProperties
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.OAuth2GrantType
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.core.oauth2.OnBehalfOfTokenClient
import java.net.URI
import java.util.*


fun Route.veilarbregistrering() {

    route("/veilarbregistrering{...}") {

        val veilarbregistreringBaseUrl = "https://veilarbregistrering.dev.intern.nav.no"

        get {
            val path = call.request.uri
            logger.info("Kall til veilarbregistrering med path: $path")
            call.respondText("Hallo veilarbregistrering")

            val accessToken = call.request.authorization()?.substring("Bearer ".length) ?: throw IllegalStateException("Må ha access token!")
            val onBehalfOfTokenClient = OnBehalfOfTokenClient(DefaultOAuth2HttpClient(client))
            val accessTokenService =
                OAuth2AccessTokenService({ Optional.of(accessToken) }, onBehalfOfTokenClient, null, null)


            val clientProperties = ClientProperties(
                URI.create(requireProperty("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
                URI.create(requireProperty("AZURE_APP_WELL_KNOWN_URL")),
                OAuth2GrantType.JWT_BEARER,
                listOf("api://${requireClusterName()}.${requireNamespace()}.veilarbregistrering/.default"),
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

            val tokenResponse = accessTokenService.getAccessToken(clientProperties)

            logger.info("Hurra! ${tokenResponse.accessToken}")


            val response = client.get<String>("$veilarbregistreringBaseUrl$path")
            logger.info("Respons fra veilarbregistrering: $response")
        }
    }
}


