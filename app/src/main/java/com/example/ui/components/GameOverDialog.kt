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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted

@Composable
fun GameOverDialog(
    playerStates: List<PlayerRoundState>,
    onPlayAgain: () -> Unit,
    onHomeClick: () -> Unit
) {
    val sortedByScore = playerStates.sortedByDescending { it.totalScore }
    val winner = sortedByScore.firstOrNull()

    AlertDialog(
        onDismissRequest = { /* Modal */ },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆",
                    fontSize = 42.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Game Finished!",
                    color = GoldLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${winner?.player?.name ?: "Champion"} wins the Kaachu Phool match!",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
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
                sortedByScore.forEachIndexed { index, state ->
                    val medal = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "#${index + 1}"
                    }

                    val isFirst = index == 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isFirst) GoldPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated)
                            .border(
                                1.5.dp,
                                if (isFirst) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = medal,
                                fontSize = if (index < 3) 20.sp else 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextLight
                            )
                            Text(
                                text = state.player.avatarEmoji,
                                fontSize = 18.sp
                            )
                            Text(
                                text = state.player.name,
                                color = if (isFirst) GoldLight else TextLight,
                                fontSize = 14.sp,
                                fontWeight = if (isFirst) FontWeight.Black else FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "${state.totalScore} pts",
                            color = if (isFirst) GoldPrimary else TextLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("play_again_button")
                ) {
                    Text(
                        text = "Play Again 🃏",
                        color = EmeraldDeep,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onHomeClick,
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(EmeraldBorder)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_button")
                ) {
                    Text(
                        text = "Back to Main Menu",
                        color = TextLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    )
}
