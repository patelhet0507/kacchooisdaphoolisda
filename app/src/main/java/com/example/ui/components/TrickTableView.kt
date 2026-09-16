package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayedCard
import com.example.model.Player
import com.example.model.Suit
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldFelt
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted

@Composable
fun TrickTableView(
    playedCards: List<PlayedCard>,
    trumpSuit: Suit,
    leadSuit: Suit?,
    trickWinner: Player?,
    isTrickFinished: Boolean,
    onNextTrickClick: () -> Unit,
    modifier: Modifier = Modifier,
    is3DMode: Boolean = false
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 600
    val tableMinHeight = if (isSmallScreen) 180.dp else 260.dp
    val tableMaxHeight = if (isSmallScreen) 280.dp else 420.dp
    val outerShapeRadius = if (isSmallScreen) 24.dp else 40.dp
    val centerRing1Size = if (isSmallScreen) 100.dp else 180.dp
    val centerRing2Size = if (isSmallScreen) 130.dp else 210.dp

    BoxWithConstraints(
        modifier = modifier
            .testTag("trick_table_view"),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        
        OvalTableCanvas(
            modifier = Modifier.fillMaxSize(),
            is3DMode = is3DMode
        )

        // Display current lead suit if active
        if (leadSuit != null && !isTrickFinished) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = if (isSmallScreen) 12.dp else 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(DarkSurface.copy(alpha = 0.85f), DarkSurfaceElevated.copy(alpha = 0.85f))
                        )
                    )
                    .border(1.5.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "LEAD:",
                        color = TextMuted,
                        fontSize = if (isSmallScreen) 10.sp else 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${leadSuit.symbol} ${leadSuit.displayName}",
                        color = if (leadSuit.isRed) Color(0xFFF87171) else GoldLight,
                        fontSize = if (isSmallScreen) 11.sp else 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        if (playedCards.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 8.dp)
            ) {
                Text(
                    text = "♠ ♣ ♥ ♦",
                    color = GoldLight.copy(alpha = 0.3f),
                    fontSize = if (isSmallScreen) 18.sp else 26.sp,
                    letterSpacing = if (isSmallScreen) 4.sp else 8.sp
                )
                Text(
                    text = "Awaiting Lead...",
                    color = TextMuted.copy(alpha = 0.7f),
                    fontSize = if (isSmallScreen) 12.sp else 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            // Display cards in trick (slightly staggered or arranged)
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 8.dp else 14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                playedCards.forEach { played ->
                    val isTrump = played.card.suit == trumpSuit
                    val isWinner = trickWinner?.id == played.player.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 4.dp else 6.dp)
                    ) {
                        PlayingCardView(
                            card = played.card,
                            isTrump = isTrump,
                            isPlayable = false,
                            isSelected = isWinner && isTrickFinished,
                            width = if (isSmallScreen) 50.dp else 74.dp,
                            height = if (isSmallScreen) 72.dp else 106.dp,
                            is3DMode = is3DMode
                        )

                        // Name of player who played this card
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isWinner && isTrickFinished) GoldPrimary else DarkSurface.copy(alpha = 0.95f)
                                )
                                .border(
                                    1.dp,
                                    if (isWinner && isTrickFinished) GoldLight else EmeraldBorder.copy(alpha = 0.6f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = if (isSmallScreen) 6.dp else 10.dp, vertical = if (isSmallScreen) 2.dp else 4.dp)
                        ) {
                            Text(
                                text = played.player.name,
                                color = if (isWinner && isTrickFinished) EmeraldDeep else TextLight,
                                fontSize = if (isSmallScreen) 9.sp else 11.sp,
                                fontWeight = if (isWinner && isTrickFinished) FontWeight.Black else FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Winner overlay banner when trick is finished
        AnimatedVisibility(
            visible = isTrickFinished && trickWinner != null,
            enter = fadeIn() + scaleIn(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = if (isSmallScreen) 4.dp else 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onNextTrickClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = EmeraldDeep
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier.testTag("next_trick_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = EmeraldDeep,
                            modifier = Modifier.size(if (isSmallScreen) 12.dp else 16.dp)
                        )
                        Text(
                            text = "${trickWinner?.name} won! Next Trick »",
                            color = EmeraldDeep,
                            fontWeight = FontWeight.Black,
                            fontSize = if (isSmallScreen) 10.sp else 12.sp
                        )
                    }
                }
            }
        }
    }
}

