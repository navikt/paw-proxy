package no.nav.pawproxy.app

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import no.nav.pawproxy.arena.veilarbarenaRoute
import no.nav.pawproxy.health.healthRoute
import no.nav.pawproxy.oppfolging.veilarboppfolgingRoute
import no.nav.pawproxy.person.veilarbpersonRoute
import no.nav.pawproxy.registrering.veilarbregistreringRoute
import no.nav.pawproxy.veileder.veilarbveilederRoute
import no.nav.security.token.support.ktor.IssuerConfig
import no.nav.security.token.support.ktor.TokenSupportConfig
import no.nav.security.token.support.ktor.tokenValidationSupport
import java.util.*

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

    install(CallId) {
        retrieve { call ->
            listOf(
                HttpHeaders.XCorrelationId,
                "Nav-Call-Id",
                "Nav-CallId"
            ).firstNotNullOfOrNull { call.request.header(it) }
        }

        generate {
            UUID.randomUUID().toString()
        }

        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

    install(CallLogging) {
        callIdMdc("correlation_id")

        mdc("request_id") { call -> call.request.header(HttpHeaders.XRequestId) ?: UUID.randomUUID().toString() }
    }

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
            veilarbregistreringRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarbarenaRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarboppfolgingRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarbpersonRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarbveilederRoute(appContext.internalHttpClient, appContext.tokenService)
        }
    }

    configureShutdownHook(listOf(appContext.internalHttpClient, appContext.externalHttpClient))
}

private fun Application.configureShutdownHook(list: List<HttpClient>) {
    environment.monitor.subscribe(ApplicationStopping) {
        list.forEach { it.close() }
    }
}
