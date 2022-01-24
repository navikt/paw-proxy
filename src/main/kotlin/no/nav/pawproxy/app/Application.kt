package no.nav.pawproxy.app

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import no.nav.pawproxy.health.healthApi
import no.nav.pawproxy.hello.helloApi
import no.nav.pawproxy.person.veilarbperson
import no.nav.pawproxy.registrering.veilarbregistrering
import no.nav.security.token.support.ktor.IssuerConfig
import no.nav.security.token.support.ktor.TokenSupportConfig
import no.nav.security.token.support.ktor.tokenValidationSupport

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val appContext = ApplicationContext()
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

    install(Authentication) {
        tokenValidationSupport(config = TokenSupportConfig(config))
    }

    routing {
        healthApi(appContext.healthService)

        authenticate {
            veilarbregistrering(appContext.internalHttpClient, appContext.aadOboService)
            veilarbperson(appContext.internalHttpClient, appContext.aadOboService)
            helloApi()
        }
    }

    configureShutdownHook(listOf(appContext.internalHttpClient, appContext.externalHttpClient))
}

private fun Application.configureShutdownHook(list: List<HttpClient>) {
    environment.monitor.subscribe(ApplicationStopping) {
        list.forEach { it.close() }
    }
}
