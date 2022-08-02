package no.nav.pawproxy.app


data class Environment(
    val corsAllowedOrigins: String = requireProperty("CORS_ALLOWED_ORIGINS"),
    val azureWellKnownUrl: String = requireProperty("AZURE_APP_WELL_KNOWN_URL"),
    val azureTokenEndpoint: String = requireProperty("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
    val azureClientId: String = requireProperty("AZURE_APP_CLIENT_ID"),
    val azureIssuer: String = requireProperty("AZURE_OPENID_CONFIG_ISSUER"),
    val tokenXWellKnownUrl: String = requireProperty("TOKEN_X_WELL_KNOWN_URL"),
    val tokenXTokenEndpoint: String = requireProperty("TOKEN_X_TOKEN_ENDPOINT"),
    val tokenXClientId: String = requireProperty("TOKEN_X_CLIENT_ID"),
    val tokenXIssuer: String = requireProperty("TOKEN_X_ISSUER"),
)

fun requireProperty(property: String) =
    getPropertyOrNull(property) ?: throw IllegalStateException("Missing required property $property")

fun getPropertyOrNull(property: String): String? =
    System.getProperty(property, System.getenv(property))

fun requireClusterName() =
    requireProperty("NAIS_CLUSTER_NAME")

fun requireNamespace(): String =
    requireProperty("NAIS_NAMESPACE")
