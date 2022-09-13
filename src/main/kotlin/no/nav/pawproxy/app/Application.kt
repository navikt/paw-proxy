package no.nav.pawproxy.app

import io.ktor.client.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.pawproxy.abac.abacRoute
import no.nav.pawproxy.arena.veilarbarenaRoute
import no.nav.pawproxy.arenaords.arenaOrdsPingRoute
import no.nav.pawproxy.arenaords.arenaOrdsRoute
import no.nav.pawproxy.health.healthRoute
import no.nav.pawproxy.oppfolging.veilarboppfolgingRoute
import no.nav.pawproxy.person.veilarbpersonRoute
import no.nav.pawproxy.registrering.veilarbregistreringRoute
import no.nav.pawproxy.veileder.veilarbveilederRoute
import no.nav.security.token.support.v2.asIssuerProps
import no.nav.security.token.support.v2.tokenValidationSupport
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val appContext = ApplicationContext()
    val applicationConfig: ApplicationConfig = this.environment.config
    val allIssuers = applicationConfig.asIssuerProps().keys

    install(Authentication) {
        allIssuers
            .forEach { issuer: String ->
                tokenValidationSupport(
                    name = issuer,
                    config = applicationConfig
                )
            }
    }

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
        callIdMdc("callId")

        mdc("requestId") { call -> call.request.header(HttpHeaders.XRequestId) ?: UUID.randomUUID().toString() }
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowSameOrigin = true
    }


    install(ContentNegotiation) {
        jackson()
    }

    routing {
        healthRoute(appContext.healthService)
        abacRoute(appContext.internalHttpClient)
        arenaOrdsPingRoute(appContext.internalHttpClient)

        authenticate (
            configurations = allIssuers.toTypedArray()
        ) {
            veilarbregistreringRoute(appContext.internalHttpClient, appContext.tokenService)
            arenaOrdsRoute(appContext.internalHttpClient)
            veilarbarenaRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarboppfolgingRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarbpersonRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarbveilederRoute(appContext.internalHttpClient, appContext.tokenService)
        }
    }

    configureShutdownHook(listOf(appContext.internalHttpClient, appContext.externalHttpClient))
}

fun Application.configureShutdownHook(list: List<HttpClient>) {
    environment.monitor.subscribe(ApplicationStopping) {
        list.forEach { it.close() }
    }
}
