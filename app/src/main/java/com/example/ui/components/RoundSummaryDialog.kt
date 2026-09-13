package com.example.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerRoundState
import com.example.model.Suit
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted

@Composable
fun RoundSummaryDialog(
    roundNumber: Int,
    totalRounds: Int,
    trumpSuit: Suit,
    nextTrumpSuit: Suit?,
    playerStates: List<PlayerRoundState>,
    isLastRound: Boolean,
    onContinueClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* Modal */ },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Round $roundNumber Summary",
                    color = GoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Trump was ${trumpSuit.displayName} (${trumpSuit.symbol} ${trumpSuit.mnemonic})",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Player", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.8f))
                    Text(text = "Bid", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(text = "Won", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                    Text(text = "Round", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.Center)
                    Text(text = "Total", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                }

                // Table Rows
                playerStates.forEach { state ->
                    val isSuccess = state.bid != null && state.bid == state.tricksWon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSuccess) SuccessGreen.copy(alpha = 0.08f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSuccess) SuccessGreen.copy(alpha = 0.3f) else EmeraldBorder.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Name
                        Row(
                            modifier = Modifier.weight(1.8f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = state.player.avatarEmoji, fontSize = 12.sp)
                            Text(
                                text = state.player.name,
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Bid
                        Text(
                            text = "${state.bid ?: "-"}",
                            color = TextLight,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        // Won
                        Text(
                            text = "${state.tricksWon}",
                            color = if (isSuccess) SuccessGreen else TextLight,
                            fontSize = 12.sp,
                            fontWeight = if (isSuccess) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )

                        // Round Score
                        Text(
                            text = if (state.roundScore > 0) "+${state.roundScore}" else "0",
                            color = if (isSuccess) SuccessGreen else ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.Center
                        )

                        // Total Score
                        Text(
                            text = "${state.totalScore}",
                            color = GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Next round preview
                if (!isLastRound && nextTrumpSuit != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Next Round Trump: ${nextTrumpSuit.displayName} ${nextTrumpSuit.symbol} (${nextTrumpSuit.mnemonic})",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onContinueClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("summary_continue_button")
            ) {
                Text(
                    text = if (isLastRound) "View Game Results 🏆" else "Start Next Round »",
                    color = EmeraldDeep,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
        }
    )
}
