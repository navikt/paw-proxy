package no.nav.pawproxy.oauth2

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.pawproxy.app.logger
import no.nav.security.token.support.client.core.http.OAuth2HttpClient
import no.nav.security.token.support.client.core.http.OAuth2HttpRequest
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenResponse

class DefaultOAuth2HttpClient(private val httpClient: HttpClient) : OAuth2HttpClient {

    override fun post(oAuth2HttpRequest: OAuth2HttpRequest): OAuth2AccessTokenResponse {
        return runBlocking {
            try {
                return@runBlocking httpClient.submitForm(
                    url = oAuth2HttpRequest.tokenEndpointUrl.toString(),
                    formParameters = Parameters.build {
                        oAuth2HttpRequest.formParameters.forEach {
                            append(it.key, it.value)
                        }
                    }
                )
            } catch (ex: Exception) {
                if (ex is ClientRequestException) {
                    logger.error(ex.response.receive<String>())
                }

                throw ex
            }
        }
    }
}
