package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.UserProfileManager
import com.example.engine.KaachuPhoolEngine
import com.example.engine.SoundEffectsManager
import com.example.model.Emote
import com.example.model.GamePhase
import com.example.model.Player
import com.example.ui.components.BiddingDialog
import com.example.ui.components.EmotePickerBar
import com.example.ui.components.GameOverDialog
import com.example.ui.components.PlayerSeatView
import com.example.ui.components.PlayingCardView
import com.example.ui.components.RoundSummaryDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.components.TrickTableView
import com.example.ui.components.TrumpIndicator
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showQuitDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val soundEffectsManager = remember { SoundEffectsManager.getInstance(context) }
    val userProfileManager = remember { UserProfileManager(context) }
    val settingsManager = remember { com.example.data.SettingsManager.getInstance(context) }
    val appSettings by settingsManager.settings.collectAsStateWithLifecycle()

    LaunchedEffect(soundEffectsManager) {
        viewModel.setSoundEffectsManager(soundEffectsManager)
    }

    // Intercept hardware / system back gesture
    BackHandler {
        showQuitDialog = true
    }

    val myPlayer = uiState.players.find {
        it.name == uiState.localPlayerName || it.id == uiState.localPlayerName || it.id == "user"
    } ?: uiState.players.firstOrNull()

    val opponents = uiState.players.filter { it.name != myPlayer?.name }

    val userState = uiState.playerStates.find { it.player.name == myPlayer?.name }

    // Dynamic opponent layout mapping
    val topOpponents = when (opponents.size) {
        1 -> listOf(opponents[0])
        2 -> emptyList()
        3 -> listOf(opponents[1])
        4 -> listOf(opponents[1], opponents[2])
        else -> opponents.drop(1).dropLast(1)
    }
    val leftOpponent = when (opponents.size) {
        1 -> null
        2 -> opponents[0]
        else -> opponents.firstOrNull()
    }
    val rightOpponent = when (opponents.size) {
        1 -> null
        2 -> opponents[1]
        else -> opponents.lastOrNull()
    }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isSmallScreen = configuration.screenHeightDp < 600
        val cardWidth = if (isSmallScreen) 48.dp else 68.dp
        val cardHeight = if (isSmallScreen) 70.dp else 98.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (isSmallScreen) 6.dp else 12.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Sleek Top Bar (Single Row: Back + Title | Trump Indicator | Settings + Restart)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSmallScreen) 36.dp else 48.dp)
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Back button + Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { showQuitDialog = true },
                        modifier = Modifier
                            .size(if (isSmallScreen) 32.dp else 40.dp)
                            .testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave Match",
                            tint = GoldPrimary,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (uiState.isMultiplayer) "Play Together" else "Kaachu Phool",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSmallScreen) 13.sp else 16.sp
                        )
                        if (uiState.isMultiplayer && uiState.roomCode != null) {
                            Text(
                                text = "Code: ${uiState.roomCode}",
                                color = EmeraldLight,
                                fontSize = if (isSmallScreen) 9.sp else 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Center: Trump Rotation & Round Info Banner
                TrumpIndicator(
                    currentTrump = uiState.currentTrump,
                    roundNumber = uiState.currentRoundIndex + 1,
                    totalRounds = uiState.rounds.size.coerceAtLeast(1),
                    cardCount = uiState.currentRoundCardCount,
                    modifier = Modifier.wrapContentWidth()
                )

                // Right: Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 2.dp else 6.dp)
                ) {
                    IconButton(
                        onClick = {
                            soundEffectsManager.playButtonTap()
                            showSettingsDialog = true
                        },
                        modifier = Modifier
                            .size(if (isSmallScreen) 32.dp else 40.dp)
                            .testTag("game_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = GoldLight,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 22.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            soundEffectsManager.playButtonTap()
                            viewModel.restartCurrentGame()
                        },
                        modifier = Modifier
                            .size(if (isSmallScreen) 32.dp else 40.dp)
                            .testTag("restart_match_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Match",
                            tint = TextMuted,
                            modifier = Modifier.size(if (isSmallScreen) 18.dp else 22.dp)
                        )
                    }
                }
            }

            // 2. Center Playing Table Arena with Seated Opponents (Takes all remaining vertical space)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Opponent Seat
                Box(
                    modifier = Modifier.width(if (isSmallScreen) 68.dp else 78.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (leftOpponent != null) {
                        val pState = uiState.playerStates.find { it.player.name == leftOpponent.name }
                        val isTurn = (uiState.phase == GamePhase.BIDDING || uiState.phase == GamePhase.PLAYING) &&
                                uiState.players.getOrNull(uiState.currentTurnIndex)?.name == leftOpponent.name
                        val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == leftOpponent.name
                        val activeEmote = uiState.activeEmotes[leftOpponent.name] ?: uiState.activeEmotes[leftOpponent.id]

                        PlayerSeatView(
                            player = leftOpponent,
                            bid = pState?.bid,
                            tricksWon = pState?.tricksWon ?: 0,
                            isDealer = isDealer,
                            isCurrentTurn = isTurn,
                            turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                            totalScore = pState?.totalScore ?: 0,
                            activeEmote = activeEmote,
                            is3DMode = appSettings.is3DMode
                        )
                    }
                }

                // Center Felt Table with Top Opponent(s)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top Opponent(s) Row
                    if (topOpponents.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            topOpponents.forEach { opponent ->
                                val pState = uiState.playerStates.find { it.player.name == opponent.name }
                                val isTurn = (uiState.phase == GamePhase.BIDDING || uiState.phase == GamePhase.PLAYING) &&
                                        uiState.players.getOrNull(uiState.currentTurnIndex)?.name == opponent.name
                                val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == opponent.name
                                val activeEmote = uiState.activeEmotes[opponent.name] ?: uiState.activeEmotes[opponent.id]

                                PlayerSeatView(
                                    player = opponent,
                                    bid = pState?.bid,
                                    tricksWon = pState?.tricksWon ?: 0,
                                    isDealer = isDealer,
                                    isCurrentTurn = isTurn,
                                    turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                                    totalScore = pState?.totalScore ?: 0,
                                    activeEmote = activeEmote,
                                    is3DMode = appSettings.is3DMode
                                )
                            }
                        }
                    }

                    // Felt Trick Arena (Takes the majority of the center area!)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TrickTableView(
                            modifier = Modifier.fillMaxSize(),
                            playedCards = uiState.currentTrick,
                            trumpSuit = uiState.currentTrump,
                            leadSuit = uiState.leadSuit,
                            trickWinner = uiState.lastTrickWinner,
                            isTrickFinished = uiState.phase == GamePhase.TRICK_FINISHED,
                            onNextTrickClick = { viewModel.continueNextTrick() },
                            is3DMode = appSettings.is3DMode
                        )
                    }

                    // Table Status Message Banner
                    if (uiState.statusMessage.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceElevated.copy(alpha = 0.92f))
                                .border(1.dp, EmeraldBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.statusMessage,
                                color = GoldLight,
                                fontSize = if (isSmallScreen) 10.sp else 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Right Opponent Seat
                Box(
                    modifier = Modifier.width(if (isSmallScreen) 68.dp else 78.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rightOpponent != null) {
                        val pState = uiState.playerStates.find { it.player.name == rightOpponent.name }
                        val isTurn = (uiState.phase == GamePhase.BIDDING || uiState.phase == GamePhase.PLAYING) &&
                                uiState.players.getOrNull(uiState.currentTurnIndex)?.name == rightOpponent.name
                        val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == rightOpponent.name
                        val activeEmote = uiState.activeEmotes[rightOpponent.name] ?: uiState.activeEmotes[rightOpponent.id]

                        PlayerSeatView(
                            player = rightOpponent,
                            bid = pState?.bid,
                            tricksWon = pState?.tricksWon ?: 0,
                            isDealer = isDealer,
                            isCurrentTurn = isTurn,
                            turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                            totalScore = pState?.totalScore ?: 0,
                            activeEmote = activeEmote,
                            is3DMode = appSettings.is3DMode
                        )
                    }
                }
            }

            // 3. Bottom Player Controls & Hand (Unified Horizontal Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSmallScreen) 74.dp else 86.dp)
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: User Seat View
                Box(
                    modifier = Modifier.width(if (isSmallScreen) 84.dp else 96.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (myPlayer != null) {
                        val isTurn = (uiState.phase == GamePhase.BIDDING || uiState.phase == GamePhase.PLAYING) &&
                                uiState.players.getOrNull(uiState.currentTurnIndex)?.name == myPlayer.name
                        val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == myPlayer.name
                        val myActiveEmote = uiState.activeEmotes[myPlayer.name] ?: uiState.activeEmotes[myPlayer.id] ?: uiState.activeEmotes["You"]

                        PlayerSeatView(
                            player = myPlayer,
                            bid = userState?.bid,
                            tricksWon = userState?.tricksWon ?: 0,
                            isDealer = isDealer,
                            isCurrentTurn = isTurn,
                            turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Bid!" else "Play!") else null,
                            totalScore = userState?.totalScore ?: 0,
                            activeEmote = myActiveEmote,
                            isBottomUser = true,
                            is3DMode = appSettings.is3DMode
                        )
                    }
                }

                // Center: Cards in Hand
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isMyTurnToPlay = uiState.phase == GamePhase.PLAYING &&
                            uiState.players.getOrNull(uiState.currentTurnIndex)?.name == myPlayer?.name

                    val playableCards = remember(uiState.userHand, uiState.leadSuit, isMyTurnToPlay) {
                        if (isMyTurnToPlay) {
                            KaachuPhoolEngine.getPlayableCards(uiState.userHand, uiState.leadSuit)
                        } else emptyList()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (uiState.userHand.isEmpty()) {
                            Text(
                                text = if (uiState.phase == GamePhase.ROUND_FINISHED) "Round Finished"
                                       else if (uiState.phase == GamePhase.TRICK_FINISHED) "Trick Finished"
                                       else if (uiState.phase == GamePhase.BIDDING) "Bidding Phase"
                                       else "No cards in hand",
                                color = TextMuted,
                                fontSize = if (isSmallScreen) 11.sp else 13.sp
                            )
                        } else {
                            uiState.userHand.forEach { card ->
                                val isPlayable = playableCards.contains(card)
                                PlayingCardView(
                                    card = card,
                                    isPlayable = isPlayable,
                                    isSelected = false,
                                    isTrump = card.suit == uiState.currentTrump,
                                    width = cardWidth,
                                    height = cardHeight,
                                    onClick = {
                                        if (isMyTurnToPlay && isPlayable) {
                                            viewModel.playUserCard(card)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Right: Emote Reaction Picker & Score Pill
                Column(
                    modifier = Modifier.width(if (isSmallScreen) 96.dp else 110.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "Score: ${userState?.totalScore ?: 0}",
                            color = GoldLight,
                            fontSize = if (isSmallScreen) 11.sp else 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    EmotePickerBar(
                        onEmoteSelected = { emote ->
                            viewModel.sendEmote(emote)
                        }
                    )
                }
            }
        }

        // Bidding Dialog
        if (uiState.isUserBiddingTurn) {
            val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == myPlayer?.name
            BiddingDialog(
                totalCards = uiState.currentRoundCardCount,
                isDealer = isDealer,
                forbiddenBid = uiState.userForbiddenBid,
                onBidSelected = { bid ->
                    viewModel.submitUserBid(bid)
                },
                onLeaveMatch = {
                    showQuitDialog = true
                }
            )
        }

        // Round Summary Dialog
        if (uiState.phase == GamePhase.ROUND_FINISHED) {
            RoundSummaryDialog(
                roundNumber = uiState.currentRoundIndex + 1,
                totalRounds = uiState.rounds.size.coerceAtLeast(1),
                trumpSuit = uiState.currentTrump,
                nextTrumpSuit = uiState.nextTrump,
                playerStates = uiState.playerStates,
                isLastRound = uiState.currentRoundIndex >= uiState.rounds.size - 1,
                onContinueClick = { viewModel.continueNextRound() }
            )
        }

        // Game Over Dialog
        if (uiState.phase == GamePhase.GAME_OVER) {
            val userState = uiState.playerStates.find { it.player.name == uiState.localPlayerName || it.player.id == "user" }
            val highestScorePlayer = uiState.playerStates.maxByOrNull { it.totalScore }
            val userWon = highestScorePlayer != null && (highestScorePlayer.player.name == uiState.localPlayerName || highestScorePlayer.player.id == "user")
            val userScore = userState?.totalScore ?: 0
            val perfectBids = userState?.bid != null && userState.bid == userState.tricksWon

            var newlyUnlocked by remember { mutableStateOf<List<com.example.model.Achievement>>(emptyList()) }
            LaunchedEffect(uiState.phase) {
                newlyUnlocked = userProfileManager.recordGameFinished(
                    userWon = userWon,
                    userTotalScore = userScore,
                    perfectBids = perfectBids,
                    difficultyOrMultiplayer = if (uiState.isMultiplayer) "MULTIPLAYER" else uiState.botDifficulty.name
                )
            }

            GameOverDialog(
                playerStates = uiState.playerStates,
                newlyUnlockedAchievements = newlyUnlocked,
                onPlayAgain = { viewModel.restartCurrentGame() },
                onHomeClick = {
                    viewModel.exitGame()
                    onBackClick()
                }
            )
        }

        // Room Disbanded Dialog
        if (uiState.isRoomDisbanded) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.exitGame()
                    onBackClick()
                },
                title = {
                    Text("Room Disbanded", color = GoldLight, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text("The host has left the match and the room has been disbanded.", color = TextLight)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.exitGame()
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Return to Home", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = DarkSurfaceElevated,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Leave Confirmation Dialog
        if (showQuitDialog) {
            AlertDialog(
                onDismissRequest = { showQuitDialog = false },
                title = { Text("Leave Match?", color = GoldLight, fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to leave? Your match progress will be stopped.", color = TextLight) },
                confirmButton = {
                    Button(
                        onClick = {
                            showQuitDialog = false
                            viewModel.exitGame()
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEF4444))
                    ) {
                        Text("Leave Match", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuitDialog = false }) {
                        Text("Stay", color = TextMuted)
                    }
                },
                containerColor = DarkSurfaceElevated
            )
        }

        // Settings & Sound Effects Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                userProfileManager = userProfileManager,
                onDismiss = { showSettingsDialog = false },
                onLoggedOut = {
                    showSettingsDialog = false
                    viewModel.exitGame()
                    onBackClick()
                },
                onAccountDeleted = {
                    showSettingsDialog = false
                    viewModel.exitGame()
                    onBackClick()
                }
            )
        }
    }
}
