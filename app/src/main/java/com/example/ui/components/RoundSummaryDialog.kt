package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.90f).dp

    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDialogHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ROUND $roundNumber SUMMARY",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldLight,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "TRUMP: ${trumpSuit.symbol} ${trumpSuit.displayName.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = trumpSuit.suitColor
                    )
                }

                // Stats List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    playerStates.forEach { state ->
                        val isSuccess = state.bid != null && state.bid == state.tricksWon
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSuccess) SuccessGreen.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.02f))
                                .border(0.5.dp, if (isSuccess) SuccessGreen.copy(alpha = 0.3f) else GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
                                    Text(state.player.avatarEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = state.player.name.uppercase(),
                                        color = TextLight,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("BID/WON", fontSize = 8.sp, color = TextMuted)
                                        Text("${state.bid ?: "-"}/${state.tricksWon}", color = if (isSuccess) SuccessGreen else GoldLight, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("ROUND", fontSize = 8.sp, color = TextMuted)
                                        Text("+${state.roundScore}", color = if (isSuccess) SuccessGreen else ErrorRed, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("TOTAL", fontSize = 8.sp, color = TextMuted)
                                        Text("${state.totalScore}", color = GoldPrimary, fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                if (!isLastRound && nextTrumpSuit != null) {
                    Text(
                        text = "NEXT TRUMP: ${nextTrumpSuit.symbol} ${nextTrumpSuit.displayName.uppercase()}",
                        color = GoldLight.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                PremiumButton(
                    text = if (isLastRound) "VIEW RESULTS" else "NEXT ROUND",
                    onClick = onContinueClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
