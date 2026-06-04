package com.brawlvpn.app.vpn

import com.wireguard.android.backend.Tunnel

class AppTunnel(private val name: String) : Tunnel {
    private var currentState: Tunnel.State = Tunnel.State.DOWN

    override fun getName(): String = name

    override fun onStateChange(newState: Tunnel.State) {
        currentState = newState
    }

    fun state(): Tunnel.State = currentState
}
