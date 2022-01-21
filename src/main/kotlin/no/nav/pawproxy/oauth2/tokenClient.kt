package no.nav.pawproxy.app

val tokenClient = OAuth2Client(
    httpClient = httpClient,
    wellKnownUrl = data["TOKEN_X_WELL_KNOWN_URL"]!!,
    clientAuthProperties = ClientAuthenticationProperties(
        data["TOKEN_X_CLIENT_ID"]!!,
        ClientAuthenticationMethod.PRIVATE_KEY_JWT,
        "", //THIS is A SECRET
        data["TOKEN_X_PRIVATE_JWK"]!!
    ),
    cacheConfig = OAuth2CacheConfig(enabled = true, maximumSize = 10, evictSkew = 0)
)
