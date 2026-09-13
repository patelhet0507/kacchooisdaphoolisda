package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ScorecardGameEntity
import com.example.model.GameMode
import com.example.model.ScoringRule
import com.example.model.Suit
import com.example.ui.components.TrumpIndicator
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarningAmber
import com.example.viewmodel.ScorecardViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScorecardScreen(
    viewModel: ScorecardViewModel,
    onBackClick: () -> Unit
) {
    val activeState by viewModel.activeState.collectAsStateWithLifecycle()
    val savedGames by viewModel.allSavedGames.collectAsStateWithLifecycle()

    BackHandler {
        onBackClick()
    }

    var showNewGameDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Round Entry, 1: Full Scoreboard, 2: Leaderboard

    // Edit Player Name State
    var editingPlayerIndex by remember { mutableStateOf<Int?>(null) }
    var editingPlayerCurrentName by remember { mutableStateOf("") }

    val activeGame = activeState.activeGame

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = activeGame?.title ?: "Kaachu Phool Scorekeeper",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("scorecard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showNewGameDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("new_scorecard_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldDeep, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Match", color = EmeraldDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (activeGame == null) {
            // No active game loaded: show saved games or prompt to create one
            EmptyScorecardHome(
                savedGames = savedGames,
                onGameClick = { viewModel.loadGame(it.id) },
                onDeleteClick = { viewModel.deleteGame(it.id) },
                onNewMatchClick = { showNewGameDialog = true },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            // Active match is loaded!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurface,
                    contentColor = GoldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldPrimary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Round Entry", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Scorecard Table", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Standings", fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp) }
                    )
                }

                when (selectedTab) {
                    0 -> RoundEntryTab(
                        viewModel = viewModel,
                        onEditPlayerName = { index, name ->
                            editingPlayerIndex = index
                            editingPlayerCurrentName = name
                        }
                    )
                    1 -> FullScoreboardTab(
                        viewModel = viewModel,
                        onEditPlayerName = { index, name ->
                            editingPlayerIndex = index
                            editingPlayerCurrentName = name
                        }
                    )
                    2 -> StandingsTab(
                        viewModel = viewModel,
                        onEditPlayerName = { index, name ->
                            editingPlayerIndex = index
                            editingPlayerCurrentName = name
                        }
                    )
                }
            }
        }
    }

    // Rename Player Dialog
    if (editingPlayerIndex != null) {
        var tempName by remember(editingPlayerCurrentName) { mutableStateOf(editingPlayerCurrentName) }
        AlertDialog(
            onDismissRequest = { editingPlayerIndex = null },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Edit Player Name",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter new name for player ${(editingPlayerIndex ?: 0) + 1}:",
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = EmeraldBorder,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_player_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idx = editingPlayerIndex
                        if (idx != null && tempName.isNotBlank()) {
                            viewModel.renamePlayer(idx, tempName.trim())
                        }
                        editingPlayerIndex = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Save", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPlayerIndex = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }

    if (showNewGameDialog) {
        NewScorecardGameDialog(
            onDismiss = { showNewGameDialog = false },
            onCreate = { title, players, mode, rule, customRounds ->
                viewModel.createNewMatch(title, players, mode, rule, customRounds) {
                    showNewGameDialog = false
                    selectedTab = 0
                }
            }
        )
    }
}

@Composable
private fun EmptyScorecardHome(
    savedGames: List<ScorecardGameEntity>,
    onGameClick: (ScorecardGameEntity) -> Unit,
    onDeleteClick: (ScorecardGameEntity) -> Unit,
    onNewMatchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(EmeraldBorder)
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📋 Real Card Game Scorekeeper",
                        color = GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Playing with physical playing cards with friends & family? Let this app track trumps, rotation, bids, dealer hook rule, and auto-score every round with custom rules and customizable cards!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNewMatchClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("start_first_scorecard_button")
                    ) {
                        Text("Create New Scorecard Match", color = EmeraldDeep, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        if (savedGames.isNotEmpty()) {
            item {
                Text(
                    text = "Saved Matches History",
                    color = GoldLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            items(savedGames) { game ->
                val dateStr = remember(game.createdAt) {
                    SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(game.createdAt))
                }
                val players = remember(game.playerNamesRaw) { game.getPlayerNames() }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGameClick(game) }
                        .testTag("saved_game_${game.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(EmeraldBorder.copy(alpha = 0.5f))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = game.title,
                                color = TextLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Players: ${players.joinToString(", ")}",
                                color = TextMuted,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = dateStr,
                                color = GoldPrimary,
                                fontSize = 10.sp
                            )
                        }

                        IconButton(onClick = { onDeleteClick(game) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundEntryTab(
    viewModel: ScorecardViewModel,
    onEditPlayerName: (index: Int, currentName: String) -> Unit
) {
    val state by viewModel.activeState.collectAsStateWithLifecycle()
    val game = state.activeGame ?: return
    val currentRound = state.rounds.getOrNull(state.currentRoundIndex)
    val players = remember(game.playerNamesRaw) { game.getPlayerNames() }

    if (currentRound == null || state.isRoundComplete) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🎉", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Match Completed!",
                    color = GoldLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All rounds have been played and scored.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }
        return
    }

    val trumpSuit = remember(currentRound.trumpSuitName) {
        runCatching { Suit.valueOf(currentRound.trumpSuitName) }.getOrDefault(Suit.SPADES)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Trump rotation & round status
            TrumpIndicator(
                currentTrump = trumpSuit,
                roundNumber = state.currentRoundIndex + 1,
                totalRounds = state.rounds.size,
                cardCount = currentRound.cardCount
            )
        }

        item {
            // Scoring Rule & Dealer Info Banner
            val dealerName = players.getOrNull(state.dealerIndex) ?: "Dealer"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(GoldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("D", color = EmeraldDeep, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                            Text(
                                text = "Dealer: $dealerName (bids last)",
                                color = TextLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (state.forbiddenBid != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ErrorRed.copy(alpha = 0.2f))
                                    .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Hook: Cannot bid ${state.forbiddenBid}",
                                    color = ErrorRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rule: ${state.scoringRule.title}",
                        color = GoldLight,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Players entry rows
        items(players.indices.toList()) { index ->
            val playerName = players[index]
            val isDealer = index == state.dealerIndex
            val bid = state.draftBids.getOrNull(index)
            val won = state.draftTricks.getOrNull(index)
            val isHookForbidden = isDealer && state.forbiddenBid != null && bid == state.forbiddenBid

            val roundScore = if (bid != null && won != null) {
                state.scoringRule.calculateScore(bid, won)
            } else null

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isHookForbidden) ErrorRed else EmeraldBorder.copy(alpha = 0.4f)
                    )
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.clickable { onEditPlayerName(index, playerName) }
                        ) {
                            Text(
                                text = playerName,
                                color = TextLight,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit name",
                                tint = GoldPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                            if (isDealer) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("D", color = EmeraldDeep, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }

                        if (roundScore != null) {
                            Text(
                                text = if (roundScore >= 0) "+$roundScore pts" else "$roundScore pts",
                                color = if (roundScore > 0) SuccessGreen else if (roundScore == 0) TextMuted else ErrorRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Bid Stepper
                        NumberStepper(
                            label = "Bid",
                            value = bid,
                            maxValue = currentRound.cardCount,
                            forbiddenValue = if (isDealer) state.forbiddenBid else null,
                            onValueChange = { viewModel.setPlayerBid(index, it) }
                        )

                        // Tricks Won Stepper
                        NumberStepper(
                            label = "Won",
                            value = won,
                            maxValue = currentRound.cardCount,
                            forbiddenValue = null,
                            onValueChange = { viewModel.setPlayerTricks(index, it) }
                        )
                    }
                }
            }
        }

        // Tricks sum error banner if any
        if (state.tricksSumError != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorRed.copy(alpha = 0.15f))
                        .border(1.dp, ErrorRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = state.tricksSumError!!,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Button(
                onClick = { viewModel.saveAndNextRound() },
                enabled = state.draftBids.all { it != null } && state.draftTricks.all { it != null },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    disabledContainerColor = DarkSurfaceElevated
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_next_round_button")
            ) {
                Text(
                    text = "Save Round & Next Round »",
                    color = EmeraldDeep,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NumberStepper(
    label: String,
    value: Int?,
    maxValue: Int,
    forbiddenValue: Int?,
    onValueChange: (Int?) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "$label:",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = {
                val current = value ?: 0
                if (current > 0) onValueChange(current - 1)
            },
            enabled = (value ?: 0) > 0,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TextLight, modifier = Modifier.size(16.dp))
        }

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (value == forbiddenValue && forbiddenValue != null) ErrorRed.copy(alpha = 0.2f) else DarkSurfaceElevated
                )
                .border(
                    1.dp,
                    if (value == forbiddenValue && forbiddenValue != null) ErrorRed else EmeraldBorder.copy(alpha = 0.4f),
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value?.toString() ?: "-",
                color = if (value == forbiddenValue && forbiddenValue != null) ErrorRed else GoldLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }

        IconButton(
            onClick = {
                val current = value ?: -1
                if (current < maxValue) onValueChange(current + 1)
            },
            enabled = (value ?: 0) < maxValue,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DarkSurfaceElevated)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TextLight, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun FullScoreboardTab(
    viewModel: ScorecardViewModel,
    onEditPlayerName: (index: Int, currentName: String) -> Unit
) {
    val state by viewModel.activeState.collectAsStateWithLifecycle()
    val game = state.activeGame ?: return
    val players = remember(game.playerNamesRaw) { game.getPlayerNames() }
    val horizontalScroll = rememberScrollState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rnd", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                        Text("Cards", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                        Text("Trump", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)

                        players.forEachIndexed { pIdx, pName ->
                            Row(
                                modifier = Modifier
                                    .width(96.dp)
                                    .clickable { onEditPlayerName(pIdx, pName) }
                                    .padding(horizontal = 2.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = pName,
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldPrimary.copy(alpha = 0.7f), modifier = Modifier.size(10.dp))
                            }
                        }
                    }

                    // Data Rows
                    state.rounds.forEachIndexed { rIndex, round ->
                        val trump = runCatching { Suit.valueOf(round.trumpSuitName) }.getOrDefault(Suit.SPADES)
                        val bids = round.getBids()
                        val tricks = round.getTricksWon()
                        val scores = round.getRoundScores()

                        val isCurrent = rIndex == state.currentRoundIndex

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) GoldPrimary.copy(alpha = 0.08f) else DarkSurface)
                                .border(
                                    1.dp,
                                    if (isCurrent) GoldPrimary.copy(alpha = 0.4f) else EmeraldBorder.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${rIndex + 1}", color = TextLight, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                            Text("${round.cardCount}", color = TextLight, fontSize = 11.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)

                            // Trump
                            Row(
                                modifier = Modifier.width(50.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = trump.symbol,
                                    color = if (trump.isRed) Color(0xFFEF4444) else TextLight,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = trump.mnemonic,
                                    color = GoldPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Each player's column (Bid / Won / Pts)
                            players.forEachIndexed { pIndex, _ ->
                                val b = bids.getOrNull(pIndex)
                                val w = tricks.getOrNull(pIndex)
                                val s = scores.getOrNull(pIndex) ?: 0
                                val isDealer = pIndex == round.dealerIndex

                                Column(
                                    modifier = Modifier.width(96.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (b != null && w != null) {
                                        Text(
                                            text = "B:$b • W:$w",
                                            color = TextMuted,
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = if (s > 0) "+$s" else "$s",
                                            color = if (s > 0) SuccessGreen else if (s == 0) TextMuted else ErrorRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = if (isDealer) "(Dealer)" else "-",
                                            color = TextMuted.copy(alpha = 0.5f),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StandingsTab(
    viewModel: ScorecardViewModel,
    onEditPlayerName: (index: Int, currentName: String) -> Unit
) {
    val state by viewModel.activeState.collectAsStateWithLifecycle()
    val game = state.activeGame ?: return
    val players = remember(game.playerNamesRaw) { game.getPlayerNames() }

    // Calculate total score for each player
    val totalScores = remember(state.rounds) {
        val totals = MutableList(players.size) { 0 }
        for (round in state.rounds) {
            val scores = round.getRoundScores()
            scores.forEachIndexed { idx, s ->
                if (idx < totals.size) {
                    totals[idx] += s
                }
            }
        }
        totals
    }

    val rankedPlayers = remember(totalScores, players) {
        players.indices.map { idx ->
            Triple(idx, players[idx], totalScores.getOrElse(idx) { 0 })
        }.sortedByDescending { it.third }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "🏆 Current Standings & Total Scores",
                color = GoldLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tap any player's name to edit.",
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(rankedPlayers.indices.toList()) { rankIdx ->
            val (originalIdx, name, score) = rankedPlayers[rankIdx]
            val isLeader = rankIdx == 0 && score > 0
            val medal = when (rankIdx) {
                0 -> "🥇"
                1 -> "🥈"
                2 -> "🥉"
                else -> "#${rankIdx + 1}"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditPlayerName(originalIdx, name) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLeader) GoldPrimary.copy(alpha = 0.12f) else DarkSurface
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isLeader) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f)
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = medal,
                            fontSize = if (rankIdx < 3) 22.sp else 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextLight
                        )
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = name,
                                    color = if (isLeader) GoldLight else TextLight,
                                    fontSize = 16.sp,
                                    fontWeight = if (isLeader) FontWeight.Black else FontWeight.SemiBold
                                )
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = GoldPrimary.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
                            }
                            if (isLeader) {
                                Text(text = "Match Leader", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "$score pts",
                        color = if (isLeader) GoldPrimary else TextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun NewScorecardGameDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, players: List<String>, mode: GameMode, rule: ScoringRule, customRounds: List<Int>?) -> Unit
) {
    var title by remember { mutableStateOf("Game Night") }
    var selectedMode by remember { mutableStateOf(GameMode.CLASSIC) }
    var selectedRule by remember { mutableStateOf(ScoringRule.STANDARD) }
    var maxCardsChoice by remember { mutableIntStateOf(8) }
    var roundTypeChoice by remember { mutableIntStateOf(0) } // 0: Ladder (1->Max->1), 1: Ascending (1->Max)
    val playersList = remember { mutableStateListOf("Player 1", "Player 2", "Player 3", "Player 4") }
    var newPlayerName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(text = "Create Scorecard Match", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Match Title
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Match Title", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = EmeraldBorder,
                            focusedTextColor = TextLight,
                            unfocusedTextColor = TextLight
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Players List
                item {
                    Text(text = "Players (${playersList.size}):", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        playersList.forEachIndexed { idx, pName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${idx + 1}. $pName", color = TextLight, fontSize = 13.sp)
                                if (playersList.size > 2) {
                                    IconButton(
                                        onClick = { playersList.removeAt(idx) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                if (playersList.size < 8) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newPlayerName,
                                onValueChange = { newPlayerName = it },
                                placeholder = { Text("Add player name", color = TextMuted, fontSize = 12.sp) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = EmeraldBorder,
                                    focusedTextColor = TextLight,
                                    unfocusedTextColor = TextLight
                                ),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    if (newPlayerName.isNotBlank()) {
                                        playersList.add(newPlayerName.trim())
                                        newPlayerName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                            ) {
                                Text("Add", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Scoring Rules Selection (Options requested by user)
                item {
                    Text(text = "Scoring Rule:", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ScoringRule.values().forEach { rule ->
                            val isSelected = selectedRule == rule
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) GoldPrimary.copy(alpha = 0.15f) else DarkSurfaceElevated)
                                    .border(1.dp, if (isSelected) GoldPrimary else EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .clickable { selectedRule = rule }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedRule = rule },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary, unselectedColor = TextMuted)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = rule.title,
                                        color = if (isSelected) GoldLight else TextLight,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = rule.description,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Rounds Generation (Starts from 1 up to 8 or 5 or whatever)
                item {
                    Text(text = "Rounds & Card Progression:", color = GoldLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Max cards selector
                        Text(
                            text = "Max Cards in Round: $maxCardsChoice cards",
                            color = TextLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(5, 8, 10, 13).forEach { count ->
                                val isSelected = maxCardsChoice == count
                                Button(
                                    onClick = { maxCardsChoice = count },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) GoldPrimary else DarkSurfaceElevated,
                                        contentColor = if (isSelected) EmeraldDeep else TextLight
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("$count", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        // Round Pattern
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { roundTypeChoice = 0 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (roundTypeChoice == 0) GoldPrimary else DarkSurfaceElevated,
                                    contentColor = if (roundTypeChoice == 0) EmeraldDeep else TextLight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ladder (1→$maxCardsChoice→1)", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { roundTypeChoice = 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (roundTypeChoice == 1) GoldPrimary else DarkSurfaceElevated,
                                    contentColor = if (roundTypeChoice == 1) EmeraldDeep else TextLight
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Ascending (1→$maxCardsChoice)", fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }

                        val generatedRounds = remember(maxCardsChoice, roundTypeChoice) {
                            if (roundTypeChoice == 0) {
                                (1..maxCardsChoice).toList() + ((maxCardsChoice - 1) downTo 1).toList()
                            } else {
                                (1..maxCardsChoice).toList()
                            }
                        }

                        Text(
                            text = "Sequence (${generatedRounds.size} rounds): ${generatedRounds.joinToString(" → ")}",
                            color = GoldLight,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val customRounds = if (roundTypeChoice == 0) {
                        (1..maxCardsChoice).toList() + ((maxCardsChoice - 1) downTo 1).toList()
                    } else {
                        (1..maxCardsChoice).toList()
                    }
                    onCreate(title, playersList.toList(), selectedMode, selectedRule, customRounds)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_create_scorecard_button")
            ) {
                Text("Start Scorekeeper", color = EmeraldDeep, fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
