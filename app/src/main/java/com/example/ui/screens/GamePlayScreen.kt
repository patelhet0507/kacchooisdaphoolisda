package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.IntOffset
import com.example.ui.components.OvalTableCanvas
import kotlin.math.abs
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
import com.example.viewmodel.PlayerSeatState
import com.example.viewmodel.TableState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePlayScreen(
    viewModel: GameViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tableStateRaw by viewModel.tableState.collectAsStateWithLifecycle()
    val playerSeatsRaw by viewModel.playerSeats.collectAsStateWithLifecycle()
    val userHandRaw by viewModel.userHand.collectAsStateWithLifecycle()

    val userHand = if (uiState.isMultiplayer) {
        if (userHandRaw.isNotEmpty()) userHandRaw else uiState.userHand
    } else {
        val userStateCards = uiState.playerStates.find { it.player.id == "user" || !it.player.isBot }?.cards ?: emptyList()
        if (uiState.userHand.isNotEmpty()) {
            uiState.userHand
        } else if (userHandRaw.isNotEmpty()) {
            userHandRaw
        } else {
            userStateCards
        }
    }

    val playerSeats = if (playerSeatsRaw.isNotEmpty()) {
        playerSeatsRaw
    } else {
        uiState.players.mapIndexed { index, p ->
            val pState = uiState.playerStates.find { it.player.id == p.id }
            PlayerSeatState(
                player = p,
                bid = pState?.bid,
                tricksWon = pState?.tricksWon ?: 0,
                isDealer = uiState.dealerIndex == index,
                isCurrentTurn = uiState.currentTurnIndex == index,
                totalScore = pState?.totalScore ?: 0,
                activeEmote = uiState.activeEmotes[p.name] ?: uiState.activeEmotes[p.id],
                cardsCount = pState?.cards?.size ?: 0
            )
        }
    }

    val tableState = if (uiState.isMultiplayer && (tableStateRaw.playedCards.isNotEmpty() || tableStateRaw.trickWinner != null)) {
        tableStateRaw
    } else {
        TableState(
            playedCards = uiState.currentTrick,
            trumpSuit = uiState.currentTrump,
            leadSuit = uiState.leadSuit,
            trickWinner = uiState.lastTrickWinner,
            isTrickFinished = uiState.phase == GamePhase.TRICK_FINISHED
        )
    }

    var showQuitDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val soundEffectsManager = remember { SoundEffectsManager.getInstance(context) }
    val userProfileManager = remember { UserProfileManager(context) }
    val settingsManager = remember { com.example.data.SettingsManager.getInstance(context) }
    val appSettings by settingsManager.settings.collectAsStateWithLifecycle()

    LaunchedEffect(soundEffectsManager) {
        viewModel.setSoundEffectsManager(soundEffectsManager, context.applicationContext as android.app.Application)
    }

    LaunchedEffect(uiState.players.isEmpty()) {
        if (uiState.players.isEmpty()) {
            viewModel.startNewGame()
        }
    }

    // Intercept hardware / system back gesture
    BackHandler {
        showQuitDialog = true
    }

    val mySeat = playerSeats.find {
        it.player.id == "user" || it.player.name == uiState.localPlayerName || !it.player.isBot
    } ?: playerSeats.firstOrNull()

    val opponentSeats = playerSeats.filter { it.player.id != mySeat?.player?.id && it.player.name != mySeat?.player?.name }

    // Dynamic opponent layout mapping
    val topOpponentSeats = when (opponentSeats.size) {
        1 -> listOf(opponentSeats[0])
        2 -> emptyList()
        3 -> listOf(opponentSeats[1])
        4 -> listOf(opponentSeats[1], opponentSeats[2])
        else -> opponentSeats.drop(1).dropLast(1)
    }
    val leftOpponentSeat = when (opponentSeats.size) {
        1 -> null
        2 -> opponentSeats[0]
        else -> opponentSeats.firstOrNull()
    }
    val rightOpponentSeat = when (opponentSeats.size) {
        1 -> null
        2 -> opponentSeats[1]
        else -> opponentSeats.lastOrNull()
    }

    if (uiState.isMultiplayer && uiState.players.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(DarkBackground), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator(color = GoldPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Waiting for game state...", color = GoldLight)
            }
        }
        return
    }

    Scaffold(
        containerColor = DarkBackground
    ) { innerPadding ->
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val isSmallScreen = configuration.screenHeightDp < 600
        val cardWidth = if (isSmallScreen) 60.dp else 84.dp
        val cardHeight = if (isSmallScreen) 88.dp else 122.dp

        val density = androidx.compose.ui.platform.LocalDensity.current

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val screenWidth = maxWidth
            val screenHeight = maxHeight

            // 1. Oval Table (Centered, takes ~65% width, ~45% height)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.65f)
                    .fillMaxHeight(0.45f)
                    .padding(bottom = 40.dp) // Slight shift up for hand space
            ) {
                TrickTableView(
                    modifier = Modifier.fillMaxSize(),
                    playedCards = tableState.playedCards,
                    trumpSuit = tableState.trumpSuit,
                    leadSuit = tableState.leadSuit,
                    trickWinner = tableState.trickWinner,
                    isTrickFinished = tableState.isTrickFinished,
                    onNextTrickClick = { viewModel.continueNextTrick() },
                    is3DMode = appSettings.is3DMode
                )
            }

            // 2. Top Opponent Seats
            val topSeatsCount = topOpponentSeats.size
            topOpponentSeats.forEachIndexed { index, seat ->
                val xOffset = if (topSeatsCount > 1) {
                    val spread = 120.dp
                    with(density) { (index - (topSeatsCount - 1) / 2f) * spread.toPx() }
                } else 0f
                
                PlayerSeatView(
                    player = seat.player,
                    bid = seat.bid,
                    tricksWon = seat.tricksWon,
                    isDealer = seat.isDealer,
                    isCurrentTurn = seat.isCurrentTurn,
                    turnActionText = if (seat.isCurrentTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                    totalScore = seat.totalScore,
                    activeEmote = seat.activeEmote,
                    is3DMode = appSettings.is3DMode,
                    cardCount = seat.cardsCount,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(xOffset.toInt(), with(density) { 20.dp.toPx().toInt() }) }
                )
            }

            // 3. Left Opponent Seat
            if (leftOpponentSeat != null) {
                PlayerSeatView(
                    player = leftOpponentSeat.player,
                    bid = leftOpponentSeat.bid,
                    tricksWon = leftOpponentSeat.tricksWon,
                    isDealer = leftOpponentSeat.isDealer,
                    isCurrentTurn = leftOpponentSeat.isCurrentTurn,
                    turnActionText = if (leftOpponentSeat.isCurrentTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                    totalScore = leftOpponentSeat.totalScore,
                    activeEmote = leftOpponentSeat.activeEmote,
                    is3DMode = appSettings.is3DMode,
                    cardCount = leftOpponentSeat.cardsCount,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(x = 10.dp, y = (-20).dp)
                )
            }

            // 4. Right Opponent Seat
            if (rightOpponentSeat != null) {
                PlayerSeatView(
                    player = rightOpponentSeat.player,
                    bid = rightOpponentSeat.bid,
                    tricksWon = rightOpponentSeat.tricksWon,
                    isDealer = rightOpponentSeat.isDealer,
                    isCurrentTurn = rightOpponentSeat.isCurrentTurn,
                    turnActionText = if (rightOpponentSeat.isCurrentTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                    totalScore = rightOpponentSeat.totalScore,
                    activeEmote = rightOpponentSeat.activeEmote,
                    is3DMode = appSettings.is3DMode,
                    cardCount = rightOpponentSeat.cardsCount,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = (-10).dp, y = (-20).dp)
                )
            }

            // 5. Top Bar Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showQuitDialog = true }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = GoldPrimary)
                }
                
                TrumpIndicator(
                    currentTrump = uiState.currentTrump,
                    roundNumber = uiState.currentRoundIndex + 1,
                    totalRounds = uiState.rounds.size,
                    cardCount = uiState.currentRoundCardCount,
                    modifier = Modifier.width(180.dp)
                )

                Row {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, "Settings", tint = GoldLight)
                    }
                    IconButton(onClick = { viewModel.restartCurrentGame() }) {
                        Icon(Icons.Default.Refresh, "Restart", tint = TextMuted)
                    }
                }
            }

            // 6. Bottom User Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                // User seat on the left
                if (mySeat != null) {
                    PlayerSeatView(
                        player = mySeat.player,
                        bid = mySeat.bid,
                        tricksWon = mySeat.tricksWon,
                        isDealer = mySeat.isDealer,
                        isCurrentTurn = mySeat.isCurrentTurn,
                        turnActionText = if (mySeat.isCurrentTurn) (if (uiState.phase == GamePhase.BIDDING) "Bid!" else "Play!") else null,
                        totalScore = mySeat.totalScore,
                        activeEmote = mySeat.activeEmote,
                        isBottomUser = true,
                        is3DMode = appSettings.is3DMode,
                        cardCount = mySeat.cardsCount,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                // Cards in hand (Centered, fan arrangement)
                val isMyTurnToPlay = uiState.phase == GamePhase.PLAYING && mySeat?.isCurrentTurn == true
                val playableCards = if (isMyTurnToPlay) KaachuPhoolEngine.getPlayableCards(userHand, uiState.leadSuit) else emptyList()

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.6f)
                        .height(cardHeight + 20.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    userHand.forEachIndexed { index, card ->
                        val totalCards = userHand.size
                        val maxFanAngle = 20f
                        val normalizedPosition = if (totalCards > 1) {
                            (index - (totalCards - 1) / 2f) / (totalCards - 1)
                        } else 0f
                        
                        val rotation = normalizedPosition * maxFanAngle
                        val yOffset = abs(normalizedPosition) * 12f // pixels
                        val xOffset = normalizedPosition * (totalCards * 15f) // pixels

                        PlayingCardView(
                            card = card,
                            isPlayable = playableCards.contains(card),
                            isTrump = card.suit == tableState.trumpSuit,
                            width = cardWidth,
                            height = cardHeight,
                            is3DMode = appSettings.is3DMode,
                            onClick = { if (isMyTurnToPlay && playableCards.contains(card)) viewModel.playUserCard(card) },
                            modifier = Modifier
                                .offset { IntOffset(with(density) { xOffset.dp.toPx().toInt() }, with(density) { yOffset.dp.toPx().toInt() }) }
                                .graphicsLayer { rotationZ = rotation }
                        )
                    }
                }

                // Score + Emotes on the right
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 8.dp),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Score: ${mySeat?.totalScore ?: 0}", color = GoldLight, fontWeight = FontWeight.Bold)
                    }
                    EmotePickerBar(onEmoteSelected = { viewModel.sendEmote(it) })
                }
            }
        }

        // Bidding Dialog
        if (uiState.isUserBiddingTurn) {
            val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == mySeat?.player?.name
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
