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
    val maxDialogHeight = (configuration.screenHeightDp * 0.88f).dp
    val dialogWidthFraction = if (configuration.screenWidthDp > 600) 0.65f else 0.92f

    Dialog(
        onDismissRequest = { /* Modal: must select a bid */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
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
                        text = "Make Your Judgement (Bid) 🎯",
                        color = GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "How many tricks will you win in this round?",
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                // Scrollable content area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Hook Rule Alert for dealer
                    if (isDealer && forbiddenBid != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorRed.copy(alpha = 0.15f))
                                .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .clickable {
                                    soundEffectsManager.playDealerHookAlert()
                                    showHookExplanation = true
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = "Forbidden Hook Rule",
                                    tint = ErrorRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "DEALER HOOK: Cannot bid $forbiddenBid",
                                        color = ErrorRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Tap to view rule explanation",
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    // Grid of selectable bids from 0 to totalCards
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (bidOption in 0..totalCards) {
                            val isForbidden = isDealer && forbiddenBid != null && bidOption == forbiddenBid
                            val isSelected = selectedBid == bidOption

                            val bgColor = when {
                                isForbidden -> HookForbiddenColor.copy(alpha = 0.2f)
                                isSelected -> GoldPrimary
                                else -> DarkSurfaceElevated
                            }

                            val textColor = when {
                                isForbidden -> ErrorRed
                                isSelected -> EmeraldDeep
                                else -> TextLight
                            }

                            val borderColor = when {
                                isForbidden -> ErrorRed.copy(alpha = 0.6f)
                                isSelected -> GoldLight
                                else -> EmeraldBorder.copy(alpha = 0.4f)
                            }

                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(bgColor)
                                    .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isForbidden) {
                                            soundEffectsManager.playDealerHookAlert()
                                            showHookExplanation = true
                                        } else {
                                            soundEffectsManager.playButtonTap()
                                            selectedBid = bidOption
                                        }
                                    }
                                    .testTag("bid_option_$bidOption"),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = bidOption.toString(),
                                        color = textColor,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    if (isForbidden) {
                                        Text(
                                            text = "HOOK",
                                            color = ErrorRed,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = {
                            selectedBid?.let {
                                soundEffectsManager.playButtonTap()
                                onBidSelected(it)
                            }
                        },
                        enabled = selectedBid != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            disabledContainerColor = DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("confirm_bid_button")
                    ) {
                        Text(
                            text = if (selectedBid != null) "Confirm Bid: $selectedBid" else "Select a number",
                            color = if (selectedBid != null) EmeraldDeep else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    if (onLeaveMatch != null) {
                        TextButton(
                            onClick = onLeaveMatch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bidding_leave_button")
                        ) {
                            Text("Leave Match", color = Color(0xFFEF4444), fontSize = 13.sp)
                        }
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
                    .fillMaxWidth(dialogWidthFraction)
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
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
