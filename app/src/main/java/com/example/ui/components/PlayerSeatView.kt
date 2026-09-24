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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
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
            // Turn Countdown Circular Progress Indicator (30s max turn timer)
            if (isCurrentTurn) {
                var secondsLeft by remember(player.id, isCurrentTurn) { mutableIntStateOf(30) }
                val animatedProgress = remember(player.id, isCurrentTurn) { Animatable(1f) }

                LaunchedEffect(player.id, isCurrentTurn) {
                    animatedProgress.snapTo(1f)
                    // Animate 30s countdown
                    animatedProgress.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 30000, easing = LinearEasing)
                    )
                }

                LaunchedEffect(player.id, isCurrentTurn) {
                    secondsLeft = 30
                    while (secondsLeft > 0) {
                        delay(1000L)
                        secondsLeft--
                    }
                }

                val timerColor = when {
                    secondsLeft > 15 -> GoldPrimary
                    secondsLeft > 5 -> Color(0xFFF97316) // Warning Orange
                    else -> Color(0xFFEF4444) // Urgent Red
                }

                Canvas(
                    modifier = Modifier.size(avatarSize + 12.dp)
                ) {
                    // Background track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    // Animated countdown sweep
                    drawArc(
                        color = timerColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress.value,
                        useCenter = false,
                        style = Stroke(width = 3.5.dp.toPx())
                    )
                }
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

            // Turn Timer Seconds Badge
            if (isCurrentTurn) {
                var badgeSecs by remember(player.id, isCurrentTurn) { mutableIntStateOf(30) }
                LaunchedEffect(player.id, isCurrentTurn) {
                    badgeSecs = 30
                    while (badgeSecs > 0) {
                        delay(1000L)
                        badgeSecs--
                    }
                }
                val badgeColor = when {
                    badgeSecs > 15 -> GoldLight
                    badgeSecs > 5 -> Color(0xFFFED7AA)
                    else -> Color(0xFFFCA5A5)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.9f))
                        .border(1.dp, badgeColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "${badgeSecs}s",
                        color = badgeColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
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
