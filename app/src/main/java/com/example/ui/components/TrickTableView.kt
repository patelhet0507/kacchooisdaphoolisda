package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .shadow(10.dp, RoundedCornerShape(28.dp), spotColor = GoldDark.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        EmeraldFelt,
                        EmeraldDeep,
                        Color(0xFF03140A)
                    ),
                    radius = 900f
                )
            )
            .border(
                2.5.dp,
                Brush.linearGradient(listOf(GoldLight.copy(alpha = 0.6f), EmeraldBorder, GoldDark.copy(alpha = 0.7f))),
                RoundedCornerShape(28.dp)
            )
            .testTag("trick_table_view"),
        contentAlignment = Alignment.Center
    ) {
        // Decorative center felt emblem ring
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .border(1.5.dp, GoldLight.copy(alpha = 0.15f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .border(1.dp, EmeraldBorder.copy(alpha = 0.20f), CircleShape)
        )

        // Display current lead suit if active
        if (leadSuit != null && !isTrickFinished) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(DarkSurface.copy(alpha = 0.95f), DarkSurfaceElevated)
                        )
                    )
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Lead Suit:",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${leadSuit.symbol} ${leadSuit.displayName} (${leadSuit.localName})",
                        color = if (leadSuit.isRed) Color(0xFFF87171) else GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (playedCards.isEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "♠ ♣ ♥ ♦",
                    color = GoldLight.copy(alpha = 0.4f),
                    fontSize = 18.sp,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "Lead a card to start the trick",
                    color = TextMuted.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Display cards in trick side-by-side
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                playedCards.forEach { played ->
                    val isTrump = played.card.suit == trumpSuit
                    val isWinner = trickWinner?.id == played.player.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PlayingCardView(
                            card = played.card,
                            isTrump = isTrump,
                            isPlayable = false,
                            isSelected = isWinner && isTrickFinished,
                            width = 56.dp,
                            height = 80.dp
                        )

                        // Name of player who played this card
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isWinner && isTrickFinished) GoldPrimary else DarkSurface.copy(alpha = 0.95f)
                                )
                                .border(
                                    1.dp,
                                    if (isWinner && isTrickFinished) GoldLight else EmeraldBorder.copy(alpha = 0.6f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = played.player.name,
                                color = if (isWinner && isTrickFinished) EmeraldDeep else TextLight,
                                fontSize = 10.sp,
                                fontWeight = if (isWinner && isTrickFinished) FontWeight.Black else FontWeight.SemiBold
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
                    .padding(bottom = 10.dp),
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
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${trickWinner?.name} won the trick! Next Trick »",
                            color = EmeraldDeep,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

