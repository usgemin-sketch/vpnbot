package com.brawlvpn.app

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsState()
            var awaitingVpnPermission by remember { mutableStateOf(false) }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                awaitingVpnPermission = false
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.connectSelectedCountry()
                } else {
                    viewModel.showError("VPN permission is required before connecting.")
                }
            }

            LaunchedEffect(state.launchVpnPermissionPrompt) {
                if (state.launchVpnPermissionPrompt && !awaitingVpnPermission) {
                    val intent = VpnService.prepare(this@MainActivity)
                    if (intent == null) {
                        viewModel.connectSelectedCountry()
                    } else {
                        awaitingVpnPermission = true
                        launcher.launch(intent)
                    }
                    viewModel.onVpnPromptHandled()
                }
            }

            BrawlVpnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0D131C)
                ) {
                    BrawlVpnScreen(
                        state = state,
                        onCountrySelected = viewModel::selectCountry,
                        onConnectPressed = viewModel::connectOrDisconnect,
                        onConfigurePressed = viewModel::openEditor,
                        onEditorChanged = viewModel::updateEditorText,
                        onEditorSaved = viewModel::saveEditor,
                        onEditorDismissed = viewModel::dismissEditor,
                        onRetryAccess = viewModel::clearError
                    )
                }
            }
        }
    }
}

@Composable
private fun BrawlVpnScreen(
    state: HomeUiState,
    onCountrySelected: (String) -> Unit,
    onConnectPressed: () -> Unit,
    onConfigurePressed: (String) -> Unit,
    onEditorChanged: (String) -> Unit,
    onEditorSaved: () -> Unit,
    onEditorDismissed: () -> Unit,
    onRetryAccess: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D131C),
                        Color(0xFF152332),
                        Color(0xFF091018)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HeroCard(
                    status = state.statusTitle,
                    detail = state.statusDetail,
                    buttonLabel = state.primaryButtonLabel,
                    busy = state.isBusy,
                    onConnectPressed = onConnectPressed
                )
            }

            item {
                SetupCard(
                    activeCountry = state.activeCountryName,
                    transfer = state.transferText,
                    handshake = state.handshakeText
                )
            }

            item {
                TipsCard()
            }

            item {
                Text(
                    text = "Countries",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state.countries) { country ->
                CountryCard(
                    country = country,
                    isSelected = state.selectedCountryId == country.id,
                    onSelected = { onCountrySelected(country.id) },
                    onConfigurePressed = { onConfigurePressed(country.id) }
                )
            }
        }
    }

    if (state.editor != null) {
        ConfigEditorDialog(
            title = "WireGuard config for ${state.editor.countryName}",
            value = state.editor.configText,
            onValueChanged = onEditorChanged,
            onSave = onEditorSaved,
            onDismiss = onEditorDismissed
        )
    }

    if (state.errorMessage != null) {
        AlertDialog(
            onDismissRequest = onRetryAccess,
            title = { Text("Connection note") },
            text = { Text(state.errorMessage) },
            confirmButton = {
                TextButton(onClick = onRetryAccess) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun HeroCard(
    status: String,
    detail: String,
    buttonLabel: String,
    busy: Boolean,
    onConnectPressed: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xD8172536)),
        shape = RoundedCornerShape(30.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Badge(text = "WIREGUARD CORE")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_power_orbit),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp)
                )
                Column {
                    Text(
                        text = "Brawl VPN",
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = status,
                        color = Color(0xFF8AF5E9),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = detail,
                color = Color(0xFFD3E2F2),
                lineHeight = 22.sp
            )
            Button(
                onClick = onConnectPressed,
                enabled = !busy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD24A),
                    contentColor = Color(0xFF171B22)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (busy) "Working..." else buttonLabel,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SetupCard(
    activeCountry: String,
    transfer: String,
    handshake: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF101A27)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.ic_shield_route),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Tunnel status",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            StatRow(label = "Active route", value = activeCountry)
            StatRow(label = "Transfer", value = transfer)
            StatRow(label = "Last handshake", value = handshake)
        }
    }
}

@Composable
private fun TipsCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC162130)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "How to make it live",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "1. Paste a real WireGuard config for each country.\n2. Save it inside the app.\n3. Tap Connect and approve VPN permission.",
                color = Color(0xFFD3E2F2),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun CountryCard(
    country: CountryCardUiState,
    isSelected: Boolean,
    onSelected: () -> Unit,
    onConfigurePressed: () -> Unit
) {
    val accent = Color(country.accentColor)
    val borderColor = if (isSelected) accent else Color.Transparent

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xD8162130)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onSelected)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) {
                        Brush.linearGradient(
                            colors = listOf(borderColor.copy(alpha = 0.16f), Color.Transparent)
                        )
                    } else {
                        Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = country.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${country.city} • ${country.endpointHint}",
                            color = Color(0xFF93A9C1),
                            fontSize = 13.sp
                        )
                    }
                }
                Text(
                    text = country.latencyHint,
                    color = Color(0xFFFFD24A),
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (country.hasConfig) "Config saved" else "Needs config",
                    color = if (country.hasConfig) Color(0xFF8AF5E9) else Color(0xFFFF9B7A),
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onConfigurePressed) {
                    Text(if (country.hasConfig) "Edit config" else "Set config")
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF93A9C1)
        )
        Text(
            text = value,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ConfigEditorDialog(
    title: String,
    value: String,
    onValueChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Paste a full wg-quick config with [Interface] and [Peer] sections.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    minLines = 12
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun Badge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0x332CD5C4))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF8AF5E9),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BrawlVpnTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
