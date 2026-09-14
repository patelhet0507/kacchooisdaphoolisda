package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted

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
    is3DMode: Boolean = false
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 600

    val borderColor by animateColorAsState(
        targetValue = if (isCurrentTurn) GoldPrimary else if (is3DMode) GoldLight else EmeraldBorder.copy(alpha = 0.6f),
        label = "border_color"
    )

    val avatarSize = if (isSmallScreen) {
        if (isBottomUser) 36.dp else 30.dp
    } else {
        if (isBottomUser) {
            if (is3DMode) 52.dp else 46.dp
        } else {
            if (is3DMode) 46.dp else 40.dp
        }
    }

    val chairWidth = if (isBottomUser) 72.dp else 60.dp
    val chairHeight = if (isBottomUser) 36.dp else 30.dp
    val chairOffset = if (isBottomUser) 18.dp else 14.dp

    Column(
        modifier = modifier.testTag("player_seat_${player.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (is3DMode && !isSmallScreen) {
                // 3D Chair Seat Base (Cushion & Backrest visual) - Hidden in compact mode to maximize space
                Box(
                    modifier = Modifier
                        .size(chairWidth, chairHeight)
                        .offset(y = chairOffset)
                        .shadow(8.dp, RoundedCornerShape(50))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(0xFF8B5A2B), Color(0xFF5C3A21))
                            )
                        )
                        .border(1.5.dp, GoldPrimary.copy(alpha = 0.8f), RoundedCornerShape(50))
                )
            }

            // Main avatar circle with 3D elevation if is3DMode is true
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .shadow(if (is3DMode && !isSmallScreen) 14.dp else if (isCurrentTurn) 8.dp else 2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = if (is3DMode && !isSmallScreen) {
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(player.colorHex), Color(player.colorHex).copy(alpha = 0.6f))
                            )
                        } else {
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color(player.colorHex).copy(alpha = 0.25f), Color(player.colorHex).copy(alpha = 0.25f))
                            )
                        }
                    )
                    .border(
                        width = if (is3DMode && !isSmallScreen) 3.dp else if (isCurrentTurn) 2.5.dp else 1.5.dp,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.avatarEmoji,
                    fontSize = if (isSmallScreen) {
                        if (isBottomUser) 16.sp else 14.sp
                    } else {
                        if (isBottomUser) (if (is3DMode) 26.sp else 22.sp) else (if (is3DMode) 22.sp else 18.sp)
                    }
                )
            }

            // Floating Active Emote Bubble
            if (!activeEmote.isNullOrBlank()) {
                EmoteBubbleView(
                    emoteEmoji = activeEmote,
                    modifier = Modifier.offset(y = if (isSmallScreen) (-22).dp else (-32).dp)
                )
            }

            // Dealer chip "D"
            if (isDealer) {
                Box(
                    modifier = Modifier
                        .offset(
                            x = if (isSmallScreen) 12.dp else (if (is3DMode) 20.dp else 18.dp),
                            y = if (isSmallScreen) (-8).dp else (-12).dp
                        )
                        .size(if (isSmallScreen) 14.dp else 22.dp)
                        .shadow(if (is3DMode && !isSmallScreen) 6.dp else 2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .border(1.dp, GoldLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = EmeraldDeep,
                        fontSize = if (isSmallScreen) 8.sp else 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(if (isSmallScreen) 1.dp else 3.dp))

        // Player Name & Score
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isBottomUser) "You" else player.name,
                color = if (isCurrentTurn) GoldLight else TextLight,
                fontSize = if (isSmallScreen) 10.sp else 12.sp,
                fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "($totalScore)",
                color = GoldPrimary,
                fontSize = if (isSmallScreen) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Status / Bid & Tricks Badge
        Box(
            modifier = Modifier
                .padding(top = if (isSmallScreen) 1.dp else 2.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isCurrentTurn && turnActionText != null) {
                        GoldPrimary.copy(alpha = 0.2f)
                    } else {
                        DarkSurface.copy(alpha = 0.85f)
                    }
                )
                .border(
                    1.dp,
                    if (isCurrentTurn) GoldPrimary.copy(alpha = 0.5f) else EmeraldBorder.copy(alpha = 0.3f),
                    RoundedCornerShape(6.dp)
                )
                .padding(
                    horizontal = if (isSmallScreen) 4.dp else 6.dp,
                    vertical = if (isSmallScreen) 1.dp else 2.dp
                )
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
