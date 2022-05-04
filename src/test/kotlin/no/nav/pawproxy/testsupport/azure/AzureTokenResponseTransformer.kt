package no.nav.pawproxy.testsupport.azure

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response

internal class AzureTokenResponseTransformer(
    private val name: String,
    private val issuer: String
) : ResponseTransformer() {

    override fun getName() = name
    override fun applyGlobally() = false

    override fun transform(
        request: Request?,
        response: Response?,
        files: FileSource?,
        parameters: Parameters?
    ): Response {
        val tokenResponse = response(
            request = WireMockTokenRequest(request!!),
            issuer = issuer
        )

        return Response.Builder.like(response)
            .status(200)
            .headers(HttpHeaders(HttpHeader.httpHeader("Content-Type", "application/json; charset=UTF-8")))
            .body(tokenResponse)
            .build()
    }

    private fun response(request: TokenRequest, issuer: String) : String = onBehalfOf(request, issuer)

    private fun onBehalfOf(
        request: TokenRequest,
        issuer: String) : String {
        val clientId = request.getClientId()
        val clientAuthenticationMode = request.clientAuthenticationMode()
        val scopes = request.getScopes()
        val audience = scopes.extractAudience()
        val name = request.getAssertion().jwtClaimsSet.getStringClaim("name")

        val accessToken = Azure.V2_0.generateJwt(
            issuer = issuer,
            clientId = clientId,
            clientAuthenticationMode = clientAuthenticationMode,
            scopes = scopes,
            audience = audience,
            overridingClaims = mapOf(
                "name" to name
            ),
            accessAsApplication = false
        )

        return """
            {
                "token_type": "Bearer",
                "access_token" : "$accessToken",
                "expires_in" : ${accessToken.getExpiresIn()}
            }
        """.trimIndent()
    }
}

