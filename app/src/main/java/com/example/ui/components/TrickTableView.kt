package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayedCard
import com.example.model.Player
import com.example.model.Suit
import com.example.ui.theme.*

@Composable
fun TrickTableView(
    playedCards: List<PlayedCard>,
    allPlayers: List<String>,
    localPlayerName: String,
    trumpSuit: Suit?,
    leadSuit: Suit?,
    trickWinner: Player?,
    isTrickFinished: Boolean,
    onNextTrickClick: () -> Unit,
    modifier: Modifier = Modifier,
    is3DMode: Boolean = false
) {
    val numPlayers = allPlayers.size
    val myIdx = allPlayers.indexOf(localPlayerName).coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier.testTag("trick_table_view"),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        
        OvalTableCanvas(
            modifier = Modifier.fillMaxSize(),
            is3DMode = is3DMode
        )

        // Lead Suit Indicator
        if (leadSuit != null && !isTrickFinished) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.8f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("LEAD:", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(leadSuit.symbol, color = leadSuit.suitColor, fontSize = 14.sp)
                }
            }
        }

        if (playedCards.isEmpty()) {
            Text(
                text = "AWAITING LEAD",
                style = MaterialTheme.typography.labelMedium,
                color = GoldPrimary.copy(alpha = 0.3f),
                letterSpacing = 2.sp
            )
        } else {
            val isSmallTable = width < 380.dp || height < 240.dp
            val playedCardWidth = if (isSmallTable) 52.dp else 60.dp
            val playedCardHeight = playedCardWidth * 1.42f

            Box(modifier = Modifier.fillMaxSize()) {
                playedCards.forEach { played ->
                    val playerIdx = allPlayers.indexOf(played.player.name)
                    val relIdx = if (playerIdx != -1) (playerIdx - myIdx + numPlayers) % numPlayers else 0
                    
                    val (xOffset, yOffset) = when (relIdx) {
                        0 -> 0.dp to (height * 0.18f).coerceIn(24.dp, 50.dp)
                        1 -> if (numPlayers == 2) 0.dp to (-height * 0.18f).coerceIn((-50).dp, (-24).dp)
                             else (-width * 0.22f).coerceIn((-100).dp, (-35).dp) to 0.dp
                        2 -> if (numPlayers == 3) (width * 0.20f).coerceIn(35.dp, 90.dp) to (-height * 0.14f).coerceIn((-45).dp, (-20).dp)
                             else 0.dp to (-height * 0.18f).coerceIn((-50).dp, (-24).dp)
                        3 -> (width * 0.22f).coerceIn(35.dp, 100.dp) to 0.dp
                        else -> 0.dp to 0.dp
                    }

                    val isVisible = remember { mutableStateOf(false) }
                    LaunchedEffect(played.card.id) { isVisible.value = true }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = xOffset, y = yOffset),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedVisibility(
                            visible = isVisible.value,
                            enter = scaleIn(animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f)) + fadeIn()
                        ) {
                            PlayingCardView(
                                card = played.card,
                                isTrump = played.card.suit == trumpSuit,
                                isPlayable = false,
                                isSelected = trickWinner?.id == played.player.id && isTrickFinished,
                                width = playedCardWidth,
                                height = playedCardHeight
                            )
                        }
                    }
                }
            }
        }

        // Winner overlay
        if (isTrickFinished && trickWinner != null) {
            Button(
                onClick = onNextTrickClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(bottom = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "${trickWinner.name} won! Next Trick »",
                    color = DeepEmerald,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
