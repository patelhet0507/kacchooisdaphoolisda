package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import com.example.model.CustomizationData
import com.example.ui.theme.*

@Composable
fun ProfileCustomizationDialog(
    gamesPlayed: Int,
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Profile & Avatars, 1: Table Themes

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        titleContentColor = GoldLight,
        textContentColor = TextLight,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Player Profile & Customization",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total Games Played: $gamesPlayed",
                    fontSize = 12.sp,
                    color = GoldLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

                // Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) GoldPrimary else DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Avatars",
                            color = if (selectedTab == 0) Color.Black else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) GoldPrimary else DarkSurfaceElevated
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Table Themes",
                            color = if (selectedTab == 1) Color.Black else TextLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
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
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                } else {
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
                            .weight(1f),
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
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}
