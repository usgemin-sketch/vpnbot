package com.brawlvpn.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brawlvpn.app.data.ConfigNormalizer
import com.brawlvpn.app.data.CountryCatalog
import com.brawlvpn.app.data.CountryConfigStore
import com.brawlvpn.app.data.CountryProfile
import com.brawlvpn.app.data.NullsDns
import com.brawlvpn.app.vpn.TunnelSnapshot
import com.brawlvpn.app.vpn.WireGuardManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.concurrent.CancellationException

data class CountryCardUiState(
    val id: String,
    val name: String,
    val city: String,
    val endpointHint: String,
    val latencyHint: String,
    val accentColor: Long,
    val hasConfig: Boolean
)

data class EditorUiState(
    val countryId: String,
    val countryName: String,
    val configText: String
)

data class HomeUiState(
    val countries: List<CountryCardUiState> = emptyList(),
    val selectedCountryId: String = "",
    val activeCountryName: String = "Not connected",
    val statusTitle: String = "Ready for setup",
    val statusDetail: String = "Add a real WireGuard config for a country, then connect with built-in Null's DNS.",
    val primaryButtonLabel: String = "Enable VPN",
    val transferText: String = "0 B down / 0 B up",
    val handshakeText: String = "No handshake yet",
    val dnsText: String = NullsDns.hostname,
    val editor: EditorUiState? = null,
    val errorMessage: String? = null,
    val isBusy: Boolean = false,
    val launchVpnPermissionPrompt: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val configStore = CountryConfigStore(application)
    private val wireGuardManager = WireGuardManager(application)
    private val profiles = CountryCatalog.defaults
    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var statsJob: Job? = null

    fun selectCountry(countryId: String) {
        _uiState.value = _uiState.value.copy(selectedCountryId = countryId)
    }

    fun connectOrDisconnect() {
        val state = _uiState.value
        val selected = selectedProfile() ?: return
        val currentName = wireGuardManager.activeProfileId

        if (currentName == selected.id) {
            disconnect()
            return
        }

        val configText = configStore.read(selected.id)
        if (configText.isBlank()) {
            showError("Save a real WireGuard config for ${selected.name} first.")
            return
        }

        _uiState.value = state.copy(launchVpnPermissionPrompt = true)
    }

    fun onVpnPromptHandled() {
        _uiState.value = _uiState.value.copy(launchVpnPermissionPrompt = false)
    }

    fun connectSelectedCountry() {
        val selected = selectedProfile() ?: return
        val configText = configStore.read(selected.id)
        if (configText.isBlank()) {
            showError("Save a real WireGuard config for ${selected.name} first.")
            return
        }

        viewModelScope.launch {
            setBusy(true)
            try {
                val snapshot = wireGuardManager.connect(
                    selected,
                    ConfigNormalizer.withNullsDns(configText)
                )
                applySnapshot(snapshot, selected.name)
                beginStatsLoop()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                showError(error.message ?: "WireGuard failed to connect.")
            } finally {
                setBusy(false)
            }
        }
    }

    fun openEditor(countryId: String) {
        val profile = profiles.firstOrNull { it.id == countryId } ?: return
        _uiState.value = _uiState.value.copy(
            editor = EditorUiState(
                countryId = countryId,
                countryName = profile.name,
                configText = configStore.read(countryId).ifBlank { profile.template }
            )
        )
    }

    fun updateEditorText(text: String) {
        val editor = _uiState.value.editor ?: return
        _uiState.value = _uiState.value.copy(editor = editor.copy(configText = text))
    }

    fun saveEditor() {
        val editor = _uiState.value.editor ?: return
        configStore.write(editor.countryId, ConfigNormalizer.withNullsDns(editor.configText.trim()))
        _uiState.value = buildState(
            selectedCountryId = editor.countryId,
            activeCountryName = _uiState.value.activeCountryName,
            statusTitle = _uiState.value.statusTitle,
            statusDetail = _uiState.value.statusDetail,
            primaryButtonLabel = _uiState.value.primaryButtonLabel,
            transferText = _uiState.value.transferText,
            handshakeText = _uiState.value.handshakeText,
            isBusy = _uiState.value.isBusy
        )
    }

    fun dismissEditor() {
        _uiState.value = _uiState.value.copy(editor = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    private fun disconnect() {
        viewModelScope.launch {
            setBusy(true)
            try {
                wireGuardManager.disconnect()
                statsJob?.cancel()
                _uiState.value = buildState(
                    selectedCountryId = _uiState.value.selectedCountryId,
                    activeCountryName = "Not connected",
                    statusTitle = "Tunnel offline",
                    statusDetail = "Choose a country and connect when you are ready.",
                    primaryButtonLabel = "Enable VPN",
                    transferText = "0 B down / 0 B up",
                    handshakeText = "No handshake yet",
                    isBusy = false
                )
            } catch (error: Exception) {
                showError(error.message ?: "Failed to disconnect the tunnel.")
            } finally {
                setBusy(false)
            }
        }
    }

    private fun beginStatsLoop() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            while (true) {
                delay(1500)
                try {
                    val snapshot = wireGuardManager.refreshStats()
                    if (snapshot != null) {
                        val activeName = profiles.firstOrNull { it.id == snapshot.profileId }?.name
                            ?: "Tunnel active"
                        applySnapshot(snapshot, activeName)
                    }
                } catch (_: Exception) {
                    return@launch
                }
            }
        }
    }

    private fun applySnapshot(snapshot: TunnelSnapshot, activeCountryName: String) {
        _uiState.value = buildState(
            selectedCountryId = snapshot.profileId ?: _uiState.value.selectedCountryId,
            activeCountryName = activeCountryName,
            statusTitle = if (snapshot.isUp) "Connected" else "Tunnel offline",
            statusDetail = if (snapshot.isUp) {
                "Traffic is routed through WireGuard with Null's DNS. You can switch countries any time."
            } else {
                "Choose a country and connect when you are ready."
            },
            primaryButtonLabel = if (snapshot.isUp) "Disconnect" else "Enable VPN",
            transferText = "${formatBytes(snapshot.rxBytes)} down / ${formatBytes(snapshot.txBytes)} up",
            handshakeText = snapshot.handshakeText(),
            isBusy = false
        )
    }

    private fun selectedProfile(): CountryProfile? {
        val selectedId = _uiState.value.selectedCountryId
        return profiles.firstOrNull { it.id == selectedId }
    }

    private fun setBusy(value: Boolean) {
        _uiState.value = _uiState.value.copy(isBusy = value)
    }

    private fun buildInitialState(): HomeUiState {
        val selectedCountryId = profiles.first().id
        return buildState(
            selectedCountryId = selectedCountryId,
            activeCountryName = "Not connected",
            statusTitle = "Ready for setup",
            statusDetail = "Add a real WireGuard config for a country, then connect with built-in Null's DNS.",
            primaryButtonLabel = "Enable VPN",
            transferText = "0 B down / 0 B up",
            handshakeText = "No handshake yet",
            isBusy = false
        )
    }

    private fun buildState(
        selectedCountryId: String,
        activeCountryName: String,
        statusTitle: String,
        statusDetail: String,
        primaryButtonLabel: String,
        transferText: String,
        handshakeText: String,
        isBusy: Boolean
    ): HomeUiState {
        return HomeUiState(
            countries = profiles.map { profile ->
                CountryCardUiState(
                    id = profile.id,
                    name = profile.name,
                    city = profile.city,
                    endpointHint = profile.endpointHint,
                    latencyHint = profile.latencyHint,
                    accentColor = profile.accentColor,
                    hasConfig = configStore.read(profile.id).isNotBlank()
                )
            },
            selectedCountryId = selectedCountryId,
            activeCountryName = activeCountryName,
            statusTitle = statusTitle,
            statusDetail = statusDetail,
            primaryButtonLabel = primaryButtonLabel,
            transferText = transferText,
            handshakeText = handshakeText,
            dnsText = NullsDns.hostname,
            editor = null,
            errorMessage = null,
            isBusy = isBusy
        )
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes < 1024) {
            return "$bytes B"
        }

        val units = listOf("KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var index = -1
        while (value >= 1024 && index < units.lastIndex) {
            value /= 1024
            index += 1
        }

        return "${DecimalFormat("0.0").format(value)} ${units[index]}"
    }
}
