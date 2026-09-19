package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.example.model.Achievement
import com.example.model.PlayerRoundState
import com.example.ui.theme.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun GameOverDialog(
    playerStates: List<PlayerRoundState>,
    newlyUnlockedAchievements: List<Achievement> = emptyList(),
    onPlayAgain: () -> Unit,
    onHomeClick: () -> Unit
) {
    val sortedByScore = playerStates.sortedByDescending { it.totalScore }
    val winner = sortedByScore.firstOrNull()

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.90f).dp

    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize(), particleCount = 100)

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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🏆", fontSize = 48.sp)
                        Text(
                            text = "GAME FINISHED",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        if (winner != null) {
                            Text(
                                text = "WINNER: ${winner.player.name.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Scoreboard
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sortedByScore.forEachIndexed { index, state ->
                            val isWinner = index == 0
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isWinner) GoldPrimary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f))
                                    .border(1.dp, if (isWinner) GoldPrimary else GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (index == 0) "🥇" else if (index == 1) "🥈" else if (index == 2) "🥉" else "#${index + 1}", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(state.player.avatarEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            state.player.name.uppercase(),
                                            color = if (isWinner) GoldLight else TextLight,
                                            fontWeight = FontWeight.Black,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                    Text(
                                        "${state.totalScore} PTS",
                                        color = if (isWinner) GoldPrimary else GoldLight,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }

                    // Actions
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        PremiumButton(
                            text = "PLAY AGAIN",
                            onClick = onPlayAgain,
                            modifier = Modifier.fillMaxWidth()
                        )
                        PremiumButton(
                            text = "MAIN MENU",
                            onClick = onHomeClick,
                            modifier = Modifier.fillMaxWidth(),
                            isPrimary = false
                        )
                    }
                }
            }
        }
    }
}
