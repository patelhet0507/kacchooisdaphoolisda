package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.update.AppUpdateManager
import com.example.update.DownloadStatus
import com.example.update.GithubReleaseInfo

@Composable
fun UpdateAvailableDialog(
    release: GithubReleaseInfo,
    appUpdateManager: AppUpdateManager,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val downloadStatus by appUpdateManager.downloadStatus.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val dialogWidthFraction = if (isLandscape) 0.62f else 0.92f
    val maxDialogHeight = (configuration.screenHeightDp * 0.88f).dp

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(dialogWidthFraction)
                .heightIn(max = maxDialogHeight)
                .padding(12.dp)
                .testTag("update_available_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header
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
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Update Available! 🚀",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                            Text(
                                text = "New release on GitHub",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                // Version Tag & Size Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldDeep)
                            .border(1.dp, EmeraldLight.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "New: ${release.tagName}",
                            color = EmeraldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Current: v${appUpdateManager.currentVersionName}",
                            color = TextLight,
                            fontSize = 11.sp
                        )
                    }

                    if (release.apkSizeBytes > 0) {
                        val mb = release.apkSizeBytes / (1024 * 1024.0)
                        Text(
                            text = "%.1f MB".format(mb),
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Release Notes Content
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.7f)),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "What's New in ${release.releaseTitle.ifBlank { release.tagName }}:",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = release.releaseNotes.ifBlank { "Performance updates and bug fixes." },
                            color = TextLight,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                // Download Progress / Status Indicator
                when (val status = downloadStatus) {
                    is DownloadStatus.Downloading -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val speedText = if (status.speedKbps > 1024) {
                                    "%.2f MB/s".format(status.speedKbps / 1024.0)
                                } else {
                                    "%.1f KB/s".format(status.speedKbps)
                                }
                                Text("Downloading: ${status.progressPercent}% ($speedText)", color = GoldLight, fontSize = 11.sp)
                                Text("Please wait...", color = TextMuted, fontSize = 11.sp)
                            }
                            LinearProgressIndicator(
                                progress = { status.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = GoldPrimary,
                                trackColor = DarkSurfaceElevated
                            )
                        }
                    }
                    is DownloadStatus.ReadyToInstall -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF065F46).copy(alpha = 0.4f))
                                .border(1.dp, EmeraldLight, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "✓ APK downloaded successfully!",
                                color = EmeraldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Divider(color = EmeraldLight.copy(alpha = 0.2f), thickness = 0.5.dp)
                            Text(
                                text = "💡 Note: If install fails with 'Package Conflict', uninstall the current app first, then come back here to install.",
                                color = TextLight.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 13.sp
                            )
                        }
                    }
                    is DownloadStatus.Failed -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7F1D1D).copy(alpha = 0.3f))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(20.dp))
                                Text(
                                    text = "Update Problem",
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Text(
                                text = status.reason,
                                color = Color(0xFFFCA5A5).copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )

                            Divider(color = Color(0xFFEF4444).copy(alpha = 0.3f), thickness = 0.5.dp)

                            Text(
                                text = "⚠️ If you see 'Package Conflict':",
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "This happens if the new version has a different security key than your current one (common if you're switching from a Debug to Release version).",
                                color = TextLight,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 15.sp
                            )

                            Text(
                                text = "FIX: Manually UNINSTALL the app from your home screen, then click 'Install Update' again.",
                                color = GoldPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    DownloadStatus.Idle -> Unit
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Browser Fallback Button
                    OutlinedButton(
                        onClick = {
                            appUpdateManager.openReleasesPageInBrowser()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurfaceElevated),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.4f))
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = GoldLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("GitHub", color = GoldLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Main Action Button (Download or Install)
                    when (val status = downloadStatus) {
                        is DownloadStatus.ReadyToInstall -> {
                            Button(
                                onClick = {
                                    val success = appUpdateManager.installApk(status.apkUri, status.file)
                                    if (!success) {
                                        Toast.makeText(context, "Could not open installer. Check Downloads folder.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.weight(1.5f).testTag("install_update_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DownloadDone, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Install Update", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        is DownloadStatus.Downloading -> {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.weight(1.5f),
                                colors = ButtonDefaults.buttonColors(disabledContainerColor = DarkSurfaceElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Downloading...", color = TextMuted, fontSize = 12.sp)
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    if (release.apkDownloadUrl != null) {
                                        appUpdateManager.startDownload(release)
                                    } else {
                                        appUpdateManager.openReleasesPageInBrowser()
                                    }
                                },
                                modifier = Modifier.weight(1.5f).testTag("download_update_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (release.apkDownloadUrl != null) "Download APK" else "Get on GitHub",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
