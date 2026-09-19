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
import androidx.compose.ui.graphics.graphicsLayer
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
                    Column {
                        Text(
                            text = "CAREER & PROFILE",
                            style = MaterialTheme.typography.titleLarge,
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "BADGES: $unlockedCount / ${achievements.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = TextMuted)
                    }
                }

                // Stats Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(label = "PLAYED", value = "$gamesPlayed")
                    StatItem(label = "WINS", value = "$winsCount", valueColor = GoldLight)
                    StatItem(label = "WIN RATE", value = "$winRate%", valueColor = SuccessGreen)
                }

                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabItem(text = "AVATARS", isSelected = selectedTab == 0, modifier = Modifier.weight(1f)) { selectedTab = 0 }
                    TabItem(text = "THEMES", isSelected = selectedTab == 1, modifier = Modifier.weight(1f)) { selectedTab = 1 }
                    TabItem(text = "BADGES", isSelected = selectedTab == 2, modifier = Modifier.weight(1f)) { selectedTab = 2 }
                }

                // Content
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        0 -> AvatarGrid(gamesPlayed, selectedAvatar, onSelectAvatar)
                        1 -> ThemeGrid(gamesPlayed, selectedTheme, onSelectTheme)
                        2 -> AchievementList(achievements)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, valueColor: Color = TextLight) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = valueColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
        Text(label, color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TabItem(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) GoldPrimary else Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.Black else TextLight,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun AvatarGrid(gamesPlayed: Int, selectedAvatar: String, onSelectAvatar: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(CustomizationData.avatars) { avatar ->
            val isUnlocked = gamesPlayed >= avatar.requiredGames
            val isSelected = selectedAvatar == avatar.id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.03f))
                    .border(1.dp, if (isSelected) GoldPrimary else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable(enabled = isUnlocked) { onSelectAvatar(avatar.id) }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Text(avatar.emoji, fontSize = 32.sp, modifier = Modifier.graphicsLayer { alpha = if (isUnlocked) 1f else 0.4f })
                        if (!isUnlocked) Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(16.dp).align(Alignment.Center))
                    }
                    Text(avatar.name, fontSize = 9.sp, color = if (isUnlocked) TextLight else TextMuted, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ThemeGrid(gamesPlayed: Int, selectedTheme: String, onSelectTheme: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(CustomizationData.themes) { theme ->
            val isUnlocked = gamesPlayed >= theme.requiredGames
            val isSelected = selectedTheme == theme.id
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(theme.surfaceColorHex))
                    .border(2.dp, if (isSelected) GoldPrimary else Color.Transparent, RoundedCornerShape(12.dp))
                    .clickable(enabled = isUnlocked) { onSelectTheme(theme.id) }
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(theme.name, color = TextLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        if (!isUnlocked) Icon(Icons.Default.Lock, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    }
                    Text(if (isUnlocked) theme.description else "Unlocks at ${theme.requiredGames} games", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
private fun AchievementList(achievements: List<Achievement>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(achievements) { achievement ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (achievement.isUnlocked) GoldPrimary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.03f))
                    .border(1.dp, if (achievement.isUnlocked) GoldPrimary.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(if (achievement.isUnlocked) GoldPrimary.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(achievement.emoji, fontSize = 24.sp, modifier = Modifier.graphicsLayer { alpha = if (achievement.isUnlocked) 1f else 0.4f })
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(achievement.title, color = if (achievement.isUnlocked) GoldLight else TextLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(achievement.description, color = TextMuted, fontSize = 10.sp)
                    }
                    if (achievement.isUnlocked) {
                        Text("UNLOCKED", color = GoldPrimary, fontSize = 8.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}
