package no.nav.pawproxy.app

import io.ktor.client.features.*
import io.ktor.http.*

fun exceptionToStatusCode(e: Throwable): HttpStatusCode =
    when (e) {
        is RedirectResponseException -> e.response.status
        is ClientRequestException -> e.response.status
        is ServerResponseException -> e.response.status
        else -> HttpStatusCode.InternalServerError
    }