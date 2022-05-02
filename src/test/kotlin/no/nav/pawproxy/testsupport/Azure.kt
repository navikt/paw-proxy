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
                 "p": "2dZeNRmZow7uWKzqpOolNf7FUIr6dP5bThwnqpcDca7sP96fzPGaryZmYAawZj7h1UthpDp9b2D5v0-D0fSrbdp-MisaOz_ZL-2kdwyTSIP0ii-4yPHpFqaZuGTbuLmROwDhklTGMoYC4fN8vb0jgE6cR33bA52JH255qz5R1rc",
                 "kty": "RSA",
                 "q": "pIt7sgMqDPGZDMiksZ19R9iuUZk5ZcsnPeI0yAGIaEp75Nc7IH9F1LQ8mPw-wtV3Yde26mByszjeskVfldlReZmzeCTXq4jgu5WEi2GM7craTZj-ES7SLkuP21uvbgxGCLxEizr4RCdZD8TtkxcSG2-GPkp-N4IX9187kvWbWl8",
                 "d": "R_P82iKNJflwkPnpOr5eGmtekLvTq1cZwJ7M0vbox3LlVmpIP9iRPKVEwuBva0ybRu1pkvM4S3DFgYK6gKjHVzPYl6lHvKZxbFyP8lJoaj1km2NhA3cwqJjqkx4VAJhLlEuG5wDlTSRXNpzqfamdZcH-XMG2rM-nh6yFqbSzyaeO99ZnGMDp5mZvzGuR0VmV6IXPXqelP4uT9cPQD60h1v2DaOKlmd-0ghGfdHa0hzR5S8C55oZ5hF1_bhgx6tA8VzC1jp41mDbKmKAOKvcFG2T9JQRBml2izRVVaCsVN0_ZCR7NhQYrkreqgVN_ZLlgzI6YSA2EN1FWmc9GvNFAbQ",
                 "e": "AQAB",
                 "use": "sig",
                 "kid": "ut-Fle8JH9IdPqo7QDDakblWR1DdvMIijJT9A-lVntk",
                 "qi": "uoncSFVC9_vS652rNSZQO8I7KCk0b2bpt38Sb1iQ8Vha5lYkrTp-AsZLoduj7TscCCqlftm9R-FkfERjEYZLdPKQIaGcCQ-L0RzIG_K3w48Tk2T_EEiMqds4UeBpQxccMjUvX-t_b7pwMjFL1RIEBSWAxg5YShT8C83hv0llh9Y",
                 "dp": "BLMxWSfyPqhl0Bf7AA_lOaMDktdMzBVo1uiYmn-jnWJOypn9DKjx03Gap9u9Fpeou7dipe51ImAPQ2dtyqvivv4F1wNDD6AzCWuxLrhgvSHLtueMrxk5FDoH-wiCDRxD2-gK9eNKW3C0wzdDq7xW9b-8c3ZtsUhG2xzBF0bC8UU",
                 "alg": "RS256",
                 "dq": "R_ji4BhWOlcq9NaGg1I5zEVQ6kw1OPtFbOIW6C0Td1qtGomySSKibslvgBNFeH9auqdaUOZjBVWowx1pE-h8pM3AHJsw4sz6T9K0qSrAM_r4xdxXtThfovRWNkLCV0ZzE7sV2DixA06avDUNHbuHpgyAEZsP3kO_K-qx6jQYAc0",
                 "n": "jAQFAKQ9omNtb_I2iSryCulJnkB56qGf35fA1RrDBLup7ysJCez9dnu-HTZ62SKoe-9Pxu-4WzjjBNQacotUXYTIi7GFWM5Pyb4ha-bBJprbiwhyrYGIVzZw4LIcleexWPcIOI0cTKmpM6qKb9_6CTFa-A6uX_16n-n3fQjWGPKrJBY7mcIalJ4YTmLhavs6yt6efSD67SaJ2FabzjouRa_yeDmsGPq2LA-4FymDvuGCHeeMtPO9ClnA2eWC15L7n3-Pagm5pso5GchORl2Rwr_bhCmNCKsC_Qh6TqTHJyymuJwZIuSOv88cf-5UsSidRSJ9r0dBl0S0KgndCagD6Q"
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
                putIfAbsent(
                    "roles", when (accessAsApplication) {
                        true -> roles.plus(accessAsApplicationRole)
                        false -> roles
                    }
                )
                putIfAbsent("scp", scopes.joinToString(" "))
                putIfAbsent("sub", UUID.randomUUID().toString())
            }.toMap()
        )
    }
}

interface Issuer {
    fun getPublicJwk(): String
    fun getIssuer(): String
}