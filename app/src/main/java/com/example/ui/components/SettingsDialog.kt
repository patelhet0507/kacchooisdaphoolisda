package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SettingsManager
import com.example.data.UserProfileManager
import com.example.engine.SoundEffectsManager
import com.example.model.CustomizationData
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    userProfileManager: UserProfileManager,
    onDismiss: () -> Unit,
    onOpenRules: () -> Unit = {},
    onOpenCustomization: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    onAccountDeleted: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val soundEffectsManager = remember { SoundEffectsManager.getInstance(context) }

    val appSettings by settingsManager.settings.collectAsStateWithLifecycle()
    val userProfile by userProfileManager.state.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var statusNotification by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(12.dp),
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
                // Header with Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = "Game Settings ⚙️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (statusNotification != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF064E3B).copy(alpha = 0.6f))
                                .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = statusNotification!!,
                                color = Color(0xFF6EE7B7),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                // ====================================================
                // 1. SOUND EFFECTS & AUDIO
                // ====================================================
                SettingsSectionCard(title = "CARD SOUND EFFECTS & AUDIO", icon = Icons.Default.VolumeUp) {
                    // Sound Master Switch
                    SettingsToggleRow(
                        title = "Card Sound Effects",
                        subtitle = "Tactile card deal, slap, flip, trick win & fanfare audio",
                        checked = appSettings.soundEffectsEnabled,
                        onCheckedChange = {
                            settingsManager.setSoundEffectsEnabled(it)
                            if (it) soundEffectsManager.playButtonTap()
                        },
                        testTag = "toggle_sound_effects"
                    )

                    // Volume Slider
                    if (appSettings.soundEffectsEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Sound Volume",
                                    color = TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(appSettings.soundVolume * 100).toInt()}%",
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Slider(
                                value = appSettings.soundVolume,
                                onValueChange = { settingsManager.setSoundVolume(it) },
                                onValueChangeFinished = { soundEffectsManager.playCardPlay() },
                                colors = SliderDefaults.colors(
                                    thumbColor = GoldPrimary,
                                    activeTrackColor = GoldPrimary,
                                    inactiveTrackColor = DarkSurfaceElevated
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Haptic Feedback
                    SettingsToggleRow(
                        title = "Haptic Vibrations",
                        subtitle = "Tactile feedback when tapping cards and placing bids",
                        checked = appSettings.hapticFeedbackEnabled,
                        onCheckedChange = {
                            settingsManager.setHapticFeedbackEnabled(it)
                            if (it) soundEffectsManager.vibrate(30, 150)
                        },
                        testTag = "toggle_haptics"
                    )

                    // Interactive Sound Test Board
                    if (appSettings.soundEffectsEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "TEST SOUND EFFECTS",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SoundTestChip("🃏 Deal", Modifier.weight(1f)) {
                                    soundEffectsManager.playCardDeal()
                                }
                                SoundTestChip("🂠 Play Slap", Modifier.weight(1f)) {
                                    soundEffectsManager.playCardPlay()
                                }
                                SoundTestChip("🏆 Trick Win", Modifier.weight(1f)) {
                                    soundEffectsManager.playTrickWin()
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SoundTestChip("🎺 Victory", Modifier.weight(1f)) {
                                    soundEffectsManager.playRoundWin()
                                }
                                SoundTestChip("👑 Trump", Modifier.weight(1f)) {
                                    soundEffectsManager.playTrumpAnnounce()
                                }
                                SoundTestChip("⚠️ Hook Alert", Modifier.weight(1f)) {
                                    soundEffectsManager.playDealerHookAlert()
                                }
                            }
                        }
                    }
                }

                // ====================================================
                // 2. ACCOUNT & CLOUD SYNCHRONIZATION
                // ====================================================
                SettingsSectionCard(title = "ACCOUNT & CLOUD DATA", icon = Icons.Default.AccountCircle) {
                    val avatarEmoji = CustomizationData.avatars.find { it.id == userProfile.selectedAvatar }?.emoji ?: "🦁"
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurfaceElevated)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatarEmoji, fontSize = 20.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userProfile.googleName,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (userProfile.isLoggedIn) {
                                    if (userProfile.googleEmail.isNotBlank()) userProfile.googleEmail else "Authenticated"
                                } else {
                                    "Guest / Local Profile"
                                },
                                color = if (userProfile.isLoggedIn) Color(0xFF34D399) else TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        if (!userProfile.isLoggedIn) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onOpenAuth()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Sign In", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Account Actions
                    if (userProfile.isLoggedIn) {
                        Button(
                            onClick = { showLogoutConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE5E7EB).copy(alpha = 0.2f))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = TextLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out from Account", color = TextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Delete Account Button (Destructive)
                    OutlinedButton(
                        onClick = { showDeleteConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF7F1D1D).copy(alpha = 0.15f),
                            contentColor = Color(0xFFF87171)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF4444).copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteForever,
                            contentDescription = null,
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Account & Wipe Data",
                            color = Color(0xFFF87171),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // ====================================================
                // 3. GAMEPLAY & CARD PREFERENCES
                // ====================================================
                SettingsSectionCard(title = "GAMEPLAY PREFERENCES", icon = Icons.Default.Style) {
                    SettingsToggleRow(
                        title = "Auto-Sort Hand",
                        subtitle = "Automatically sort dealt cards by suit (♠ ♦ ♣ ♥) and descending rank",
                        checked = appSettings.autoSortHand,
                        onCheckedChange = { settingsManager.setAutoSortHand(it) }
                    )

                    SettingsToggleRow(
                        title = "Dealer Hook Rule Warning",
                        subtitle = "Show warning notification if dealer selects forbidden hook bid",
                        checked = appSettings.dealerHookWarning,
                        onCheckedChange = { settingsManager.setDealerHookWarning(it) }
                    )

                    SettingsToggleRow(
                        title = "Fast Bot Turns",
                        subtitle = "Reduce AI thinking delay for ultra-fast single-player matches",
                        checked = appSettings.fastBotTurns,
                        onCheckedChange = { settingsManager.setFastBotTurns(it) }
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "GAMEPLAY VIEW MODE",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { settingsManager.set3DMode(false) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_2d_mode"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!appSettings.is3DMode) GoldPrimary else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "2D Classic",
                                color = if (!appSettings.is3DMode) Color.Black else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                        Button(
                            onClick = { settingsManager.set3DMode(true) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_3d_mode"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (appSettings.is3DMode) GoldPrimary else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "3D Avatar Mode",
                                color = if (appSettings.is3DMode) Color.Black else TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenCustomization()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurfaceElevated),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.5f))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("🎨 Themes & Avatars", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                onOpenRules()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurfaceElevated),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(EmeraldLight.copy(alpha = 0.5f))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📖 Rules Guide", color = EmeraldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ====================================================
                // 4. ABOUT APP
                // ====================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Kaachu Phool • Version 1.0.0",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Traditional Indian Trick-Taking Card Game (Judgement)",
                        color = TextMuted.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
                }
            }
        }
    }

    // Log Out Confirmation Dialog
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = DarkSurfaceElevated,
            title = { Text("Log Out?", color = GoldLight, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "You will be signed out. Your local offline matches will remain saved.",
                    color = TextLight,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        userProfileManager.logout()
                        showLogoutConfirmDialog = false
                        statusNotification = "Successfully logged out."
                        onLoggedOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444))
                    Text("Delete Account & Wipe Data?", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "This action is permanent and cannot be undone.\n\n• Your Firebase user credentials will be deleted.\n• All local preferences, statistics, avatars, and scorecard history will be wiped.",
                    color = TextLight,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            userProfileManager.deleteAccountAndWipeData()
                            showDeleteConfirmDialog = false
                            statusNotification = "Account deleted and all data wiped."
                            onAccountDeleted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Permanently Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.7f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.25f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    color = GoldLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                color = TextLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = GoldPrimary,
                checkedTrackColor = GoldPrimary.copy(alpha = 0.4f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurface
            ),
            modifier = if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier
        )
    }
}

@Composable
private fun SoundTestChip(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(GoldPrimary.copy(alpha = 0.15f))
            .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = GoldLight,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
