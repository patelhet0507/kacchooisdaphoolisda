package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Suit
import com.example.ui.theme.CardWhite
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted

@Composable
fun TrumpIndicator(
    currentTrump: Suit,
    roundNumber: Int,
    totalRounds: Int,
    cardCount: Int,
    modifier: Modifier = Modifier
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 600

    Card(
        modifier = modifier
            .testTag("trump_indicator"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurface.copy(alpha = 0.9f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.verticalGradient(
                listOf(GoldPrimary, EmeraldBorder.copy(alpha = 0.5f))
            ),
            width = 2.dp
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Round Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ROUND $roundNumber / $totalRounds",
                    color = GoldLight,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$cardCount CARDS",
                    color = TextLight.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, EmeraldBorder, Color.Transparent)))
            )

            // Suit Rotation
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Suit.ROTATION_ORDER.forEach { suit ->
                    val isActive = suit == currentTrump
                    MnemonicPill(
                        suit = suit,
                        isActive = isActive,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun MnemonicPill(
    suit: Suit,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        Brush.verticalGradient(listOf(GoldLight, GoldPrimary))
    } else {
        Brush.verticalGradient(listOf(EmeraldDeep.copy(alpha = 0.6f), Color(0xFF03140A)))
    }
    val textColor = if (isActive) EmeraldDeep else TextMuted
    val borderColor = if (isActive) GoldLight else EmeraldBorder.copy(alpha = 0.3f)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_large")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isActive) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (isActive) 8.dp else 0.dp,
        animationSpec = spring(),
        label = "elevation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(shadowElevation, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(if (isActive) 2.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = suit.symbol,
                fontSize = if (isActive) 28.sp else 18.sp,
                fontWeight = FontWeight.Black,
                color = if (isActive) {
                    if (suit.isRed) Color(0xFF991B1B) else EmeraldDeep
                } else {
                    if (suit.isRed) Color(0xFFF87171) else TextLight
                }
            )

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = suit.mnemonic.uppercase(),
                    fontSize = if (isActive) 16.sp else 13.sp,
                    fontWeight = FontWeight.Black,
                    color = textColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = suit.localName,
                    fontSize = if (isActive) 11.sp else 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) EmeraldDeep.copy(alpha = 0.7f) else TextMuted.copy(alpha = 0.6f)
                )
            }
        }
    }
}

