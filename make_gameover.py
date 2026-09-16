content = """package com.example.ui.components

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

    val configuration = LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.88f).dp
    val dialogWidthFraction = if (configuration.screenWidthDp > 600) 0.65f else 0.92f

    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ConfettiOverlay(modifier = Modifier.fillMaxSize(), particleCount = 90)

            Card(
                modifier = Modifier
                    .fillMaxWidth(dialogWidthFraction)
                    .heightIn(max = maxDialogHeight)
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆",
                            fontSize = 42.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Game Finished!",
                            color = GoldLight,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        if (winner != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Winner: ${winner.player.name} 🎉",
                                color = GoldPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Scrollable Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Newly Unlocked Achievements Notification Banner
                        if (newlyUnlockedAchievements.isNotEmpty()) {
                            newlyUnlockedAchievements.forEach { achievement ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.2f)),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(text = achievement.emoji, fontSize = 24.sp)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "🏆 Achievement Unlocked!",
                                                color = GoldLight,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                            Text(
                                                text = achievement.title,
                                                color = TextLight,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = achievement.description,
                                                color = TextMuted,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

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

                    // Footer Buttons
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
            }
        }
    }
}
"""

with open('app/src/main/java/com/example/ui/components/GameOverDialog.kt', 'w') as f:
    f.write(content)
print("GameOverDialog.kt written via python script")
