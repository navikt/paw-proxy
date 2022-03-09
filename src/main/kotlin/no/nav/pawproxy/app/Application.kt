package no.nav.pawproxy.app

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import no.nav.pawproxy.arena.veilarbarena
import no.nav.pawproxy.health.healthRoute
import no.nav.pawproxy.oppfolging.veilarboppfolging
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
        anyHost()
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header(HttpHeaders.ContentType)
        allowSameOrigin = true
    }

    install(Authentication) {
        tokenValidationSupport(config = TokenSupportConfig(config))
    }

    install(ContentNegotiation) {
        jackson()
    }

    routing {
        healthRoute(appContext.healthService)

        authenticate {
            veilarbregistrering(appContext.internalHttpClient, appContext.tokenService)
            veilarbarena(appContext.internalHttpClient, appContext.tokenService)
            veilarboppfolging(appContext.internalHttpClient, appContext.tokenService)
        }
    }

    configureShutdownHook(listOf(appContext.internalHttpClient, appContext.externalHttpClient))
}

private fun Application.configureShutdownHook(list: List<HttpClient>) {
    environment.monitor.subscribe(ApplicationStopping) {
        list.forEach { it.close() }
    }
}
