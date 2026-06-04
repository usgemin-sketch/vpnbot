package com.brawlvpn.app.data

data class CountryProfile(
    val id: String,
    val name: String,
    val city: String,
    val endpointHint: String,
    val latencyHint: String,
    val accentColor: Long,
    val template: String
)

object CountryCatalog {
    val defaults = listOf(
        CountryProfile(
            id = "nl",
            name = "Netherlands",
            city = "Amsterdam",
            endpointHint = "nl.example.com:51820",
            latencyHint = "41 ms",
            accentColor = 0xFF2CD5C4,
            template = templateFor("10.20.0.2/32", "nl.example.com:51820")
        ),
        CountryProfile(
            id = "de",
            name = "Germany",
            city = "Frankfurt",
            endpointHint = "de.example.com:51820",
            latencyHint = "48 ms",
            accentColor = 0xFFFFD24A,
            template = templateFor("10.30.0.2/32", "de.example.com:51820")
        ),
        CountryProfile(
            id = "pl",
            name = "Poland",
            city = "Warsaw",
            endpointHint = "pl.example.com:51820",
            latencyHint = "57 ms",
            accentColor = 0xFFFF7A59,
            template = templateFor("10.40.0.2/32", "pl.example.com:51820")
        ),
        CountryProfile(
            id = "fi",
            name = "Finland",
            city = "Helsinki",
            endpointHint = "fi.example.com:51820",
            latencyHint = "63 ms",
            accentColor = 0xFF7EE081,
            template = templateFor("10.50.0.2/32", "fi.example.com:51820")
        )
    )

    private fun templateFor(address: String, endpoint: String): String = """
[Interface]
PrivateKey =
Address = $address
DNS = ${NullsDns.wireGuardLine}

[Peer]
PublicKey =
PresharedKey =
AllowedIPs = 0.0.0.0/0, ::/0
Endpoint = $endpoint
PersistentKeepalive = 25
""".trimIndent()
}
