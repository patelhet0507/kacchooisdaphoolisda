package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import com.example.engine.SoundEffectsManager
import com.example.model.CustomizationData
import com.example.ui.components.GoogleLoginDialog
import com.example.ui.components.ProfileCustomizationDialog
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Palette
import com.example.ui.components.SettingsDialog
import com.example.ui.components.FriendsDialog
import com.example.ui.components.UpdateAvailableDialog
import com.example.update.AppUpdateManager
import com.example.update.GithubReleaseInfo
import com.example.update.UpdateCheckState
import com.example.data.SettingsManager
import androidx.compose.ui.platform.LocalContext

import com.example.viewmodel.MatchHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onStartGame: (userName: String, mode: GameMode, scoringRule: ScoringRule, difficulty: BotDifficulty) -> Unit,
    onOpenMultiplayerLobby: () -> Unit,
    onOpenScorecard: () -> Unit,
    onOpenRules: () -> Unit,
    multiplayerViewModel: MultiplayerViewModel = viewModel()
) {
    val context = LocalContext.current
    val matchHistoryViewModel: MatchHistoryViewModel = viewModel()
    val lastMatches by matchHistoryViewModel.lastFiveMatches.collectAsStateWithLifecycle()
    val userProfileManager = remember { UserProfileManager(context) }
    val soundEffectsManager = remember { SoundEffectsManager.getInstance(context) }
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val appUpdateManager = remember { AppUpdateManager.getInstance(context) }

    val userState by userProfileManager.state.collectAsStateWithLifecycle()
    val appSettings by settingsManager.settings.collectAsStateWithLifecycle()

    var showQuickStartDialog by remember { mutableStateOf(false) }
    var showMultiplayerDialog by remember { mutableStateOf(false) }
    var showGoogleLoginDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showFriendsDialog by remember { mutableStateOf(false) }
    var startupUpdateRelease by remember { mutableStateOf<GithubReleaseInfo?>(null) }

    val roomCode by multiplayerViewModel.roomCode.collectAsStateWithLifecycle()
    val isLoading by multiplayerViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by multiplayerViewModel.errorMessage.collectAsStateWithLifecycle()

    // Automatically check for updates on app startup if enabled in settings
    LaunchedEffect(Unit) {
        if (appSettings.autoCheckUpdates) {
            val result = appUpdateManager.checkForUpdates(appSettings.githubRepo)
            if (result is UpdateCheckState.UpdateAvailable) {
                startupUpdateRelease = result.release
            }
        }
    }

    LaunchedEffect(roomCode) {
        if (roomCode != null) {
            Log.d("Multiplayer", "Room active: $roomCode, opening lobby")
            showMultiplayerDialog = false
            onOpenMultiplayerLobby()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen Background Image
        Image(
            painter = painterResource(id = R.drawable.img_stitch_hero),
            contentDescription = "Kaachu Phool Table Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark Gradient Scrim Overlay for High Contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DarkBackground.copy(alpha = 0.70f),
                            DarkBackground.copy(alpha = 0.85f),
                            DarkBackground.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Action & Title Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "KAACHU PHOOL",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Traditional Indian Trick-Taking Game",
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // User Profile Avatar Quick Button
                            val currentAvatar = CustomizationData.avatars.find { it.id == userState.selectedAvatar }?.emoji ?: "🦁"
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface.copy(alpha = 0.85f))
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.6f), CircleShape)
                                    .clickable { showProfileDialog = true }
                                    .testTag("home_profile_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = currentAvatar, fontSize = 22.sp)
                            }

                            // Settings Button
                            IconButton(
                                onClick = {
                                    soundEffectsManager.playButtonTap()
                                    showSettingsDialog = true
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface.copy(alpha = 0.85f))
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.6f), CircleShape)
                                    .testTag("home_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Game Settings & Audio",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Main Sign-In Banner (REMOVED when logged in)
                if (!userState.isLoggedIn) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clickable { showGoogleLoginDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.90f)),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.6f))
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "🔑", fontSize = 24.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sign In for Cloud Sync",
                                        color = TextLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = "Sync stats, friends, & achievements across devices",
                                        color = GoldLight,
                                        fontSize = 11.sp
                                    )
                                }

                                Button(
                                    onClick = { showGoogleLoginDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Sign In",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
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

                        Spacer(modifier = Modifier.height(10.dp))

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

                // Primary Feature Grid
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PLAY",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActionCard(
                                title = "Play vs AI",
                                icon = "🤖",
                                desc = "Offline practice",
                                onClick = { showQuickStartDialog = true },
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )
                            ActionCard(
                                title = "Multiplayer",
                                icon = "🌐",
                                desc = "Play with friends",
                                onClick = { showMultiplayerDialog = true },
                                modifier = Modifier.weight(1f),
                                isPrimary = true
                            )
                        }

                        Text(
                            text = "COMMUNITY & LEARN",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ActionCard(
                                title = "Friends",
                                icon = "👥",
                                desc = "",
                                onClick = { showFriendsDialog = true },
                                modifier = Modifier.weight(1f)
                            )
                            ActionCard(
                                title = "Scorepad",
                                icon = "📝",
                                desc = "",
                                onClick = onOpenScorecard,
                                modifier = Modifier.weight(1f)
                            )
                            ActionCard(
                                title = "Rules",
                                icon = "📖",
                                desc = "",
                                onClick = onOpenRules,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "RECENT HISTORY",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        MatchHistorySlide(matches = lastMatches)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
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
            winsCount = userState.winsCount,
            highestScore = userState.highestScore,
            achievements = userState.achievements,
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

    if (showSettingsDialog) {
        SettingsDialog(
            userProfileManager = userProfileManager,
            onDismiss = { showSettingsDialog = false },
            onOpenRules = onOpenRules,
            onOpenCustomization = {
                showSettingsDialog = false
                showProfileDialog = true
            },
            onOpenAuth = {
                showSettingsDialog = false
                showGoogleLoginDialog = true
            }
        )
    }

    if (showFriendsDialog) {
        FriendsDialog(
            friends = userState.friends,
            myPlayerName = userState.googleName,
            onAddFriend = { userProfileManager.addFriend(it) },
            onRemoveFriend = { userProfileManager.removeFriend(it) },
            onInviteFriend = { friendName ->
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val inviteText = "Hey $friendName! Join my Kaachu Phool multiplayer game room. Let's play together!"
                val clip = ClipData.newPlainText("Game Invite", inviteText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied game invite for $friendName to clipboard!", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showFriendsDialog = false }
        )
    }

    startupUpdateRelease?.let { release ->
        UpdateAvailableDialog(
            release = release,
            appUpdateManager = appUpdateManager,
            onDismiss = { startupUpdateRelease = null }
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

    val configuration = LocalConfiguration.current
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
                    Text(
                        text = "Start Match vs AI Bots 🎮",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
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

                // Scrollable Content Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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

                    // Game Mode Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Game Mode:",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(GameMode.QUICK, GameMode.CLASSIC, GameMode.FULL).forEach { mode ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedMode == mode) GoldPrimary else DarkSurfaceElevated)
                                        .border(1.dp, if (selectedMode == mode) GoldLight else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { selectedMode = mode }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = mode.title.substringBefore(" "),
                                            color = if (selectedMode == mode) Color.Black else TextLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when (mode) {
                                                GameMode.QUICK -> "8 Rounds"
                                                GameMode.CLASSIC -> "13 Rounds"
                                                GameMode.FULL -> "17 Rounds"
                                                else -> ""
                                            },
                                            color = if (selectedMode == mode) Color.Black.copy(alpha = 0.8f) else TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(GameMode.ASCENDING_8, GameMode.ASCENDING_5).forEach { mode ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedMode == mode) GoldPrimary else DarkSurfaceElevated)
                                        .border(1.dp, if (selectedMode == mode) GoldLight else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { selectedMode = mode }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = mode.title,
                                            color = if (selectedMode == mode) Color.Black else TextLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Scoring Rule Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Scoring Rule:",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ScoringRule.values().forEach { rule ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedRule == rule) GoldPrimary else DarkSurfaceElevated)
                                        .border(1.dp, if (selectedRule == rule) GoldLight else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { selectedRule = rule }
                                        .padding(vertical = 6.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = rule.title.substringBefore(" "),
                                            color = if (selectedRule == rule) Color.Black else TextLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = when(rule) {
                                                ScoringRule.STANDARD -> "10+Bid"
                                                ScoringRule.PENALTY -> "-10 Diff"
                                                ScoringRule.BONUS -> "Strict"
                                            },
                                            color = if (selectedRule == rule) Color.Black.copy(alpha = 0.8f) else TextMuted,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // AI Bot Difficulty Chips
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "AI Difficulty:",
                            color = GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BotDifficulty.values().forEach { diff ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedDifficulty == diff) GoldPrimary else DarkSurfaceElevated)
                                        .border(1.dp, if (selectedDifficulty == diff) GoldLight else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { selectedDifficulty = diff }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = diff.title,
                                        color = if (selectedDifficulty == diff) Color.Black else TextLight,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

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
                    Text("Deal Cards & Play", color = EmeraldDeep, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun MultiplayerDialog(
    defaultName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreateRoom: (playerName: String) -> Unit,
    onJoinRoom: (code: String, playerName: String) -> Unit
) {
    var playerName by remember { mutableStateOf(defaultName.ifBlank { "Player 1" }) }
    var roomCode by remember { mutableStateOf("") }

    val configuration = LocalConfiguration.current
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
                    Text(
                        text = "Multiplayer (Play by Code) 🌐",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
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

                // Scrollable Content Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
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

                    // Create Room Button
                    Button(
                        onClick = {
                            onCreateRoom(playerName.ifBlank { "Host" })
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
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
                            Text("Create New Room (Host)", color = Color.Black, fontWeight = FontWeight.Black)
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
                            .height(46.dp)
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
            }
        }
    }
}

@Composable
private fun HorizontalGameModeSlider(
    onPlayVsAi: () -> Unit,
    onMultiplayer: () -> Unit,
    onFriends: () -> Unit,
    onScorecard: () -> Unit,
    onRules: () -> Unit,
    onSettings: () -> Unit,
    matches: List<com.example.data.MatchHistoryEntity>
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { 7 }

    val modeTabs = listOf(
        "🤖 vs AI",
        "🌐 Multiplayer",
        "👥 Friends",
        "🕒 History",
        "🃏 Scorekeeper",
        "📖 Rules",
        "⚙️ Settings"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header & Navigation Hint
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EXPLORE GAME MODES",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "◄ Slide Left / Right ►",
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Horizontal Quick Mode Selector Tabs
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(modeTabs) { index, title ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) GoldPrimary else DarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) GoldLight else EmeraldBorder.copy(alpha = 0.5f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) EmeraldDeep else TextLight,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                    )
                }
            }
        }

        // Horizontal Pager with Previews
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            when (page) {
                0 -> AiPlaySlidePreview(onLaunch = onPlayVsAi)
                1 -> MultiplayerSlidePreview(onLaunch = onMultiplayer)
                2 -> FriendsSlidePreview(onLaunch = onFriends)
                3 -> MatchHistorySlide(matches = matches)
                4 -> ScorekeeperSlidePreview(onLaunch = onScorecard)
                5 -> RulesSlidePreview(onLaunch = onRules)
                6 -> SettingsSlidePreview(onLaunch = onSettings)
            }
        }

        // Navigation Controls & Page Dots Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prev Button
            IconButton(
                onClick = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                },
                enabled = pagerState.currentPage > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Mode",
                    tint = if (pagerState.currentPage > 0) GoldPrimary else TextMuted.copy(alpha = 0.3f)
                )
            }

            // Dot Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(7) { index ->
                    val isActive = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 10.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isActive) GoldPrimary else TextMuted.copy(alpha = 0.4f))
                            .clickable {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            }
                    )
                }
            }

            // Next Button
            IconButton(
                onClick = {
                    if (pagerState.currentPage < 6) {
                        coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                enabled = pagerState.currentPage < 6,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Mode",
                    tint = if (pagerState.currentPage < 6) GoldPrimary else TextMuted.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
private fun ModePreviewCard(
    badge: String,
    badgeColor: Color,
    title: String,
    subtitle: String,
    buttonText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    testTag: String,
    onLaunch: () -> Unit,
    previewContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.4f))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f))
                        .border(1.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = title, color = GoldLight, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .border(1.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(text = badge, color = badgeColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text(text = subtitle, color = TextMuted, fontSize = 11.sp, maxLines = 2)
                }
            }

            previewContent()

            Button(
                onClick = onLaunch,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = EmeraldDeep, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = buttonText, color = EmeraldDeep, fontSize = 13.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun AiPlaySlidePreview(onLaunch: () -> Unit) {
    ModePreviewCard(
        badge = "POPULAR",
        badgeColor = GoldPrimary,
        title = "Play Match vs AI Bots",
        subtitle = "Single-player 4-player game with intelligent AI bots, Hook rule enforcement, and dynamic trump cycles.",
        buttonText = "START AI MATCH",
        icon = Icons.Default.PlayArrow,
        iconTint = GoldPrimary,
        testTag = "menu_play_vs_ai",
        onLaunch = onLaunch
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(EmeraldFelt)
                .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurface.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("🤖 Bot 2 • Bid: 2", color = TextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🤖 Bot 1\nBid: 1", color = TextLight, fontSize = 9.sp, textAlign = TextAlign.Center)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("♠ Kali Trump", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("Dealer Hook Active ⚠️", color = Color(0xFFEF4444), fontSize = 8.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface.copy(alpha = 0.8f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("🤖 Bot 3\nBid: 0", color = TextLight, fontSize = 9.sp, textAlign = TextAlign.Center)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldPrimary)
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("👑 You (Player) • Bid: 1 • Cards: 3", color = EmeraldDeep, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun MultiplayerSlidePreview(onLaunch: () -> Unit) {
    ModePreviewCard(
        badge = "ONLINE MULTIPLAYER",
        badgeColor = EmeraldLight,
        title = "Play Multiplayer with Code",
        subtitle = "Host or join custom online rooms using a 6-digit room code to play live with up to 6 real players.",
        buttonText = "ENTER MULTIPLAYER LOBBY",
        icon = Icons.Default.TableChart,
        iconTint = EmeraldLight,
        testTag = "menu_multiplayer",
        onLaunch = onLaunch
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, EmeraldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(1.dp, GoldPrimary, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("ROOM CODE: #KACHU-9482", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                    Text("2/6 Seats Filled", color = EmeraldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldBorder)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👑 You (Host)", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldBorder)
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🟢 Alex (Ready)", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .border(1.dp, TextMuted.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+ Join Slot", color = TextMuted, fontSize = 9.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("⚡ Real-time Sync", color = GoldLight, fontSize = 9.sp)
                    Text("🔒 Private Code Protected", color = TextMuted, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun FriendsSlidePreview(onLaunch: () -> Unit) {
    ModePreviewCard(
        badge = "SOCIAL & FRIENDS",
        badgeColor = GoldPrimary,
        title = "Friends & Multiplayer Invites",
        subtitle = "Add friends using personal friend codes and send instant game room invites with 1-tap.",
        buttonText = "MANAGE FRIENDS & INVITES",
        icon = Icons.Default.PersonAdd,
        iconTint = GoldPrimary,
        testTag = "menu_friends",
        onLaunch = onLaunch
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("MY FRIEND CODE: KACHU-59102", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(GoldPrimary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("1-Tap Copy", color = EmeraldDeep, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑 Alex (Pro) • Online 🟢", color = TextLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldBorder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Invite to Room 📩", color = GoldLight, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("👑 Sam (Master) • In Room 🎮", color = TextMuted, fontSize = 10.sp)
                        Text("In Match", color = TextMuted, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScorekeeperSlidePreview(onLaunch: () -> Unit) {
    ModePreviewCard(
        badge = "COMPANION TOOL",
        badgeColor = EmeraldLight,
        title = "Real Cards Scorekeeper",
        subtitle = "Playing Kaachu Phool physically with real cards? Use our digital scorecard to log bids, Hook rules, and scores.",
        buttonText = "OPEN COMPANION SCORECARD",
        icon = Icons.Default.TableChart,
        iconTint = EmeraldLight,
        testTag = "menu_scorecard",
        onLaunch = onLaunch
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, EmeraldBorder, RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DIGITAL SCORECARD", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Round 3 of 13", color = TextMuted, fontSize = 9.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurface)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Player", color = TextMuted, fontSize = 8.sp)
                        Text("You", color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("Player 2", color = TextLight, fontSize = 9.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bids", color = TextMuted, fontSize = 8.sp)
                        Text("2", color = GoldPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("1", color = GoldPrimary, fontSize = 9.sp)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hands", color = TextMuted, fontSize = 8.sp)
                        Text("2 ✅", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("0 ❌", color = Color(0xFFEF4444), fontSize = 9.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total", color = TextMuted, fontSize = 8.sp)
                        Text("+12 pts", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("-10 pts", color = Color(0xFFEF4444), fontSize = 9.sp)
                    }
                }

                Text("⚠️ Auto Dealer Hook Check & Point Calculation", color = GoldLight, fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun MatchHistorySlide(matches: List<com.example.data.MatchHistoryEntity>) {
    ModePreviewCard(
        badge = "MATCH HISTORY",
        badgeColor = EmeraldLight,
        title = "Recent Matches & Statistics",
        subtitle = "Review your last 5 match results, including winners, scores, and game modes played.",
        buttonText = "VIEW FULL STATISTICS",
        icon = Icons.Default.TableChart,
        iconTint = EmeraldLight,
        testTag = "menu_history",
        onLaunch = { /* Could navigate to a more detailed history page later */ }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, EmeraldLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            if (matches.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No match history yet.", color = TextMuted, fontSize = 10.sp)
                    Text("Play a game to see it here!", color = GoldLight, fontSize = 9.sp)
                }
            } else {
                val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(matches.size) { index ->
                        val match = matches[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(DarkSurface.copy(alpha = 0.5f))
                                .padding(6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${match.gameMode} • ${dateFormat.format(Date(match.timestamp))}",
                                    color = TextMuted,
                                    fontSize = 8.sp
                                )
                                Text(
                                    text = "Winner: ${match.winnerName}",
                                    color = EmeraldLight,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${match.winnerScore} pts",
                                color = GoldPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RulesSlidePreview(onLaunch: () -> Unit) {
    ModePreviewCard(
        badge = "RULES & GUIDE",
        badgeColor = GoldLight,
        title = "Rules & Kaachu Phool Guide",
        subtitle = "Learn the mnemonic trump suit order (Ka-Chu-Fu-L), Dealer Hook restriction, and trick-taking scoring rules.",
        buttonText = "READ FULL RULES GUIDE",
        icon = Icons.Default.Book,
        iconTint = GoldLight,
        testTag = "menu_rules",
        onLaunch = onLaunch
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, GoldLight.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("MNEMONIC TRUMP CYCLE SEQUENCE", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .border(1.dp, GoldPrimary, RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♠ Ka\nKali", color = TextLight, fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♦ Chu\nChokat", color = Color(0xFFEF4444), fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .border(1.dp, GoldPrimary, RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♣ Fu\nFuli", color = TextLight, fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurface)
                            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("♥ L\nLaal", color = Color(0xFFEF4444), fontSize = 9.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("💡 Dealer Hook Rule: Total Bids ≠ Hand Cards for Dealer", color = GoldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SettingsSlidePreview(onLaunch: () -> Unit) {
    ModePreviewCard(
        badge = "SETTINGS & AUDIO",
        badgeColor = GoldPrimary,
        title = "Game Settings & Customization",
        subtitle = "Customize felt table colors, player avatar icons, sound effects volume, vibration haptics, and account options.",
        buttonText = "OPEN SETTINGS & AUDIO",
        icon = Icons.Default.Settings,
        iconTint = GoldPrimary,
        testTag = "menu_settings",
        onLaunch = onLaunch
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TABLE THEMES & AVATARS", color = GoldLight, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("🔊 Card Sound FX Active", color = EmeraldLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF065F46)).border(1.5.dp, GoldPrimary, CircleShape))
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF7F1D1D)))
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF1E3A8A)))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("🦁", fontSize = 16.sp)
                        Text("🐯", fontSize = 16.sp)
                        Text("👑", fontSize = 16.sp)
                        Text("🦅", fontSize = 16.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🎚️ Sound Volume", color = TextLight, fontSize = 9.sp)
                    Text("• 📳 Haptics", color = TextLight, fontSize = 9.sp)
                    Text("• 👤 Profile Sync", color = TextLight, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: String,
    desc: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (isPrimary) 130.dp else 90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPrimary) DarkSurfaceElevated else DarkSurface.copy(alpha = 0.9f)
        ),
        border = BorderStroke(
            if (isPrimary) 1.5.dp else 1.dp,
            if (isPrimary) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = if (isPrimary) 36.sp else 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isPrimary) GoldPrimary else TextLight,
                fontSize = if (isPrimary) 15.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (isPrimary && desc.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    color = TextMuted,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
