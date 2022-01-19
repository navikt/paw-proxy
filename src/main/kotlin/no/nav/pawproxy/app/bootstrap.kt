package no.nav.pawproxy.app

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.util.*
import no.nav.pawproxy.health.healthApi
import no.nav.pawproxy.hello.helloApi
import no.nav.security.token.support.ktor.IssuerConfig
import no.nav.security.token.support.ktor.TokenSupportConfig

import no.nav.security.token.support.ktor.tokenValidationSupport

@KtorExperimentalAPI
fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    val environment = Environment()
    val config = IssuerConfig(
        name = "veiledere",
        discoveryUrl = environment.wellKnownUrl,
        acceptedAudience = listOf(environment.clientId)
    )

    install(DefaultHeaders)

    install(CORS) {
        host("www.dev.nav.no", schemes = listOf("https"))
        allowCredentials = true
        header(HttpHeaders.ContentType)
    }

    install(ContentNegotiation) {
        json(jsonConfig())
    }

    install(Authentication) {
        tokenValidationSupport(config = TokenSupportConfig(config))
    }

    routing {
        healthApi(appContext.healthService)

        authenticate {
            helloApi()
        }
    }

    configureShutdownHook(appContext.httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}
