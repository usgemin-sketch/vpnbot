package com.brawlvpn.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.brawlvpn.app.data.CountryCatalog
import com.brawlvpn.app.data.CountryConfigStore
import com.brawlvpn.app.data.CountryProfile
import com.brawlvpn.app.data.NullsDns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    val statusTitle: String = "Ready",
    val statusDetail: String = "Tap the button to switch the VPN shell on or off.",
    val primaryButtonLabel: String = "Enable VPN",
    val transferText: String = "0 B down / 0 B up",
    val handshakeText: String = "Waiting",
    val dnsText: String = NullsDns.hostname,
    val editor: EditorUiState? = null,
    val errorMessage: String? = null,
    val isBusy: Boolean = false,
    val launchVpnPermissionPrompt: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val configStore = CountryConfigStore(application)
    private val profiles = CountryCatalog.defaults
    private val _uiState = MutableStateFlow(buildInitialState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var isConnected = false

    fun selectCountry(countryId: String) {
        _uiState.value = _uiState.value.copy(selectedCountryId = countryId)
        if (!isConnected) {
            _uiState.value = buildState(
                selectedCountryId = countryId,
                activeCountryName = "Not connected",
                statusTitle = "Ready",
                statusDetail = "Selected ${selectedProfile()?.name ?: "country"}. Tap the button to enable the VPN shell.",
                primaryButtonLabel = "Enable VPN"
            )
        }
    }

    fun connectOrDisconnect() {
        val selected = selectedProfile() ?: return
        isConnected = !isConnected

        _uiState.value = if (isConnected) {
            buildState(
                selectedCountryId = selected.id,
                activeCountryName = selected.name,
                statusTitle = "Connected",
                statusDetail = "VPN shell is enabled. Null's DNS is active inside the app shell.",
                primaryButtonLabel = "Disable VPN"
            ).copy(
                transferText = "Connected",
                handshakeText = "Instant"
            )
        } else {
            buildState(
                selectedCountryId = selected.id,
                activeCountryName = "Not connected",
                statusTitle = "Disabled",
                statusDetail = "VPN shell is off. Tap the button any time to enable it again.",
                primaryButtonLabel = "Enable VPN"
            )
        }
    }

    fun onVpnPromptHandled() = Unit

    fun connectSelectedCountry() = Unit

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
        configStore.write(editor.countryId, editor.configText.trim())
        _uiState.value = buildState(
            selectedCountryId = editor.countryId,
            activeCountryName = if (isConnected) selectedProfile()?.name ?: "Connected" else "Not connected",
            statusTitle = if (isConnected) "Connected" else "Ready",
            statusDetail = if (isConnected) {
                "VPN shell is enabled. Null's DNS is active inside the app shell."
            } else {
                "Saved a country note. Tap the button to enable the VPN shell."
            },
            primaryButtonLabel = if (isConnected) "Disable VPN" else "Enable VPN"
        ).copy(
            transferText = if (isConnected) "Connected" else "0 B down / 0 B up",
            handshakeText = if (isConnected) "Instant" else "Waiting"
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

    private fun selectedProfile(): CountryProfile? {
        return profiles.firstOrNull { it.id == _uiState.value.selectedCountryId }
    }

    private fun buildInitialState(): HomeUiState {
        return buildState(
            selectedCountryId = profiles.first().id,
            activeCountryName = "Not connected",
            statusTitle = "Ready",
            statusDetail = "Tap the button to switch the VPN shell on or off.",
            primaryButtonLabel = "Enable VPN"
        )
    }

    private fun buildState(
        selectedCountryId: String,
        activeCountryName: String,
        statusTitle: String,
        statusDetail: String,
        primaryButtonLabel: String
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
            transferText = "0 B down / 0 B up",
            handshakeText = "Waiting",
            dnsText = NullsDns.hostname,
            editor = null,
            errorMessage = null,
            isBusy = false
        )
    }
}
