package no.nav.pawproxy.testsupport.azure

import com.github.tomakehurst.wiremock.http.Request
import com.nimbusds.jwt.SignedJWT
import java.net.URLDecoder
import java.util.*

interface TokenRequest {
    fun urlDecodedBody(): String
    fun authorizationHeader(): String?
}

internal fun String.getOptionalParameter(parameterName: String): String? {
    if (!contains("$parameterName=")) return null
    val afterParamName = substringAfter("$parameterName=")
    return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
    else afterParamName
}

private fun String.getRequiredParameter(parameterName: String): String {
    check(contains("$parameterName=")) { "Parameter $parameterName ikke funnet i request $this" }
    val afterParamName = substringAfter("$parameterName=")
    return if (afterParamName.contains("&")) afterParamName.substringBefore("&")
    else afterParamName
}

internal fun String.asScopes() = split(" ").toSet()

internal fun Set<String>.extractAudience() = first {
    it.startsWith("api://") || it.endsWith("/.default")
}.removePrefix("api://").removeSuffix("/.default")

internal fun String.getExpiresIn() = (SignedJWT.parse(this).jwtClaimsSet.expirationTime.time - Date().time) / 1000

internal fun TokenRequest.getAssertion() = SignedJWT.parse(urlDecodedBody().getRequiredParameter("assertion"))

internal fun TokenRequest.getScopes() = urlDecodedBody().getRequiredParameter("scope").asScopes()

internal fun TokenRequest.getClientId(): String {
    val clientIdFraParameter = urlDecodedBody().getOptionalParameter("client_id")
    if (clientIdFraParameter != null) return clientIdFraParameter
    val clientAssertion = urlDecodedBody().getOptionalParameter("client_assertion")
    if (clientAssertion != null) return SignedJWT.parse(clientAssertion).jwtClaimsSet.issuer
    val credentials = authorizationHeader()!!.substringAfter("Basic ")
    return String(Base64.getDecoder().decode(credentials)).split(":")[0]
}

internal fun TokenRequest.clientAuthenticationMode(): Azure.ClientAuthenticationMode {
    val clientAssertion = urlDecodedBody().getOptionalParameter("client_assertion")
    return if (clientAssertion != null) Azure.ClientAuthenticationMode.CERTIFICATE else Azure.ClientAuthenticationMode.CLIENT_SECRET
}

internal class WireMockTokenRequest(request: Request) : TokenRequest {
    private val body = URLDecoder.decode(request.bodyAsString, Charsets.UTF_8)!!
    private val authorizationHeader = request.getHeader("Authorization")
    override fun urlDecodedBody() = body
    override fun authorizationHeader(): String? = authorizationHeader
}