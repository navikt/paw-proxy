package no.nav.pawproxy.health

interface HealthCheck {

    suspend fun status(): HealthStatus

}
