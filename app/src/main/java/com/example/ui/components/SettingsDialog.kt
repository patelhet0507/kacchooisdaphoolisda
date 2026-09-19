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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.example.update.AppUpdateManager
import com.example.update.DownloadStatus
import com.example.update.GithubReleaseInfo
import com.example.update.UpdateCheckState
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
    val appUpdateManager = remember { AppUpdateManager.getInstance(context) }

    val appSettings by settingsManager.settings.collectAsStateWithLifecycle()
    val userProfile by userProfileManager.state.collectAsStateWithLifecycle()
    val updateState by appUpdateManager.updateState.collectAsStateWithLifecycle()
    val downloadStatus by appUpdateManager.downloadStatus.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var showUpdateModal by remember { mutableStateOf<GithubReleaseInfo?>(null) }
    var statusNotification by remember { mutableStateOf<String?>(null) }
    var showRepoEditDialog by remember { mutableStateOf(false) }
    var tempRepoText by remember { mutableStateOf(appSettings.githubRepo) }

    val configuration = LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.90f).dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleLarge,
                        color = GoldLight,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Audio Section
                    SettingsSectionCard(title = "AUDIO & HAPTICS", icon = Icons.Default.VolumeUp) {
                        SettingsToggleRow(
                            title = "Sound Effects",
                            subtitle = "Card deal, slap, flip, and fanfare",
                            checked = appSettings.soundEffectsEnabled,
                            onCheckedChange = { settingsManager.setSoundEffectsEnabled(it) }
                        )
                        SettingsToggleRow(
                            title = "Haptic Feedback",
                            subtitle = "Vibrations on card actions",
                            checked = appSettings.hapticFeedbackEnabled,
                            onCheckedChange = { settingsManager.setHapticFeedbackEnabled(it) }
                        )
                    }

                    // Account Section
                    SettingsSectionCard(title = "ACCOUNT", icon = Icons.Default.AccountCircle) {
                        val avatarEmoji = CustomizationData.avatars.find { it.id == userProfile.selectedAvatar }?.emoji ?: "🦁"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(GoldPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(avatarEmoji, fontSize = 20.sp)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(userProfile.googleName, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(if (userProfile.isLoggedIn) "Authenticated" else "Guest", color = if (userProfile.isLoggedIn) SuccessGreen else TextMuted, fontSize = 11.sp)
                            }
                            if (!userProfile.isLoggedIn) {
                                PremiumButton(text = "SIGN IN", onClick = onOpenAuth)
                            }
                        }
                    }

                    // Preferences
                    SettingsSectionCard(title = "GAMEPLAY", icon = Icons.Default.Style) {
                        SettingsToggleRow(
                            title = "Auto-Sort Hand",
                            subtitle = "Sort by suit and rank automatically",
                            checked = appSettings.autoSortHand,
                            onCheckedChange = { settingsManager.setAutoSortHand(it) }
                        )
                        SettingsToggleRow(
                            title = "3D Avatar Mode",
                            subtitle = "Enable 3D table and animations",
                            checked = appSettings.is3DMode,
                            onCheckedChange = { settingsManager.set3DMode(it) }
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

    // Modal when Update is Available
    showUpdateModal?.let { release ->
        UpdateAvailableDialog(
            release = release,
            appUpdateManager = appUpdateManager,
            onDismiss = { showUpdateModal = null }
        )
    }

    // Repository Config Dialog
    if (showRepoEditDialog) {
        AlertDialog(
            onDismissRequest = { showRepoEditDialog = false },
            containerColor = DarkSurfaceElevated,
            title = {
                Text("GitHub Repository", color = GoldLight, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Set the GitHub owner/repo to check for APK releases (format: owner/repo):",
                        color = TextLight,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = tempRepoText,
                        onValueChange = { tempRepoText = it },
                        singleLine = true,
                        placeholder = { Text("e.g. patelhet0507/kacchooisdaphoolisda", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = GoldPrimary.copy(alpha = 0.4f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tempRepoText.isNotBlank()) {
                            settingsManager.setGithubRepo(tempRepoText)
                        }
                        showRepoEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepoEditDialog = false }) {
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
