package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.theme.*
import kotlin.math.min

@Composable
fun PlayerSeatView(
    player: Player,
    bid: Int?,
    tricksWon: Int,
    isDealer: Boolean,
    isCurrentTurn: Boolean,
    turnActionText: String? = null,
    totalScore: Int = 0,
    activeEmote: String? = null,
    modifier: Modifier = Modifier,
    isBottomUser: Boolean = false,
    is3DMode: Boolean = false,
    cardCount: Int = 0
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 600

    val infiniteTransition = rememberInfiniteTransition(label = "turn_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    val avatarSize = if (isSmallScreen) {
        if (isBottomUser) 38.dp else 32.dp
    } else {
        if (isBottomUser) 54.dp else 46.dp
    }

    Column(
        modifier = modifier.testTag("player_seat_${player.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Turn indicator glow ring
            if (isCurrentTurn) {
                Box(
                    modifier = Modifier
                        .size(avatarSize + 12.dp)
                        .graphicsLayer {
                            scaleX = glowScale
                            scaleY = glowScale
                        }
                        .border(2.dp, GoldPrimary.copy(alpha = glowAlpha), CircleShape)
                )
            }

            // Chair Visual
            Box(
                modifier = Modifier
                    .size(if (isBottomUser) 76.dp else 64.dp, if (isBottomUser) 40.dp else 34.dp)
                    .offset(y = if (isBottomUser) 16.dp else 12.dp)
                    .shadow(if (is3DMode) 8.dp else 4.dp, RoundedCornerShape(50))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(ChairCushion, ChairCushionDark)
                        ),
                        shape = RoundedCornerShape(50)
                    )
                    .border(1.dp, WoodRail.copy(alpha = 0.5f), RoundedCornerShape(50))
            )
            
            if (is3DMode) {
                // Backrest (simplified arc)
                Canvas(modifier = Modifier
                    .size(if (isBottomUser) 60.dp else 50.dp, if (isBottomUser) 24.dp else 20.dp)
                    .offset(y = if (isBottomUser) (-22).dp else (-18).dp)
                ) {
                    drawArc(
                        brush = Brush.verticalGradient(listOf(ChairCushion, ChairCushionDark)),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.1f),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        style = Stroke(width = 1f)
                    )
                }
            }

            // Card fan indicators behind avatar
            if (cardCount > 0 && !isBottomUser) {
                Row(
                    modifier = Modifier.offset(y = (-avatarSize/2) - 4.dp),
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    repeat(min(cardCount, 3)) { i ->
                        Box(
                            modifier = Modifier
                                .size(12.dp, 16.dp)
                                .graphicsLayer { rotationZ = (i - 1) * 15f }
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color(0xFFB91C1C)) // Red card back
                                .border(0.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("♣", color = Color.White.copy(alpha = 0.3f), fontSize = 6.sp)
                        }
                    }
                }
            }

            // Main avatar circle
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .shadow(if (is3DMode) 10.dp else 4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(player.colorHex), Color(player.colorHex).copy(alpha = 0.7f))
                        )
                    )
                    .border(
                        width = if (isCurrentTurn) 2.dp else 1.dp,
                        color = if (isCurrentTurn) GoldPrimary else Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.avatarEmoji,
                    fontSize = if (isSmallScreen) {
                        if (isBottomUser) 18.sp else 16.sp
                    } else {
                        if (isBottomUser) 28.sp else 24.sp
                    }
                )
            }

            // Dealer chip "D"
            if (isDealer) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(if (isSmallScreen) 16.dp else 20.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .border(1.dp, GoldLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = EmeraldDeep,
                        fontSize = if (isSmallScreen) 9.sp else 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            if (!activeEmote.isNullOrBlank()) {
                EmoteBubbleView(
                    emoteEmoji = activeEmote,
                    modifier = Modifier.offset(y = (-avatarSize/2) - 20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

        // Player Name Plate
        Box(
            modifier = Modifier
                .width(if (isSmallScreen) 70.dp else 86.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(WoodRailDark)
                .border(1.dp, WoodRail, RoundedCornerShape(4.dp))
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isBottomUser) "You" else player.name,
                    color = GoldLight,
                    fontSize = if (isSmallScreen) 10.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "($totalScore)",
                    color = GoldPrimary,
                    fontSize = if (isSmallScreen) 9.sp else 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Status / Bid & Tricks Badge
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(DarkSurface.copy(alpha = 0.8f))
                .border(0.5.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            if (isCurrentTurn && turnActionText != null) {
                Text(
                    text = turnActionText,
                    color = GoldLight,
                    fontSize = if (isSmallScreen) 8.sp else 10.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "Bid: ${bid?.toString() ?: "-"}",
                        color = if (bid != null) TextLight else TextMuted,
                        fontSize = if (isSmallScreen) 8.sp else 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = TextMuted,
                        fontSize = if (isSmallScreen) 7.sp else 8.sp
                    )
                    val isGoalMet = bid != null && tricksWon == bid
                    Text(
                        text = "Won: $tricksWon",
                        color = if (isGoalMet) SuccessGreen else GoldLight,
                        fontSize = if (isSmallScreen) 8.sp else 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
