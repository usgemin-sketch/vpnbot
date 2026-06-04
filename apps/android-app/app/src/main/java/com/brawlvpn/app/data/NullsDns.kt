package com.brawlvpn.app.data

object NullsDns {
    const val hostname = "dns.nullsproxy.com"

    val servers = listOf(
        "185.211.245.131",
        "45.139.239.56",
        "179.43.147.42",
        "141.95.97.120",
        "185.175.46.137",
        "81.17.20.83"
    )

    val wireGuardLine = servers.joinToString(separator = ", ")
}
