package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.KaachuPhoolDatabase
import com.example.data.ScorecardGameEntity
import com.example.data.ScorecardRepository
import com.example.data.ScorecardRoundEntity
import com.example.engine.KaachuPhoolEngine
import com.example.model.GameMode
import com.example.model.ScoringRule
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScorecardActiveUiState(
    val activeGame: ScorecardGameEntity? = null,
    val rounds: List<ScorecardRoundEntity> = emptyList(),
    val currentRoundIndex: Int = 0,
    val draftBids: List<Int?> = emptyList(),
    val draftTricks: List<Int?> = emptyList(),
    val scoringRule: ScoringRule = ScoringRule.STANDARD,
    val dealerIndex: Int = 0,
    val forbiddenBid: Int? = null,
    val tricksSumError: String? = null,
    val isRoundComplete: Boolean = false
)

class ScorecardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScorecardRepository
    private var gameObservationJob: Job? = null

    init {
        val db = KaachuPhoolDatabase.getDatabase(application)
        repository = ScorecardRepository(db.scorecardDao())
        
        // Clean any expired completed games on startup and periodically every 30s
        viewModelScope.launch {
            while (true) {
                repository.cleanOldCompletedGames()
                delay(30000)
            }
        }
    }

    val allSavedGames: StateFlow<List<ScorecardGameEntity>> = repository.allGames
        .map { games ->
            val now = System.currentTimeMillis()
            games.filterNot { it.isCompleted && it.completedAt > 0 && (now - it.completedAt) > 5 * 60 * 1000L }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeState = MutableStateFlow(ScorecardActiveUiState())
    val activeState: StateFlow<ScorecardActiveUiState> = _activeState.asStateFlow()

    fun createNewMatch(
        title: String,
        playerNames: List<String>,
        gameMode: GameMode,
        scoringRule: ScoringRule,
        customRounds: List<Int>? = null,
        onCreated: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val cleanedNames = playerNames.filter { it.isNotBlank() }
            val validNames = if (cleanedNames.size >= 2) cleanedNames else listOf("Player 1", "Player 2", "Player 3", "Player 4")
            val gameId = repository.createNewGame(
                title = title,
                playerNames = validNames,
                gameMode = gameMode,
                scoringRule = scoringRule,
                customRounds = customRounds
            )
            loadGame(gameId)
            onCreated(gameId)
        }
    }

    fun renamePlayer(playerIndex: Int, newName: String) {
        val game = _activeState.value.activeGame ?: return
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return
        
        val names = game.getPlayerNames().toMutableList()
        if (playerIndex in names.indices) {
            names[playerIndex] = trimmed
            val updatedGame = game.copy(playerNamesRaw = names.joinToString(","))
            _activeState.update { it.copy(activeGame = updatedGame) }
            viewModelScope.launch {
                repository.updatePlayerName(game.id, playerIndex, trimmed)
            }
        }
    }

    fun loadGame(gameId: Long) {
        gameObservationJob?.cancel()
        gameObservationJob = viewModelScope.launch {
            combine(
                repository.getGame(gameId),
                repository.getRounds(gameId)
            ) { game, roundsList ->
                Pair(game, roundsList)
            }.collect { (game, roundsList) ->
                if (game == null) {
                    _activeState.update { ScorecardActiveUiState() }
                    return@collect
                }

                val rule = runCatching { ScoringRule.valueOf(game.scoringRuleName) }.getOrDefault(ScoringRule.STANDARD)
                val currIndex = game.currentRoundIndex.coerceIn(0, maxOf(0, roundsList.size - 1))
                val currentRound = roundsList.getOrNull(currIndex)

                val playersCount = game.getPlayerNames().size
                val existingBids = currentRound?.getBids()?.take(playersCount) ?: emptyList()
                val existingTricks = currentRound?.getTricksWon()?.take(playersCount) ?: emptyList()

                val currentDraftBids = _activeState.value.draftBids
                val currentDraftTricks = _activeState.value.draftTricks

                val draftB = if (currentDraftBids.size == playersCount && _activeState.value.activeGame?.id == game.id && _activeState.value.currentRoundIndex == currIndex) {
                    currentDraftBids
                } else if (existingBids.size == playersCount) {
                    existingBids
                } else {
                    List(playersCount) { null }
                }

                val draftT = if (currentDraftTricks.size == playersCount && _activeState.value.activeGame?.id == game.id && _activeState.value.currentRoundIndex == currIndex) {
                    currentDraftTricks
                } else if (existingTricks.size == playersCount) {
                    existingTricks
                } else {
                    List(playersCount) { null }
                }

                val dealerIdx = currentRound?.dealerIndex ?: (currIndex % playersCount)
                val otherBids = draftB.mapIndexedNotNull { idx, b -> if (idx != dealerIdx) b else null }.sum()
                val forbidden = currentRound?.let {
                    KaachuPhoolEngine.getForbiddenBidForDealer(otherBids, it.cardCount)
                }

                _activeState.update {
                    it.copy(
                        activeGame = game,
                        rounds = roundsList,
                        currentRoundIndex = currIndex,
                        draftBids = draftB,
                        draftTricks = draftT,
                        scoringRule = rule,
                        dealerIndex = dealerIdx,
                        forbiddenBid = forbidden,
                        tricksSumError = null,
                        isRoundComplete = game.isCompleted
                    )
                }
            }
        }
    }

    fun setPlayerBid(playerIndex: Int, bid: Int?) {
        val state = _activeState.value
        val game = state.activeGame ?: return
        val currentRound = state.rounds.getOrNull(state.currentRoundIndex) ?: return
        val totalPlayers = game.getPlayerNames().size

        val updatedBids = state.draftBids.toMutableList()
        while (updatedBids.size < totalPlayers) updatedBids.add(null)
        if (playerIndex in updatedBids.indices) {
            updatedBids[playerIndex] = bid
        }

        val otherBids = updatedBids.mapIndexedNotNull { idx, b -> if (idx != state.dealerIndex) b else null }.sum()
        val forbidden = KaachuPhoolEngine.getForbiddenBidForDealer(otherBids, currentRound.cardCount)

        _activeState.update {
            it.copy(
                draftBids = updatedBids,
                forbiddenBid = forbidden
            )
        }
    }

    fun setPlayerTricks(playerIndex: Int, tricks: Int?) {
        val state = _activeState.value
        val game = state.activeGame ?: return
        val currentRound = state.rounds.getOrNull(state.currentRoundIndex) ?: return
        val totalPlayers = game.getPlayerNames().size

        val updatedTricks = state.draftTricks.toMutableList()
        while (updatedTricks.size < totalPlayers) updatedTricks.add(null)
        if (playerIndex in updatedTricks.indices) {
            updatedTricks[playerIndex] = tricks
        }

        // Validate tricks sum if all entered
        val allEntered = updatedTricks.all { it != null }
        val sumError = if (allEntered) {
            val sum = updatedTricks.filterNotNull().sum()
            if (sum != currentRound.cardCount) {
                "Tricks sum ($sum) must equal total cards dealt (${currentRound.cardCount})!"
            } else null
        } else null

        _activeState.update {
            it.copy(
                draftTricks = updatedTricks,
                tricksSumError = sumError
            )
        }
    }

    fun saveAndNextRound() {
        val state = _activeState.value
        val game = state.activeGame ?: return
        val currentRound = state.rounds.getOrNull(state.currentRoundIndex) ?: return

        // Hook Rule check for dealer
        val dealerBid = state.draftBids.getOrNull(state.dealerIndex)
        if (dealerBid != null && state.forbiddenBid != null && dealerBid == state.forbiddenBid) {
            _activeState.update {
                it.copy(tricksSumError = "Dealer cannot bid ${state.forbiddenBid} due to the Hook Rule!")
            }
            return
        }

        viewModelScope.launch {
            repository.updateRound(
                round = currentRound,
                bids = state.draftBids,
                tricksWon = state.draftTricks,
                scoringRule = state.scoringRule,
                markGameNextRound = true
            )

            val nextIndex = state.currentRoundIndex + 1
            val isCompleted = nextIndex >= state.rounds.size
            val completedTime = if (isCompleted) System.currentTimeMillis() else 0L

            val updatedGame = game.copy(
                currentRoundIndex = nextIndex.coerceAtMost(state.rounds.size - 1),
                isCompleted = isCompleted,
                completedAt = completedTime
            )

            repository.updateGameProgress(updatedGame)

            if (!isCompleted) {
                val nextRound = state.rounds[nextIndex]
                val playersCount = game.getPlayerNames().size
                val nextDealerIdx = nextRound.dealerIndex
                val nextBids = nextRound.getBids().take(playersCount).ifEmpty { List(playersCount) { null } }
                val nextTricks = nextRound.getTricksWon().take(playersCount).ifEmpty { List(playersCount) { null } }

                _activeState.update {
                    it.copy(
                        activeGame = updatedGame,
                        currentRoundIndex = nextIndex,
                        draftBids = nextBids,
                        draftTricks = nextTricks,
                        dealerIndex = nextDealerIdx,
                        tricksSumError = null,
                        isRoundComplete = false
                    )
                }
            } else {
                _activeState.update {
                    it.copy(
                        activeGame = updatedGame,
                        isRoundComplete = true
                    )
                }
            }
        }
    }

    fun deleteGame(gameId: Long) {
        viewModelScope.launch {
            repository.deleteGame(gameId)
        }
    }
}
