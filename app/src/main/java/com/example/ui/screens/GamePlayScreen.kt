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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.engine.KaachuPhoolEngine
import com.example.model.GamePhase
import com.example.model.Player
import com.example.ui.components.BiddingDialog
import com.example.ui.components.GameOverDialog
import com.example.ui.components.PlayerSeatView
import com.example.ui.components.PlayingCardView
import com.example.ui.components.RoundSummaryDialog
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
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (uiState.isMultiplayer) "Play Together Match" else "Kaachu Phool Match",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        if (uiState.isMultiplayer && uiState.roomCode != null) {
                            Text(
                                text = "Table Code: ${uiState.roomCode} • ${uiState.players.size} Players Online",
                                color = EmeraldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showQuitDialog = true },
                        modifier = Modifier.testTag("game_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave Match",
                            tint = GoldPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.restartCurrentGame() },
                        modifier = Modifier.testTag("restart_match_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Match",
                            tint = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Trump Rotation & Round Info Banner
            TrumpIndicator(
                currentTrump = uiState.currentTrump,
                roundNumber = uiState.currentRoundIndex + 1,
                totalRounds = uiState.rounds.size.coerceAtLeast(1),
                cardCount = uiState.currentRoundCardCount
            )

            // 2. Center Playing Table with Seated Opponents
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Opponents Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    topOpponents.forEach { opponent ->
                        val pState = uiState.playerStates.find { it.player.name == opponent.name }
                        val isTurn = uiState.players.getOrNull(uiState.currentTurnIndex)?.name == opponent.name
                        val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == opponent.name

                        PlayerSeatView(
                            player = opponent,
                            bid = pState?.bid,
                            tricksWon = pState?.tricksWon ?: 0,
                            isDealer = isDealer,
                            isCurrentTurn = isTurn,
                            turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                            totalScore = pState?.totalScore ?: 0
                        )
                    }
                }

                // Middle Row: Left Opponent - Table Arena - Right Opponent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Opponent
                    Box(
                        modifier = Modifier.width(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (leftOpponent != null) {
                            val pState = uiState.playerStates.find { it.player.name == leftOpponent.name }
                            val isTurn = uiState.players.getOrNull(uiState.currentTurnIndex)?.name == leftOpponent.name
                            val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == leftOpponent.name

                            PlayerSeatView(
                                player = leftOpponent,
                                bid = pState?.bid,
                                tricksWon = pState?.tricksWon ?: 0,
                                isDealer = isDealer,
                                isCurrentTurn = isTurn,
                                turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                                totalScore = pState?.totalScore ?: 0
                            )
                        }
                    }

                    // Trick Arena (Center Felt Table)
                    Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                        TrickTableView(
                            playedCards = uiState.currentTrick,
                            trumpSuit = uiState.currentTrump,
                            leadSuit = uiState.leadSuit,
                            trickWinner = uiState.lastTrickWinner,
                            isTrickFinished = uiState.phase == GamePhase.TRICK_FINISHED,
                            onNextTrickClick = { viewModel.continueNextTrick() }
                        )
                    }

                    // Right Opponent
                    Box(
                        modifier = Modifier.width(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (rightOpponent != null) {
                            val pState = uiState.playerStates.find { it.player.name == rightOpponent.name }
                            val isTurn = uiState.players.getOrNull(uiState.currentTurnIndex)?.name == rightOpponent.name
                            val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == rightOpponent.name

                            PlayerSeatView(
                                player = rightOpponent,
                                bid = pState?.bid,
                                tricksWon = pState?.tricksWon ?: 0,
                                isDealer = isDealer,
                                isCurrentTurn = isTurn,
                                turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Bidding..." else "Playing...") else null,
                                totalScore = pState?.totalScore ?: 0
                            )
                        }
                    }
                }

                // Table Status Message Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.9f))
                        .border(1.dp, EmeraldBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.statusMessage,
                        color = GoldLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 3. Bottom Player Controls & Hand
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // User Stats Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (myPlayer != null) {
                        val isTurn = uiState.players.getOrNull(uiState.currentTurnIndex)?.name == myPlayer.name
                        val isDealer = uiState.players.getOrNull(uiState.dealerIndex)?.name == myPlayer.name

                        PlayerSeatView(
                            player = myPlayer,
                            bid = userState?.bid,
                            tricksWon = userState?.tricksWon ?: 0,
                            isDealer = isDealer,
                            isCurrentTurn = isTurn,
                            turnActionText = if (isTurn) (if (uiState.phase == GamePhase.BIDDING) "Your Turn to Bid!" else "Your Turn to Play!") else null,
                            totalScore = userState?.totalScore ?: 0,
                            isBottomUser = true
                        )
                    }

                    // Score pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurface)
                            .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Score: ${userState?.totalScore ?: 0} pts",
                            color = GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // User Hand Cards
                val isMyTurnToPlay = uiState.phase == GamePhase.PLAYING &&
                        uiState.players.getOrNull(uiState.currentTurnIndex)?.name == myPlayer?.name

                val playableCards = remember(uiState.userHand, uiState.leadSuit, isMyTurnToPlay) {
                    if (isMyTurnToPlay) {
                        KaachuPhoolEngine.getPlayableCards(uiState.userHand, uiState.leadSuit)
                    } else emptyList()
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (uiState.userHand.isEmpty()) {
                        Text(
                            text = if (uiState.phase == GamePhase.ROUND_FINISHED) "Round finished" else "No cards in hand",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        uiState.userHand.forEach { card ->
                            val isPlayable = playableCards.contains(card)
                            PlayingCardView(
                                card = card,
                                isPlayable = isPlayable,
                                isSelected = false,
                                isTrump = card.suit == uiState.currentTrump,
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
            GameOverDialog(
                playerStates = uiState.playerStates,
                onPlayAgain = { viewModel.restartCurrentGame() },
                onHomeClick = {
                    viewModel.exitGame()
                    onBackClick()
                }
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
    }
}
