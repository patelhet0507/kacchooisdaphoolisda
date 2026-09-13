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
    modifier: Modifier = Modifier,
    isBottomUser: Boolean = false
) {
    val borderColor by animateColorAsState(
        targetValue = if (isCurrentTurn) GoldPrimary else EmeraldBorder.copy(alpha = 0.6f),
        label = "border_color"
    )

    Column(
        modifier = modifier.testTag("player_seat_${player.id}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Main avatar circle
            Box(
                modifier = Modifier
                    .size(if (isBottomUser) 46.dp else 40.dp)
                    .shadow(if (isCurrentTurn) 8.dp else 2.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color(player.colorHex).copy(alpha = 0.25f))
                    .border(
                        width = if (isCurrentTurn) 2.5.dp else 1.5.dp,
                        color = borderColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = player.avatarEmoji,
                    fontSize = if (isBottomUser) 22.sp else 18.sp
                )
            }

            // Dealer chip "D"
            if (isDealer) {
                Box(
                    modifier = Modifier
                        .offset(x = 18.dp, y = (-12).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary)
                        .border(1.dp, GoldLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "D",
                        color = EmeraldDeep,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Player Name & Score
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = if (isBottomUser) "You" else player.name,
                color = if (isCurrentTurn) GoldLight else TextLight,
                fontSize = 12.sp,
                fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "($totalScore)",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Status / Bid & Tricks Badge
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .clip(RoundedCornerShape(8.dp))
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
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            if (isCurrentTurn && turnActionText != null) {
                Text(
                    text = turnActionText,
                    color = GoldLight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Bid: ${bid?.toString() ?: "-"}",
                        color = if (bid != null) TextLight else TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = TextMuted,
                        fontSize = 8.sp
                    )
                    val isGoalMet = bid != null && tricksWon == bid
                    Text(
                        text = "Won: $tricksWon",
                        color = if (isGoalMet) SuccessGreen else GoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
