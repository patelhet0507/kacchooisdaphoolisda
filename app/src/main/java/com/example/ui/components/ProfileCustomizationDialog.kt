package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Achievement
import com.example.model.CustomizationData
import com.example.ui.theme.*

@Composable
fun ProfileCustomizationDialog(
    gamesPlayed: Int,
    winsCount: Int = 0,
    highestScore: Int = 0,
    achievements: List<Achievement> = emptyList(),
    selectedAvatar: String,
    selectedTheme: String,
    isLoggedIn: Boolean,
    googleName: String,
    googleEmail: String,
    onSelectAvatar: (String) -> Unit,
    onSelectTheme: (String) -> Unit,
    onOpenGoogleLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Avatars, 1: Table Themes, 2: Achievements

    val unlockedCount = achievements.count { it.isUnlocked }
    val winRate = if (gamesPlayed > 0) ((winsCount.toFloat() / gamesPlayed) * 100).toInt() else 0

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val maxDialogHeight = (configuration.screenHeightDp * 0.88f).dp
    val dialogWidthFraction = if (configuration.screenWidthDp > 600) 0.65f else 0.92f

    Dialog(
        onDismissRequest = onDismiss,
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
                // Header with Title & Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Profile & Career 🏆",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Text(
                            text = "Badges Unlocked: $unlockedCount / ${achievements.size}",
                            fontSize = 11.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
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

                // Scrollable Body Column
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Google Account Status Banner
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenGoogleLogin() },
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(12.dp),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(if (isLoggedIn) Color(0xFF10B981).copy(alpha = 0.5f) else GoldPrimary.copy(alpha = 0.4f))
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    color = Color(0xFF4285F4),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isLoggedIn) googleName else "Sign in with Google",
                                    color = TextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isLoggedIn) googleEmail else "Tap to connect account & sync stats",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = if (isLoggedIn) "Connected" else "Sign In",
                                color = if (isLoggedIn) Color(0xFF10B981) else GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                // Career Statistics Summary Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.3f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$gamesPlayed", color = TextLight, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text(text = "Played", color = TextMuted, fontSize = 9.sp)
                        }
                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = TextMuted.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$winsCount", color = GoldLight, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text(text = "Wins", color = TextMuted, fontSize = 9.sp)
                        }
                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = TextMuted.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$winRate%", color = EmeraldLight, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text(text = "Win Rate", color = TextMuted, fontSize = 9.sp)
                        }
                        Divider(modifier = Modifier.height(24.dp).width(1.dp), color = TextMuted.copy(alpha = 0.3f))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "$highestScore", color = GoldPrimary, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Text(text = "Best Pts", color = TextMuted, fontSize = 9.sp)
                        }
                    }
                }

                // Tabs (Avatars, Themes, Achievements)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) GoldPrimary else DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = "Avatars",
                            color = if (selectedTab == 0) Color.Black else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) GoldPrimary else DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = "Themes",
                            color = if (selectedTab == 1) Color.Black else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 2) GoldPrimary else DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        Text(
                            text = "Badges 🏆",
                            color = if (selectedTab == 2) Color.Black else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                if (selectedTab == 0) {
                    // Avatars Grid
                    Text(
                        text = "Choose Avatar (Play games to unlock)",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(CustomizationData.avatars) { avatar ->
                            val isUnlocked = gamesPlayed >= avatar.requiredGames
                            val isSelected = selectedAvatar == avatar.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isUnlocked) {
                                        onSelectAvatar(avatar.id)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) GoldPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        when {
                                            isSelected -> GoldPrimary
                                            isUnlocked -> EmeraldBorder.copy(alpha = 0.5f)
                                            else -> Color.Gray.copy(alpha = 0.2f)
                                        }
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(contentAlignment = Alignment.TopEnd) {
                                        Text(
                                            text = avatar.emoji,
                                            fontSize = 28.sp
                                        )
                                        if (!isUnlocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = avatar.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUnlocked) TextLight else TextMuted
                                    )
                                    Text(
                                        text = if (isUnlocked) (if (isSelected) "Active" else "Unlocked") else "Need ${avatar.requiredGames} games",
                                        fontSize = 9.sp,
                                        color = if (isSelected) GoldLight else TextMuted
                                    )
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    // Table Themes Grid
                    Text(
                        text = "Choose Table Felt Theme (Play games to unlock)",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CustomizationData.themes) { theme ->
                            val isUnlocked = gamesPlayed >= theme.requiredGames
                            val isSelected = selectedTheme == theme.id

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isUnlocked) {
                                        onSelectTheme(theme.id)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(theme.surfaceColorHex)
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) GoldPrimary else Color(theme.primaryColorHex)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = theme.name,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextLight
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else if (!isUnlocked) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked",
                                                tint = Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (isUnlocked) theme.description else "Unlocks at ${theme.requiredGames} games",
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Achievements Tab
                    Text(
                        text = "Career Milestones & Badges ($unlockedCount / ${achievements.size} Unlocked)",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(achievements) { achievement ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (achievement.isUnlocked) GoldPrimary.copy(alpha = 0.12f) else DarkSurfaceElevated
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (achievement.isUnlocked) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f)
                                    )
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (achievement.isUnlocked) GoldPrimary.copy(alpha = 0.25f) else Color.DarkGray.copy(alpha = 0.3f)
                                            )
                                            .border(
                                                1.dp,
                                                if (achievement.isUnlocked) GoldPrimary else Color.Gray.copy(alpha = 0.3f),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = achievement.emoji,
                                            fontSize = 20.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = achievement.title,
                                                color = if (achievement.isUnlocked) GoldLight else TextLight,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(
                                                        if (achievement.isUnlocked) GoldPrimary.copy(alpha = 0.2f) else DarkSurface
                                                    )
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = achievement.category,
                                                    color = if (achievement.isUnlocked) GoldPrimary else TextMuted,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = achievement.description,
                                            color = TextMuted,
                                            fontSize = 10.sp
                                        )

                                        if (!achievement.isUnlocked && achievement.maxProgress > 1) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { achievement.progress.toFloat() / achievement.maxProgress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = GoldPrimary,
                                                trackColor = DarkSurface
                                            )
                                        }
                                    }

                                    if (achievement.isUnlocked) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(GoldPrimary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "UNLOCKED",
                                                color = Color.Black,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Locked",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                } // close scrollable Column
            }
        }
    }
}
