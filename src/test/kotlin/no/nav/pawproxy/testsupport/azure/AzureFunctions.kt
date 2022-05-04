package no.nav.pawproxy.testsupport.azure

import io.ktor.http.*
import io.ktor.server.testing.*

object AzureFunctions {

    fun TestApplicationRequest.medAzure(
        audience: String = "paw-proxy",
        clientId: String = "allowed-1"
    ) {
        addHeader(
            HttpHeaders.Authorization, "Bearer ${
                Azure.V2_0.generateJwt(
                    clientId = clientId,
                    audience = audience
                )
            }"
        )
    }
}