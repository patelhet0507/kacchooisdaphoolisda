package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BotDifficulty
import com.example.model.GameMode
import com.example.model.ScoringRule
import com.example.model.Suit
import com.example.viewmodel.MultiplayerViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldFelt
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.data.UserProfileManager
import com.example.model.CustomizationData
import com.example.ui.components.GoogleLoginDialog
import com.example.ui.components.ProfileCustomizationDialog
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(
    onStartGame: (userName: String, mode: GameMode, scoringRule: ScoringRule, difficulty: BotDifficulty) -> Unit,
    onOpenMultiplayerLobby: () -> Unit,
    onOpenScorecard: () -> Unit,
    onOpenRules: () -> Unit,
    multiplayerViewModel: MultiplayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val userProfileManager = remember { UserProfileManager(context) }
    val userState by userProfileManager.state.collectAsStateWithLifecycle()

    var showQuickStartDialog by remember { mutableStateOf(false) }
    var showMultiplayerDialog by remember { mutableStateOf(false) }
    var showGoogleLoginDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    val roomCode by multiplayerViewModel.roomCode.collectAsStateWithLifecycle()
    val isLoading by multiplayerViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by multiplayerViewModel.errorMessage.collectAsStateWithLifecycle()

    LaunchedEffect(roomCode) {
        if (roomCode != null) {
            Log.d("Multiplayer", "Room active: $roomCode, opening lobby")
            showMultiplayerDialog = false
            onOpenMultiplayerLobby()
        }
    }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Banner with Image
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_table),
                        contentDescription = "Kaachu Phool Table",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        DarkBackground.copy(alpha = 0.4f),
                                        DarkBackground
                                    )
                                )
                            )
                    )

                    // Title in hero
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        Text(
                            text = "KAACHU PHOOL",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Traditional Indian Trick-Taking Game",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Profile & Customization Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { showProfileDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.5f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val currentAvatar = CustomizationData.avatars.find { it.id == userState.selectedAvatar }?.emoji ?: "🦁"
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentAvatar, fontSize = 24.sp)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userState.googleName,
                                color = TextLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Games Played: ${userState.gamesPlayed} • Tap for Avatars & Themes",
                                color = GoldLight,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { showGoogleLoginDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userState.isLoggedIn) Color(0xFF10B981).copy(alpha = 0.2f) else GoldPrimary
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (userState.isLoggedIn) "Google Synced" else "Sign In",
                                color = if (userState.isLoggedIn) Color(0xFF10B981) else Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // The Kaachu Phool Mnemonic Bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "THE TRUMP CYCLE (K-A-C-H-U-F-U-L)",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MnemonicPill(code = "Ka", name = "Kali", symbol = "♠", isRed = false, modifier = Modifier.weight(1f))
                        MnemonicPill(code = "Chu", name = "Chokat", symbol = "♦", isRed = true, modifier = Modifier.weight(1f))
                        MnemonicPill(code = "Fu", name = "Fuli", symbol = "♣", isRed = false, modifier = Modifier.weight(1f))
                        MnemonicPill(code = "L", name = "Laal", symbol = "♥", isRed = true, modifier = Modifier.weight(1f))
                    }
                }
            }

            // Primary Feature Cards
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play vs AI
                    FeatureMenuCard(
                        title = "Play Match vs AI Bots",
                        subtitle = "Single-player 4-player game with intelligent bots, Hook rule enforcement, and live table gameplay.",
                        badge = "POPULAR",
                        icon = Icons.Default.PlayArrow,
                        iconTint = GoldPrimary,
                        onClick = { showQuickStartDialog = true },
                        testTag = "menu_play_vs_ai"
                    )

                    // Multiplayer (New)
                    FeatureMenuCard(
                        title = "Play Multiplayer with Code",
                        subtitle = "Create or join a game room using a code to play with up to 6 players.",
                        badge = "NEW",
                        icon = Icons.Default.TableChart,
                        iconTint = EmeraldLight,
                        onClick = { showMultiplayerDialog = true },
                        testTag = "menu_multiplayer"
                    )

                    // Companion Scorekeeper
                    FeatureMenuCard(
                        title = "Real Cards Scorekeeper",
                        subtitle = "Playing with physical cards and friends? Enter bids, enforce dealer Hook rule, and calculate totals automatically.",
                        badge = "COMPANION",
                        icon = Icons.Default.TableChart,
                        iconTint = EmeraldLight,
                        onClick = onOpenScorecard,
                        testTag = "menu_scorecard"
                    )

                    // Rules & Strategy Guide
                    FeatureMenuCard(
                        title = "Rules & Kaachu Phool Guide",
                        subtitle = "Learn the mnemonic trump cycle, the Dealer Hook rule, bidding strategies, and trick taking tips.",
                        badge = "GUIDE",
                        icon = Icons.Default.Book,
                        iconTint = GoldLight,
                        onClick = onOpenRules,
                        testTag = "menu_rules"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showQuickStartDialog) {
        QuickStartGameDialog(
            defaultName = userState.googleName,
            onDismiss = { showQuickStartDialog = false },
            onConfirm = { name, mode, rule, difficulty ->
                showQuickStartDialog = false
                userProfileManager.incrementGamesPlayed()
                onStartGame(name, mode, rule, difficulty)
            }
        )
    }
    if (showMultiplayerDialog) {
        MultiplayerDialog(
            defaultName = userState.googleName,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onDismiss = {
                multiplayerViewModel.clearError()
                showMultiplayerDialog = false
            },
            onCreateRoom = { playerName ->
                Log.d("Multiplayer", "Creating room for $playerName")
                userProfileManager.incrementGamesPlayed()
                multiplayerViewModel.createRoom(playerName)
            },
            onJoinRoom = { code, playerName ->
                Log.d("Multiplayer", "Joining room $code for $playerName")
                userProfileManager.incrementGamesPlayed()
                multiplayerViewModel.joinRoom(code, playerName)
            }
        )
    }

    if (showGoogleLoginDialog) {
        GoogleLoginDialog(
            currentName = userState.googleName,
            currentEmail = userState.googleEmail,
            isLoggedIn = userState.isLoggedIn,
            onDismiss = { showGoogleLoginDialog = false },
            onLogin = { name, email ->
                userProfileManager.loginWithGoogle(email, name)
            },
            onLogout = {
                userProfileManager.logout()
            }
        )
    }

    if (showProfileDialog) {
        ProfileCustomizationDialog(
            gamesPlayed = userState.gamesPlayed,
            selectedAvatar = userState.selectedAvatar,
            selectedTheme = userState.selectedTableTheme,
            isLoggedIn = userState.isLoggedIn,
            googleName = userState.googleName,
            googleEmail = userState.googleEmail,
            onSelectAvatar = { avatarId ->
                userProfileManager.setSelectedAvatar(avatarId)
            },
            onSelectTheme = { themeId ->
                userProfileManager.setSelectedTableTheme(themeId)
            },
            onOpenGoogleLogin = {
                showProfileDialog = false
                showGoogleLoginDialog = true
            },
            onDismiss = { showProfileDialog = false }
        )
    }
}

@Composable
private fun MnemonicPill(
    code: String,
    name: String,
    symbol: String,
    isRed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, EmeraldBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = symbol,
                fontSize = 20.sp,
                color = if (isRed) Color(0xFFEF4444) else TextLight
            )
            Text(
                text = code,
                color = GoldPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = name,
                color = TextLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FeatureMenuCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(EmeraldBorder.copy(alpha = 0.6f))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        color = GoldLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(EmeraldBorder)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            color = TextLight,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun QuickStartGameDialog(
    defaultName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, mode: GameMode, rule: ScoringRule, difficulty: BotDifficulty) -> Unit
) {
    var userName by remember { mutableStateOf(defaultName.ifBlank { "Player 1" }) }
    var selectedMode by remember { mutableStateOf(GameMode.QUICK) }
    var selectedRule by remember { mutableStateOf(ScoringRule.STANDARD) }
    var selectedDifficulty by remember { mutableStateOf(BotDifficulty.MEDIUM) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Start Match vs AI Bots",
                color = GoldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Your Name", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = EmeraldBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Game Duration:",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        GameMode.QUICK,
                        GameMode.CLASSIC,
                        GameMode.FULL,
                        GameMode.ASCENDING_8,
                        GameMode.ASCENDING_5
                    ).forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedMode == mode) GoldPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated)
                                .border(1.dp, if (selectedMode == mode) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { selectedMode = mode }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mode.title,
                                    color = if (selectedMode == mode) GoldLight else TextLight,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = mode.description,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Scoring Rule:",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScoringRule.values().forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedRule == rule) GoldPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated)
                                .border(1.dp, if (selectedRule == rule) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { selectedRule = rule }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = rule.title,
                                    color = if (selectedRule == rule) GoldLight else TextLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = rule.description,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "AI Bot Difficulty:",
                    color = GoldLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BotDifficulty.values().forEach { diff ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedDifficulty == diff) GoldPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated)
                                .border(1.dp, if (selectedDifficulty == diff) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { selectedDifficulty = diff }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = diff.title,
                                    color = if (selectedDifficulty == diff) GoldLight else TextLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = diff.description,
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Opponents: 3 AI Bots (Aarav, Priya, Rohan)",
                    color = TextMuted,
                    fontSize = 11.sp
                )

            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(userName.ifBlank { "You" }, selectedMode, selectedRule, selectedDifficulty)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_start_game_button")
            ) {
                Text("Deal Cards & Play", color = EmeraldDeep, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
private fun MultiplayerDialog(
    defaultName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreateRoom: (playerName: String) -> Unit,
    onJoinRoom: (code: String, playerName: String) -> Unit
) {
    var playerName by remember { mutableStateOf(defaultName.ifBlank { "Player 1" }) }
    var roomCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Multiplayer (Play by Code)",
                color = GoldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = playerName,
                    onValueChange = { playerName = it },
                    label = { Text("Your Display Name", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("player_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = EmeraldBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF7F1D1D).copy(alpha = 0.5f))
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFFCA5A5),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Create Room Button
                Button(
                    onClick = {
                        onCreateRoom(playerName.ifBlank { "Host" })
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("create_room_button"),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldLight),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating Room...", color = Color.White, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Create New Room (Host)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(EmeraldBorder.copy(alpha = 0.5f)))
                    Text(
                        text = " OR JOIN WITH CODE ",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(EmeraldBorder.copy(alpha = 0.5f)))
                }

                OutlinedTextField(
                    value = roomCode,
                    onValueChange = { input -> 
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 6) roomCode = filtered 
                    },
                    label = { Text("6-Digit Room Code", color = TextMuted) },
                    placeholder = { Text("e.g. 481920", color = TextMuted) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("room_code_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = EmeraldBorder,
                        focusedTextColor = TextLight,
                        unfocusedTextColor = TextLight
                    )
                )

                Button(
                    onClick = {
                        if (roomCode.length == 6) {
                            onJoinRoom(roomCode, playerName.ifBlank { "Player" })
                        }
                    },
                    enabled = roomCode.length == 6 && !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("join_room_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = EmeraldDeep,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Joining Room...", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Join Room", color = EmeraldDeep, fontWeight = FontWeight.Black)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
