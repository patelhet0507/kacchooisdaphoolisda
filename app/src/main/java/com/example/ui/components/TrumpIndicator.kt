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

    if (isSmallScreen) {
        Card(
            modifier = modifier
                .testTag("trump_indicator"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface.copy(alpha = 0.95f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(EmeraldBorder, GoldPrimary.copy(alpha = 0.5f), EmeraldBorder)
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                    )
                    Text(
                        text = "R $roundNumber/$totalRounds • $cardCount C",
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Suit.ROTATION_ORDER.forEach { suit ->
                        val isActive = suit == currentTrump
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isActive) GoldPrimary else DarkSurfaceElevated.copy(alpha = 0.5f))
                                .border(1.dp, if (isActive) GoldLight else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 2.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = suit.symbol,
                                fontSize = if (isActive) 14.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isActive) EmeraldDeep else (if (suit.isRed) Color(0xFFF87171) else TextMuted)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .testTag("trump_indicator"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = DarkSurface.copy(alpha = 0.95f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(EmeraldBorder, GoldPrimary, EmeraldBorder)
                )
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Round & Card count row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                        )
                        Text(
                            text = "ROUND $roundNumber / $totalRounds",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(listOf(EmeraldDeep, DarkSurfaceElevated))
                            )
                            .border(1.5.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "$cardCount ${if (cardCount == 1) "Card" else "Cards"}",
                            color = TextLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The Ka-Chu-Fu-L Rotation Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Suit.ROTATION_ORDER.forEach { suit ->
                        val isActive = suit == currentTrump
                        MnemonicPill(
                            suit = suit,
                            isActive = isActive,
                            modifier = Modifier.weight(1f)
                        )
                    }
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
        targetValue = if (isActive) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(if (isActive) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = suit.symbol,
                fontSize = if (isActive) 22.sp else 16.sp,
                fontWeight = FontWeight.Black,
                color = if (isActive) {
                    if (suit.isRed) Color(0xFF991B1B) else EmeraldDeep
                } else {
                    if (suit.isRed) Color(0xFFF87171) else TextLight
                }
            )

            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = suit.mnemonic,
                    fontSize = if (isActive) 15.sp else 12.sp,
                    fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = suit.localName,
                    fontSize = if (isActive) 10.sp else 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) EmeraldDeep.copy(alpha = 0.8f) else TextMuted.copy(alpha = 0.6f)
                )
            }
        }
    }
}

