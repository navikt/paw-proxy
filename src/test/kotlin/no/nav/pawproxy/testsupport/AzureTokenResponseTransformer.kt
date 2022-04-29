package no.nav.pawproxy.testsupport

import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.Parameters
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import com.github.tomakehurst.wiremock.http.Request
import com.github.tomakehurst.wiremock.http.Response
import com.nimbusds.jwt.SignedJWT
import java.net.URLDecoder
import java.util.*

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

interface TokenRequest {
    fun urlDecodedBody(): String
    fun authorizationHeader() : String?
}

private fun String.getOptionalParameter(parameterName: String) : String? {
    if (!contains("$parameterName=")) return null
    val afterParamName = substringAfter("$parameterName=")
    return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
    else afterParamName
}
private fun String.getRequiredParameter(parameterName: String) : String {
    check(contains("$parameterName=")) { "Parameter $parameterName ikke funnet i request $this" }
    val afterParamName = substringAfter("$parameterName=")
    return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
    else afterParamName
}
private fun String.asScopes() = split(" ").toSet()

internal fun Set<String>.extractAudience() = first {
    it.startsWith("api://") || it.endsWith("/.default")
}.removePrefix("api://").removeSuffix("/.default")

internal fun String.getExpiresIn() = (SignedJWT.parse(this).jwtClaimsSet.expirationTime.time - Date().time) / 1000
private fun TokenRequest.getAssertion() = SignedJWT.parse(urlDecodedBody().getRequiredParameter("assertion"))
private fun TokenRequest.getScopes() = urlDecodedBody().getRequiredParameter("scope").asScopes()
private fun TokenRequest.getClientId() : String {
    val clientIdFraParameter = urlDecodedBody().getOptionalParameter("client_id")
    if (clientIdFraParameter != null) return clientIdFraParameter
    val clientAssertion = urlDecodedBody().getOptionalParameter("client_assertion")
    if (clientAssertion != null) return SignedJWT.parse(clientAssertion).jwtClaimsSet.issuer
    val credentials = authorizationHeader()!!.substringAfter("Basic ")
    return String(Base64.getDecoder().decode(credentials)).split(":")[0]
}
private fun TokenRequest.clientAuthenticationMode() : Azure.ClientAuthenticationMode {
    val clientAssertion = urlDecodedBody().getOptionalParameter("client_assertion")
    return if (clientAssertion != null) Azure.ClientAuthenticationMode.CERTIFICATE else Azure.ClientAuthenticationMode.CLIENT_SECRET
}

private class WireMockTokenRequest(request: Request) : TokenRequest {
    private val body = URLDecoder.decode(request.bodyAsString, Charsets.UTF_8)!!
    private val authorizationHeader = request.getHeader("Authorization")
    override fun urlDecodedBody() = body
    override fun authorizationHeader() : String? = authorizationHeader
}