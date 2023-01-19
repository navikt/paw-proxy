package no.nav.pawproxy.app

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import no.nav.pawproxy.health.healthRoute
import no.nav.pawproxy.oppfolging.veilarboppfolgingRoute
import no.nav.pawproxy.person.veilarbpersonRoute
import no.nav.pawproxy.veileder.veilarbveilederRoute
import no.nav.security.token.support.v2.IssuerConfig
import no.nav.security.token.support.v2.TokenSupportConfig
import no.nav.security.token.support.v2.tokenValidationSupport
import java.util.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused")
fun Application.localModule() {
    val appContext = ApplicationContextLocal()
    val config = IssuerConfig(
        name = "veiledere",
        discoveryUrl = requireProperty("AZURE_APP_WELL_KNOWN_URL"),
        acceptedAudience = listOf(requireProperty("AZURE_APP_CLIENT_ID"))
    )

    install(DefaultHeaders)
    install(Authentication) {
        tokenValidationSupport(config = TokenSupportConfig(config))
    }

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

    install(ContentNegotiation) {
        jackson()
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

    routing {
        healthRoute(appContext.healthService)
        authenticate {
            veilarboppfolgingRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarbpersonRoute(appContext.internalHttpClient, appContext.tokenService)
            veilarbveilederRoute(appContext.internalHttpClient, appContext.tokenService)
        }
    }

    configureShutdownHook(listOf(appContext.internalHttpClient, appContext.externalHttpClient))
}