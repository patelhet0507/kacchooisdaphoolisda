package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val avatarSize = if (isBottomUser) 52.dp else if (isSmallScreen) 40.dp else 46.dp

    Column(
        modifier = modifier
            .widthIn(max = 88.dp)
            .testTag("player_seat_${player.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Animated Turn Indicator
            if (isCurrentTurn) {
                Box(
                    modifier = Modifier
                        .size(avatarSize + 12.dp)
                        .border(
                            2.dp,
                            Brush.sweepGradient(listOf(GoldPrimary, GoldLight, GoldPrimary)),
                            CircleShape
                        )
                        .graphicsLayer { alpha = glowAlpha }
                )
            }

            // Avatar Circle
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(DeepEmerald)
                    .border(
                        if (isCurrentTurn) 2.dp else 1.dp,
                        if (isCurrentTurn) GoldPrimary else GoldPrimary.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.avatarEmoji,
                    fontSize = if (isBottomUser) 28.sp else 24.sp
                )
            }

            // Dealer Chip
            if (isDealer) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .border(1.dp, GoldLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("D", color = DeepEmerald, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            // Emote
            if (!activeEmote.isNullOrBlank()) {
                EmoteBubbleView(
                    emoteEmoji = activeEmote,
                    modifier = Modifier.offset(y = (-avatarSize/2) - 20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Name & Score Plate
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = if (isBottomUser) "YOU" else player.name.uppercase(),
                color = if (isCurrentTurn) GoldLight else TextLight,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Bid/Won Status
                val isGoalMet = bid != null && tricksWon == bid
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isGoalMet) SuccessGreen.copy(alpha = 0.2f) else DarkSurface.copy(alpha = 0.6f))
                        .border(0.5.dp, if (isGoalMet) SuccessGreen.copy(alpha = 0.5f) else GoldPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${bid ?: "-"}/$tricksWon",
                        color = if (isGoalMet) SuccessGreen else GoldPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Total Score
                Text(
                    text = "($totalScore)",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
