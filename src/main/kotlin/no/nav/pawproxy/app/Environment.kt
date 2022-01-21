package no.nav.pawproxy.app


data class Environment(
    val corsAllowedOrigins: String = requireProperty("CORS_ALLOWED_ORIGINS"),
    val wellKnownUrl: String = requireProperty("AZURE_APP_WELL_KNOWN_URL"),
    val tokenEndpoint: String = requireProperty("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val clientId: String = requireProperty("AZURE_APP_CLIENT_ID"),
    val issuer: String = requireProperty("AZURE_OPENID_CONFIG_ISSUER"),
)

fun requireProperty(property: String) =
    getPropertyOrNull(property) ?: throw IllegalStateException("Missing required property $property")

fun getPropertyOrNull(property: String): String? =
    System.getProperty(property, System.getenv(property))

fun requireClusterName() =
    requireProperty("NAIS_CLUSTER_NAME")

fun requireNamespace(): String =
    requireProperty("NAIS_NAMESPACE")

fun requireApplicationName() =
    requireProperty("NAIS_APP_NAME")

fun applicationNameOrNull() =
    getPropertyOrNull("NAIS_APP_NAME")

fun isDevelopment(): Boolean =
    requireClusterName().startsWith("dev")

fun ifDevelopment(block: () -> Any) {
    if (isDevelopment()) {
        run(block)
    }
}