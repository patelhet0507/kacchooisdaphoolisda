package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.engine.SoundEffectsManager
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.HookForbiddenColor
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BiddingDialog(
    totalCards: Int,
    isDealer: Boolean,
    forbiddenBid: Int?,
    onBidSelected: (Int) -> Unit,
    onLeaveMatch: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val soundEffectsManager = remember { SoundEffectsManager.getInstance(context) }
    var selectedBid by remember { mutableStateOf<Int?>(null) }
    var showHookExplanation by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.90f).dp

    Dialog(
        onDismissRequest = { /* Modal */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 500.dp)
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
                        text = "YOUR JUDGEMENT",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldLight,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "HOW MANY TRICKS WILL YOU WIN?",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                // Hook Alert
                if (isDealer && forbiddenBid != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ErrorRed.copy(alpha = 0.1f))
                            .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable {
                                soundEffectsManager.playDealerHookAlert()
                                showHookExplanation = true
                            }
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Block, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
                            Text(
                                text = "DEALER HOOK: CANNOT BID $forbiddenBid",
                                color = ErrorRed,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (bidOption in 0..totalCards) {
                        val isForbidden = isDealer && forbiddenBid != null && bidOption == forbiddenBid
                        val isSelected = selectedBid == bidOption

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldPrimary else if (isForbidden) ErrorRed.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.05f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    brush = if (isSelected) Brush.linearGradient(listOf(GoldLight, GoldPrimary)) else androidx.compose.ui.graphics.SolidColor(if (isForbidden) ErrorRed.copy(alpha = 0.3f) else GoldPrimary.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isForbidden) {
                                        soundEffectsManager.playDealerHookAlert()
                                        showHookExplanation = true
                                    } else {
                                        soundEffectsManager.playButtonTap()
                                        selectedBid = bidOption
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bidOption.toString(),
                                color = if (isSelected) DeepEmerald else if (isForbidden) ErrorRed else GoldLight,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // Action
                PremiumButton(
                    text = if (selectedBid != null) "CONFIRM BID: $selectedBid" else "SELECT A BID",
                    onClick = { selectedBid?.let { onBidSelected(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = selectedBid != null
                )

                if (onLeaveMatch != null) {
                    TextButton(onClick = onLeaveMatch) {
                        Text("LEAVE MATCH", color = ErrorRed.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }

    // Detailed explanation modal for Hook Rule
    if (showHookExplanation) {
        Dialog(
            onDismissRequest = { showHookExplanation = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = maxDialogHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "The Hook Rule (Bandi / Nasti) 🪝",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "As the dealer bidding last, you are not allowed to bid a number that makes the sum of all players' bids equal to the total cards dealt ($totalCards).\n\n" +
                                "This timeless Kaachu Phool rule guarantees that it is impossible for all players to succeed in their bids—at least one player will always be caught!",
                        color = TextLight,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Button(
                        onClick = { showHookExplanation = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got It", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
