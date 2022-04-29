package no.nav.pawproxy.testsupport

import java.util.*

object Azure {
    private const val accessAsApplicationRole = "access_as_application"

    enum class ClientAuthenticationMode(val claimValue: String) {
        PUBLIC("0"),
        CLIENT_SECRET("1"),
        CERTIFICATE("2")
    }

    object V2_0 : Issuer {
        private const val version = "2.0"
        private const val actualIssuer = "http://localhost/azure/v2.0"

        private val privateKeyJwk = """
        {
            "kid": "pxGPIjJP0b9r6ywnukU9ZYig5bo=",
            "kty": "RSA",
            "n": "ymdcz36cE2sSVRejEOhFf-OBAzCBlDNhOl2xEYXnVEt2pdfY8VtKvrAxSc4WUe6eiRmxx-M8bIAhStcjuNWal2fTszMuxg0BNGtiG3LHfp6rP0y3epUOTh-g1fcMd_VxjquMhWntAfh9pehYl6_BewdolvWomAEW3UNKwclNNpbGGvEVsn8tjKrkw6a9O-cniqYrLRL8wmlYFONulwcdXBgW02IYipRQ5zZkMEakfv3ZuZIoyU-_CnINH2wwqvPnMXdVzCCtAINAadUSDAhzAS-XjCATh-55bv163wACw0D1wW5OALEXeUjK3JbpWLO3uViSUfEYEUX-wmTsGrjjZw",
            "e": "AQAB",
            "d": "VDhqUBS40QOunyW0vqZHtQ1vc2pNoOM4Q9cUhNwZA-RavGtyZAu-sFYUTEeq1fDamuXMKgaN7__o2oFm5dRL_VBAsMJNZi-nHq7IJe--vxs62p4LgsBfMXbLr_yafZft5pXPZPxMmJNThSOHKacCdTUB-j7CJm3dm7gSdUxUCrA3kYLtRR8MxN5kSEjG7sVLCYGky1TmO__DWqHzFNB2l_9WUYOZoOii8-JpSEunXXw3EdDo1IeR4aBeKr1pdshuXcENvfB0tmzxQxi_gZLL1RTw12pJLmjRb3KRZIPiHUYVeAazpDToeTeNVheORAnrCyxY9i67ehMbl9qz-PoJsQ",
            "p": "_2Izrld2rhNewhLoZlxv6XvTO70pLZLzCm-lnQMMxqJqAPKAqa4vhK0Jm3ok5qCakXtn5qv0Kn5bgza9rPUky-P9Jl7CSGhy1V7uFHQWt-lP1RVnv6f60kxEV5j9vWPXnPHxiCj8euEoOvpexGuoPTM099lsczw7uDLw9z7jiss",
            "q": "yuRs1PvNZQUzCoBn-xD-IBXNTMdCK2aa3V2yGKKSDEvKE4rVpnfjNmo6B0OJtAURlgc6n8oazUsfm-w3I8kq0BGSL0lWVQ8CIhTtnNK1NIOBTWDRSwZw4o6C-3Dqwi3v3UTBeXTeHgeFMSW1M9smfGj31k1pws8B_7Zj1IfjqlU",
            "dp": "xh7O7R-ZSG5qgrDcbhykDUQsmRmkimCH_76hgm9NSAPTrKx0uC4TWyBKZb2aRvitMNPFxSP9JcIlCGQ9PaJoS2yxhUTaAAZXn8QneuKchUyQzEPw2rRcVy0nj7V2k6iTKoRf1jvFsyZdaXO2dTb3q5LAMs8P3U4LHlkWqxi0uYU",
            "dq": "i3SScZpZxRPbz14PGnzFj1ws7pcPHjG6RGmAXnpvlFALp38H9xH0dJRzKkb7wWayfcKeQvJxlaYLJesow0okSTuqlSH9Wx2jw7VK1T3nGx9AouTnNoBI2xBFa3pjgfB9LEN0EH2Jpm5Y2D0o3WnGfEDSCNTQ2vvaTd8Zox18GLk",
            "qi": "WQQAejSpzO-ox8h8MmoRTORAfXCYSliG1a5RYcLS-_4ZSdj1ijL31hJm1veibwIoOkxBDShZP7uZ88WJFDJS80V0Xa-nbqc0v51EVracwvfo7Nw7LC1uXDpa_h6EjZTJYv6le2_aM5_JE0FVG__91LLVmd-WJzaPOM5q2jHxmSM"
        }
        """.trimIndent()
        private val jwsFunctions = JwsFunctions(privateKeyJwk)

        override fun getIssuer() = actualIssuer
        override fun getPublicJwk() = jwsFunctions.getPublicJwk()

        fun generateJwt(
            clientId: String,
            audience: String,
            clientAuthenticationMode: ClientAuthenticationMode = ClientAuthenticationMode.CERTIFICATE,
            groups: Set<String> = emptySet(),
            roles: Set<String> = emptySet(),
            scopes: Set<String> = emptySet(),
            issuer: String = actualIssuer,
            overridingClaims: Map<String, Any> = emptyMap(),
            accessAsApplication: Boolean = true
        ) = jwsFunctions.generateJwt(
            claims = overridingClaims.toMutableMap().apply {
                putIfAbsent("ver", version)
                putIfAbsent("aud", audience)
                putIfAbsent("iss", issuer)
                putIfAbsent("azp", clientId)
                putIfAbsent("azpacr", clientAuthenticationMode.claimValue)
                putIfAbsent("groups", groups)
                putIfAbsent("roles", when (accessAsApplication) {
                    true -> roles.plus(accessAsApplicationRole)
                    false -> roles
                })
                putIfAbsent("scp", scopes.joinToString(" "))
                putIfAbsent("sub", UUID.randomUUID().toString())
            }.toMap()
        )
    }
}

interface Issuer {
    fun getPublicJwk() : String
    fun getIssuer() : String
}