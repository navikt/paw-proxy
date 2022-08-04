package no.nav.pawproxy.token

import com.nimbusds.jwt.JWTParser
import io.ktor.server.application.*
import io.ktor.server.request.*
import no.nav.common.token_client.client.AzureAdOnBehalfOfTokenClient
import no.nav.common.token_client.client.TokenXOnBehalfOfTokenClient
import no.nav.pawproxy.app.logger
import no.nav.pawproxy.app.requireClusterName
import no.nav.pawproxy.app.requireNamespace
import no.nav.pawproxy.app.requireProperty

/**
 * TokenService har som oppgave å hente ut token for videre kall bakover i verdikjeden. Hvis riktig token allerede
 * eksisterer, benyttes dette - hvis ikke veksles dette inn med et OBO-token (On behalf of).
 */
class TokenService(
    private val tokenXClient: TokenXOnBehalfOfTokenClient,
    private val azureAdClient: AzureAdOnBehalfOfTokenClient
) {

    /**
     * Henter token for utgående API-kall
     *
     * Forventer å finne token i `Downstream-Authorization`-header. Hvis den ikke finnes, benytter den
     * OBO-klienten (On behalf of) for å veksle inn token.
     */
    fun getAccessToken(incomingCall: ApplicationCall, outgoingApi: DownstreamApi): String {
        return incomingCall.request.header("Downstream-Authorization")?.let { return it.removePrefix("Bearer ") }
            ?: exchangeToken(incomingCall, outgoingApi)
    }

    private fun exchangeToken(call: ApplicationCall, api: DownstreamApi): String {
        val accessToken = call.request.authorization()?.substring("Bearer ".length)
            ?: throw TokenExchangeException("Fant ikke accesstoken for token-veksling")
        return when {
            erAzureADToken(accessToken) -> {
                logger.info("Veksler Azure AD-token for ${api.appName}")
                azureAdClient.exchangeOnBehalfOfToken(
                    "api://${api.cluster}.${api.namespace}.${api.appName}/.default",
                    accessToken
                )
            }
            erTokenXToken(accessToken) -> {
                logger.info("Veksler TokenX-token for ${api.appName}")
                tokenXClient.exchangeOnBehalfOfToken(
                    "${api.cluster}:${api.namespace}:${api.appName}",
                    accessToken
                )
            }
            else -> throw TokenExchangeException("Klarer ikke veksle token som er en annen type enn Azure AD eller TokenX")
        }
    }

    private fun erAzureADToken(token: String): Boolean {
        val tokenAsJWT = JWTParser.parse(token)
        val azureAdIssuer = requireProperty("AZURE_OPENID_CONFIG_ISSUER")
        return tokenAsJWT.jwtClaimsSet.issuer.contains(azureAdIssuer)
    }

    private fun erTokenXToken(token: String): Boolean {
        val tokenAsJWT = JWTParser.parse(token)
        val tokenXIssuer = requireProperty("TOKEN_X_ISSUER")
        return tokenAsJWT.jwtClaimsSet.issuer.contains(tokenXIssuer)
    }
}


data class DownstreamApi(val cluster: String, val namespace: String, val appName: String)

val veilarbregistrering = DownstreamApi(requireClusterName(), requireNamespace(), "veilarbregistrering")
val veilarbarena = DownstreamApi(requireClusterName(), "pto", "veilarbarena")
val veilarboppfolging = DownstreamApi(requireClusterName(), "pto", "veilarboppfolging")
val veilarbperson = DownstreamApi(requireClusterName(), "pto", "veilarbperson")
val veilarbveileder = DownstreamApi(requireClusterName(), "pto", "veilarbveileder")
