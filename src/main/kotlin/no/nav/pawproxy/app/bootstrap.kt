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

import no.nav.security.token.support.ktor.tokenValidationSupport

@KtorExperimentalAPI
fun Application.mainModule(appContext: ApplicationContext = ApplicationContext()) {
    val environment = Environment()
    val config = this.environment.config

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
        tokenValidationSupport(config = config)
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
