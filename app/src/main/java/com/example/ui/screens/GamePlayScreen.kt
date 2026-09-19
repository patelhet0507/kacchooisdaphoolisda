package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.components.PremiumTrumpIndicator
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.ui.components.PremiumButton
import com.example.ui.util.calculateWindowLayoutInfo
import kotlinx.coroutines.delay
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
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ErrorRed
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

    BackHandler {
        showQuitDialog = true
    }

    val mySeat = playerSeats.find {
        it.player.id == "user" || it.player.name == uiState.localPlayerName || !it.player.isBot
    } ?: playerSeats.firstOrNull()

    val myIdx = playerSeats.indexOf(mySeat).coerceAtLeast(0)
    val numPlayers = playerSeats.size

    val sortedOpponents = (1 until numPlayers).map { relIdx ->
        playerSeats[(myIdx + relIdx) % numPlayers]
    }

    if (uiState.isMultiplayer && uiState.players.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(DeepEmerald), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.CircularProgressIndicator(color = GoldPrimary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Waiting for players...", color = GoldLight)
            }
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepEmerald)) {
        // Table Pattern
        Image(
            painter = painterResource(id = R.drawable.img_table_texture),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.05f
        )

        var selectedCardId by remember { mutableStateOf<String?>(null) }
        LaunchedEffect(userHand) {
            if (userHand.none { it.id == selectedCardId }) {
                selectedCardId = null
            }
        }

        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val screenWidth = maxWidth
                val screenHeight = maxHeight
                val layoutInfo = calculateWindowLayoutInfo(screenWidth, screenHeight)
                val isLandscape = layoutInfo.isLandscape
                val isTablet = layoutInfo.isTablet
                val isCompact = layoutInfo.isCompact

                // Dynamically calculated table dimensions
                val tableWidth = when {
                    isTablet && isLandscape -> (maxWidth * 0.54f).coerceIn(500.dp, 720.dp)
                    isTablet -> (maxWidth * 0.72f).coerceIn(440.dp, 640.dp)
                    isLandscape -> (maxWidth * 0.56f).coerceIn(320.dp, 540.dp)
                    else -> (maxWidth - 32.dp).coerceIn(280.dp, 420.dp)
                }
                val tableHeight = when {
                    isTablet && isLandscape -> (maxHeight * 0.48f).coerceIn(300.dp, 440.dp)
                    isTablet -> (maxHeight * 0.40f).coerceIn(280.dp, 400.dp)
                    isLandscape -> (maxHeight * 0.52f).coerceIn(160.dp, 230.dp)
                    else -> (maxHeight * 0.36f).coerceIn(170.dp, 260.dp)
                }
                val tableCenterYOffset = when {
                    isTablet -> (-35).dp
                    isLandscape -> (-18).dp
                    else -> (-28).dp
                }

                // Table Center Anchor
                val tableCenterX = maxWidth / 2
                val tableCenterY = (maxHeight / 2) + tableCenterYOffset

                // Game Table (Visually centered)
                Box(
                    modifier = Modifier
                        .size(tableWidth, tableHeight)
                        .align(Alignment.Center)
                        .offset(y = tableCenterYOffset)
                ) {
                    TrickTableView(
                        modifier = Modifier.fillMaxSize(),
                        playedCards = tableState.playedCards,
                        allPlayers = uiState.players.map { it.name },
                        localPlayerName = uiState.localPlayerName,
                        trumpSuit = tableState.trumpSuit,
                        leadSuit = tableState.leadSuit,
                        trickWinner = tableState.trickWinner,
                        isTrickFinished = tableState.isTrickFinished,
                        onNextTrickClick = { viewModel.continueNextTrick() },
                        is3DMode = appSettings.is3DMode
                    )
                }

                // Opponents anchored relative to the game table
                val playerDistance = if (isTablet) 44.dp else if (isLandscape) 32.dp else 22.dp
                sortedOpponents.forEachIndexed { index, seat ->
                    val (posX, posY) = when (sortedOpponents.size) {
                        1 -> {
                            // 2-Player game: Opponent directly across
                            tableCenterX to (tableCenterY - (tableHeight / 2) - playerDistance)
                        }
                        2 -> {
                            // 3-Player game: Opponents at top-left and top-right
                            if (index == 0) {
                                (tableCenterX - (tableWidth * 0.38f) - playerDistance * 0.8f) to (tableCenterY - (tableHeight / 2) - playerDistance * 0.7f)
                            } else {
                                (tableCenterX + (tableWidth * 0.38f) + playerDistance * 0.8f) to (tableCenterY - (tableHeight / 2) - playerDistance * 0.7f)
                            }
                        }
                        else -> {
                            // 4-Player game: Left, Top, Right
                            when (index) {
                                0 -> (tableCenterX - (tableWidth / 2) - playerDistance) to tableCenterY
                                1 -> tableCenterX to (tableCenterY - (tableHeight / 2) - playerDistance)
                                else -> (tableCenterX + (tableWidth / 2) + playerDistance) to tableCenterY
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.offset(x = posX - 42.dp, y = posY - 36.dp)
                    ) {
                        PlayerSeatView(
                            player = seat.player,
                            bid = seat.bid,
                            tricksWon = seat.tricksWon,
                            isDealer = seat.isDealer,
                            isCurrentTurn = seat.isCurrentTurn,
                            totalScore = seat.totalScore,
                            activeEmote = seat.activeEmote,
                            cardCount = seat.cardsCount
                        )
                    }
                }

                // Top Controls & Status Bar (Respects cutouts & status bars)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showQuitDialog = true },
                        modifier = Modifier
                            .size(44.dp)
                            .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Leave Match", tint = GoldPrimary)
                    }

                    // Centered match info chip
                    Surface(
                        color = DarkSurface.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ROUND ${uiState.currentRoundIndex + 1}/${uiState.rounds.size.coerceAtLeast(1)}",
                                color = GoldLight,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            tableState.trumpSuit?.let { trump ->
                                Text(
                                    text = "TRUMP: ${trump.symbol}",
                                    color = if (trump.isRed) ErrorRed else GoldLight,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(44.dp)
                                .background(DarkSurface.copy(alpha = 0.7f), CircleShape)
                        ) {
                            Icon(Icons.Default.Settings, "Settings", tint = GoldPrimary)
                        }
                    }
                }

                // Bottom Player Hand & Controls (Safe above system navigation)
                val isMyTurnToPlay = uiState.phase == GamePhase.PLAYING && mySeat?.isCurrentTurn == true
                val playableCards = if (isMyTurnToPlay) KaachuPhoolEngine.getPlayableCards(userHand, uiState.leadSuit) else emptyList()
                val selectedCard = userHand.find { it.id == selectedCardId }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // User Seat (Bottom-Left)
                    if (mySeat != null) {
                        PlayerSeatView(
                            player = mySeat.player,
                            bid = mySeat.bid,
                            tricksWon = mySeat.tricksWon,
                            isDealer = mySeat.isDealer,
                            isCurrentTurn = mySeat.isCurrentTurn,
                            totalScore = mySeat.totalScore,
                            activeEmote = mySeat.activeEmote,
                            isBottomUser = true,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(bottom = 6.dp)
                        )
                    }

                    // Center Column: Action Button (above) + Hand (below)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Play Card Action Button (Placed cleanly above cards, never overlaps!)
                        AnimatedVisibility(
                            visible = isMyTurnToPlay && selectedCard != null && playableCards.contains(selectedCard),
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut() + slideOutVertically { it / 2 }
                        ) {
                            PremiumButton(
                                text = "PLAY ${selectedCard?.rank?.symbol ?: ""} ${selectedCard?.suit?.symbol ?: ""}",
                                onClick = {
                                    selectedCard?.let { card ->
                                        if (playableCards.contains(card)) {
                                            viewModel.playUserCard(card)
                                            selectedCardId = null
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .height(44.dp)
                            )
                        }

                        // Card Hand with Dynamic Overlap & Scaling
                        val availableHandWidth = if (isLandscape) {
                            (screenWidth * 0.58f).coerceAtMost(680.dp)
                        } else {
                            (screenWidth - 140.dp).coerceAtLeast(180.dp)
                        }

                        val baseCardWidth = if (isTablet) 76.dp else if (isCompact) 56.dp else 66.dp
                        val baseCardHeight = baseCardWidth * 1.45f
                        val totalCards = userHand.size

                        val availableHandWidthVal = availableHandWidth.value
                        val baseCardWidthVal = baseCardWidth.value
                        val stepVal = if (totalCards <= 1) {
                            0f
                        } else {
                            val unconstrainedStep = baseCardWidthVal * 0.62f
                            val maxStepAllowed = (availableHandWidthVal - baseCardWidthVal) / (totalCards - 1).toFloat()
                            unconstrainedStep.coerceAtMost(maxStepAllowed).coerceAtLeast(14f)
                        }
                        val handTotalWidthVal = if (totalCards <= 1) baseCardWidthVal else baseCardWidthVal + (stepVal * (totalCards - 1))
                        val startOffsetVal = (availableHandWidthVal - handTotalWidthVal) / 2f

                        Box(
                            modifier = Modifier
                                .width(availableHandWidth)
                                .height(baseCardHeight + 32.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            userHand.forEachIndexed { index, card ->
                                val isSelected = card.id == selectedCardId
                                val cardX = (startOffsetVal + stepVal * index).dp
                                val targetY = if (isSelected) (-22).dp else 0.dp
                                val animY by animateDpAsState(
                                    targetValue = targetY,
                                    animationSpec = spring(dampingRatio = 0.8f, stiffness = 350f),
                                    label = "card_select_y"
                                )

                                PlayingCardView(
                                    card = card,
                                    isPlayable = playableCards.contains(card),
                                    isTrump = card.suit == tableState.trumpSuit,
                                    isSelected = isSelected,
                                    width = baseCardWidth,
                                    height = baseCardHeight,
                                    onClick = {
                                        if (isMyTurnToPlay && playableCards.contains(card)) {
                                            if (selectedCardId == card.id) {
                                                viewModel.playUserCard(card)
                                                selectedCardId = null
                                            } else {
                                                selectedCardId = card.id
                                                soundEffectsManager.playCardPlay()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .offset(x = cardX, y = animY)
                                )
                            }
                        }
                    }

                    // Emote Picker & Trump Badge (Bottom-Right)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 6.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EmotePickerBar(onEmoteSelected = { viewModel.sendEmote(it) })

                        PremiumTrumpIndicator(
                            suit = tableState.trumpSuit,
                            round = uiState.currentRoundIndex + 1,
                            totalRounds = uiState.rounds.size,
                            cardsInRound = uiState.currentRoundCardCount
                        )
                    }
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
