package com.brawlvpn.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrawlVpnTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF10141D)
                ) {
                    BrawlVpnApp()
                }
            }
        }
    }
}

private data class CountryNode(
    val name: String,
    val ping: String,
    val accent: Color
)

private val countryNodes = listOf(
    CountryNode("Netherlands", "41 ms", Color(0xFF2CD5C4)),
    CountryNode("Germany", "48 ms", Color(0xFFFFD24A)),
    CountryNode("Poland", "57 ms", Color(0xFFFF7A59)),
    CountryNode("Finland", "63 ms", Color(0xFF7EE081))
)

@Composable
private fun BrawlVpnApp() {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val glow = pulse.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF10141D),
                        Color(0xFF182537),
                        Color(0xFF0A1018)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0xFF2CD5C4).copy(alpha = 0.16f * glow.value),
                radius = size.minDimension * 0.36f,
                center = Offset(size.width * 0.2f, size.height * 0.12f)
            )
            drawCircle(
                color = Color(0xFFFF7A59).copy(alpha = 0.12f),
                radius = size.minDimension * 0.42f,
                center = Offset(size.width * 0.92f, size.height * 0.22f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HeroCard()
            }

            item {
                StatusCard()
            }

            item {
                Text(
                    text = "Countries",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(countryNodes) { node ->
                CountryCard(node)
            }
        }
    }
}

@Composable
private fun HeroCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC192536)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Badge(text = "BRAWL READY")
            Text(
                text = "Brawl VPN",
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Fast route switching, game-first layout and clean SVG-driven visuals for the APK release.",
                color = Color(0xFFD3E2F2),
                lineHeight = 22.sp
            )
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD24A),
                    contentColor = Color(0xFF171B22)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = "Connect",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1825)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Status",
                    color = Color(0xFF93A9C1),
                    fontSize = 14.sp
                )
                Text(
                    text = "Ready to connect",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2CD5C4))
                    .alpha(0.95f)
            )
        }
    }
}

@Composable
private fun CountryCard(node: CountryNode) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xCC162130)),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(node.accent)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = node.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Optimized route",
                        color = Color(0xFF93A9C1),
                        fontSize = 13.sp
                    )
                }
            }
            Text(
                text = node.ping,
                color = Color(0xFFFFD24A),
                fontWeight = FontWeight.Bold
            )
        }
    }
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
