package com.brawlvpn.app.vpn

import android.content.Context
import com.brawlvpn.app.data.CountryProfile
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.io.BufferedReader
import java.io.StringReader
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class TunnelSnapshot(
    val profileId: String?,
    val isUp: Boolean,
    val rxBytes: Long,
    val txBytes: Long,
    val latestHandshakeEpochMillis: Long
) {
    fun handshakeText(): String {
        if (latestHandshakeEpochMillis <= 0) {
            return "No handshake yet"
        }

        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault())

        return formatter.format(Instant.ofEpochMilli(latestHandshakeEpochMillis))
    }
}

class WireGuardManager(context: Context) {
    private val backend = GoBackend(context.applicationContext)
    private var activeTunnel: AppTunnel? = null
    var activeProfileId: String? = null
        private set

    @Synchronized
    fun connect(profile: CountryProfile, configText: String): TunnelSnapshot {
        val tunnel = AppTunnel(tunnelName(profile.id))
        val config = Config.parse(BufferedReader(StringReader(configText)))
        val state = backend.setState(tunnel, Tunnel.State.UP, config)
        activeTunnel = tunnel
        activeProfileId = profile.id
        return snapshotFor(tunnel, profile.id, state == Tunnel.State.UP)
    }

    @Synchronized
    fun disconnect(): TunnelSnapshot {
        val tunnel = activeTunnel ?: return TunnelSnapshot(null, false, 0, 0, 0)
        backend.setState(tunnel, Tunnel.State.DOWN, null)
        activeTunnel = null
        activeProfileId = null
        return TunnelSnapshot(null, false, 0, 0, 0)
    }

    @Synchronized
    fun refreshStats(): TunnelSnapshot? {
        val tunnel = activeTunnel ?: return null
        return snapshotFor(tunnel, activeProfileId, true)
    }

    @Synchronized
    private fun snapshotFor(tunnel: AppTunnel, profileId: String?, isUp: Boolean): TunnelSnapshot {
        val stats = backend.getStatistics(tunnel)
        val latestHandshake = stats.peers()
            .mapNotNull { key -> stats.peer(key)?.latestHandshakeEpochMillis }
            .maxOrNull() ?: 0L

        return TunnelSnapshot(
            profileId = profileId,
            isUp = isUp,
            rxBytes = stats.totalRx(),
            txBytes = stats.totalTx(),
            latestHandshakeEpochMillis = latestHandshake
        )
    }

    private fun tunnelName(profileId: String): String = "brawl_${profileId}".take(15)
}
