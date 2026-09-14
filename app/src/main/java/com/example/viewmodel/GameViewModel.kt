package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.KaachuPhoolEngine
import com.example.engine.RoomManager
import com.example.engine.SoundEffectsManager
import com.example.model.Card
import com.example.model.BotDifficulty
import com.example.model.Emote
import com.example.model.GameMode
import com.example.model.GamePhase
import com.example.model.GameRoom
import com.example.model.PlayedCard
import com.example.model.Player
import com.example.model.PlayerRoundState
import com.example.model.ScoringRule
import com.example.model.Suit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TableState(
    val playedCards: List<PlayedCard> = emptyList(),
    val trumpSuit: Suit = Suit.SPADES,
    val leadSuit: Suit? = null,
    val trickWinner: Player? = null,
    val isTrickFinished: Boolean = false
)

data class PlayerSeatState(
    val player: Player,
    val bid: Int? = null,
    val tricksWon: Int = 0,
    val isDealer: Boolean = false,
    val isCurrentTurn: Boolean = false,
    val totalScore: Int = 0,
    val activeEmote: String? = null
)

data class GameUiState(
    val gameMode: GameMode = GameMode.QUICK,
    val scoringRule: ScoringRule = ScoringRule.STANDARD,
    val rounds: List<Int> = emptyList(),
    val currentRoundIndex: Int = 0,
    val currentRoundCardCount: Int = 0,
    val currentTrump: Suit = Suit.SPADES,
    val nextTrump: Suit? = null,
    val dealerIndex: Int = 0,
    val currentTurnIndex: Int = 0,
    val phase: GamePhase = GamePhase.BIDDING,
    val players: List<Player> = emptyList(),
    val playerStates: List<PlayerRoundState> = emptyList(),
    val userHand: List<Card> = emptyList(),
    val currentTrick: List<PlayedCard> = emptyList(),
    val leadSuit: Suit? = null,
    val lastTrickWinner: Player? = null,
    val isUserBiddingTurn: Boolean = false,
    val userForbiddenBid: Int? = null,
    val isProcessingBot: Boolean = false,
    val statusMessage: String = "",
    val isMultiplayer: Boolean = false,
    val isRoomDisbanded: Boolean = false,
    val localPlayerName: String = "You",
    val roomCode: String? = null,
    val botDifficulty: BotDifficulty = BotDifficulty.MEDIUM,
    val activeEmotes: Map<String, String> = emptyMap()
)

class GameViewModel : ViewModel() {
    private val roomManager = RoomManager()
    private var currentRoomId: String? = null
    private var localPlayerName: String = "You"
    private var isHost: Boolean = false
    private var roomJob: Job? = null
    private var botTurnJob: Job? = null
    private var soundEffectsManager: SoundEffectsManager? = null

    // Granular StateFlows for real-time synchronization
    private val _tableState = MutableStateFlow(TableState())
    val tableState: StateFlow<TableState> = _tableState.asStateFlow()

    private val _playerSeats = MutableStateFlow<List<PlayerSeatState>>(emptyList())
    val playerSeats: StateFlow<List<PlayerSeatState>> = _playerSeats.asStateFlow()

    private val _userHand = MutableStateFlow<List<Card>>(emptyList())
    val userHand: StateFlow<List<Card>> = _userHand.asStateFlow()

    fun setSoundEffectsManager(manager: SoundEffectsManager) {
        this.soundEffectsManager = manager
    }

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // ==========================================
    // MULTIPLAYER MATCH INTEGRATION
    // ==========================================

    fun startMultiplayerGame(
        roomId: String,
        localPlayerName: String,
        gameMode: GameMode = GameMode.QUICK,
        scoringRule: ScoringRule = ScoringRule.STANDARD,
        isHost: Boolean = false
    ) {
        this.currentRoomId = roomId.trim()
        val sanitizedName = localPlayerName.trim().replace(Regex("[.#$\\[\\]/]"), "").ifBlank { "You" }
        this.localPlayerName = sanitizedName
        this.isHost = isHost

        _uiState.update {
            it.copy(
                isMultiplayer = true,
                localPlayerName = this.localPlayerName,
                roomCode = this.currentRoomId,
                gameMode = gameMode,
                scoringRule = scoringRule
            )
        }

        if (isHost) {
            viewModelScope.launch {
                roomManager.startMultiplayerMatch(roomId, gameMode, scoringRule)
            }
        }

        observeMultiplayerRoom(this.currentRoomId!!)
    }

    fun exitGame() {
        roomJob?.cancel()
        roomJob = null
        val code = currentRoomId
        val player = localPlayerName
        if (!code.isNullOrBlank() && player.isNotBlank()) {
            viewModelScope.launch {
                try {
                    roomManager.leaveRoom(code, player)
                } catch (e: Exception) {
                    Log.w("GameViewModel", "Failed to leave room cleanly: ${e.message}")
                }
            }
        }
        currentRoomId = null
        isHost = false
        
        // Reset granular states
        _tableState.value = TableState()
        _playerSeats.value = emptyList()
        _userHand.value = emptyList()

        _uiState.update {
            it.copy(
                isMultiplayer = false,
                isRoomDisbanded = false,
                roomCode = null,
                phase = GamePhase.GAME_OVER,
                players = emptyList(),
                playerStates = emptyList()
            )
        }
    }

    private fun observeMultiplayerRoom(roomId: String) {
        roomJob?.cancel()
        roomJob = viewModelScope.launch {
            roomManager.getRoomUpdates(roomId).collect { room ->
                if (room == null || room.gameState == "DISBANDED") {
                    if (_uiState.value.isMultiplayer) {
                        _uiState.update { it.copy(isRoomDisbanded = true) }
                    }
                } else {
                    val myName = localPlayerName
                    if (room.kickedPlayers.contains(myName)) {
                        _uiState.update { it.copy(isRoomDisbanded = true) }
                    } else {
                        applyMultiplayerRoomState(room)
                    }
                }
            }
        }
    }

    private fun applyMultiplayerRoomState(room: GameRoom) {
        val playerNames = room.players
        if (playerNames.isEmpty()) return

        val avatarColors = listOf(0xFFF59E0B, 0xFF3B82F6, 0xFF10B981, 0xFFEC4899, 0xFF8B5CF6, 0xFFF97316)
        val avatarEmojis = listOf("👑", "🦊", "🦁", "🐼", "🐯", "🦅")

        val playersList = playerNames.mapIndexed { idx, name ->
            val isBot = name.startsWith("Bot ") || name.contains("(Bot)")
            val isMe = name == localPlayerName
            Player(
                id = name,
                name = name,
                isBot = isBot,
                avatarEmoji = if (isMe) "👑" else avatarEmojis[idx % avatarEmojis.size],
                colorHex = avatarColors[idx % avatarColors.size]
            )
        }

        val myCardsString = room.dealtHands[localPlayerName] ?: ""
        val myHand = myCardsString.split(",")
            .mapNotNull { Card.fromId(it) }
            .sortedWith(
                compareBy<Card> { card ->
                    when (card.suit) {
                        Suit.SPADES -> 0
                        Suit.DIAMONDS -> 1
                        Suit.CLUBS -> 2
                        Suit.HEARTS -> 3
                    }
                }.thenByDescending { it.rank.value }
            )

        val playerStatesList = playersList.map { p ->
            val pCardsStr = room.dealtHands[p.name] ?: ""
            val pCards = if (p.name == localPlayerName) {
                myHand
            } else {
                pCardsStr.split(",").filter { it.isNotBlank() }.map { Card(Suit.SPADES, com.example.model.Rank.TWO) }
            }
            PlayerRoundState(
                player = p,
                cards = pCards,
                bid = room.bids[p.name],
                tricksWon = room.tricksWon[p.name] ?: 0,
                totalScore = room.scores[p.name] ?: 0
            )
        }

        val trickCardsList = room.trickOrder.mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val pName = parts[0]
                val card = Card.fromId(parts[1])
                val player = playersList.find { it.name == pName } ?: Player(id = pName, name = pName)
                if (card != null) PlayedCard(player, card) else null
            } else null
        }

        val phase = when (room.gameState) {
            "WAITING", "BIDDING" -> GamePhase.BIDDING
            "PLAYING" -> GamePhase.PLAYING
            "TRICK_FINISHED" -> GamePhase.TRICK_FINISHED
            "ROUND_FINISHED" -> GamePhase.ROUND_FINISHED
            "GAME_OVER" -> GamePhase.GAME_OVER
            else -> GamePhase.BIDDING
        }

        val trump = Suit.values().find { it.name == room.trumpSuit } ?: Suit.SPADES
        val nextTrump = if (room.currentRoundIndex < room.rounds.size - 1) Suit.forRound(room.currentRoundIndex + 1) else null
        val leadSuit = Suit.values().find { it.name == room.leadSuit }
        val lastWinner = room.lastTrickWinner?.let { wName ->
            playersList.find { it.name == wName } ?: Player(id = wName, name = wName)
        }

        val isMyBiddingTurn = (phase == GamePhase.BIDDING &&
                playerNames.getOrNull(room.currentTurnIndex) == localPlayerName &&
                room.bids[localPlayerName] == null)

        val isDealer = room.currentTurnIndex == room.dealerIndex
        val otherBidsSum = room.bids.filterKeys { it != localPlayerName }.values.sum()
        val roundCardCount = room.rounds.getOrElse(room.currentRoundIndex) { 1 }
        val forbidden = if (isDealer && isMyBiddingTurn) {
            KaachuPhoolEngine.getForbiddenBidForDealer(otherBidsSum, roundCardCount)
        } else null

        val currentTurnPlayerName = playerNames.getOrNull(room.currentTurnIndex)
        val isBotTurn = currentTurnPlayerName != null &&
                (currentTurnPlayerName.startsWith("Bot ") || currentTurnPlayerName.contains("(Bot)"))

        // Update Granular Table State
        _tableState.update {
            it.copy(
                playedCards = trickCardsList,
                trumpSuit = trump,
                leadSuit = leadSuit,
                trickWinner = lastWinner,
                isTrickFinished = phase == GamePhase.TRICK_FINISHED
            )
        }

        // Update Granular Player Seats State
        val newPlayerSeats = playersList.map { p ->
            PlayerSeatState(
                player = p,
                bid = room.bids[p.name],
                tricksWon = room.tricksWon[p.name] ?: 0,
                isDealer = room.dealerIndex == playerNames.indexOf(p.name),
                isCurrentTurn = room.currentTurnIndex == playerNames.indexOf(p.name),
                totalScore = room.scores[p.name] ?: 0,
                activeEmote = room.activeEmotes[p.name]
            )
        }
        _playerSeats.value = newPlayerSeats

        // Update Granular User Hand State
        _userHand.value = myHand

        _uiState.update {
            it.copy(
                isMultiplayer = true,
                localPlayerName = localPlayerName,
                roomCode = room.roomId,
                gameMode = try { GameMode.valueOf(room.gameMode) } catch (e: Exception) { GameMode.QUICK },
                scoringRule = try { ScoringRule.valueOf(room.scoringRule) } catch (e: Exception) { ScoringRule.STANDARD },
                rounds = room.rounds,
                currentRoundIndex = room.currentRoundIndex,
                currentRoundCardCount = roundCardCount,
                currentTrump = trump,
                nextTrump = nextTrump,
                dealerIndex = room.dealerIndex,
                currentTurnIndex = room.currentTurnIndex,
                phase = phase,
                players = playersList,
                playerStates = playerStatesList,
                userHand = myHand,
                currentTrick = trickCardsList,
                leadSuit = leadSuit,
                lastTrickWinner = lastWinner,
                isUserBiddingTurn = isMyBiddingTurn,
                userForbiddenBid = forbidden,
                isProcessingBot = isBotTurn,
                statusMessage = room.statusMessage.ifBlank {
                    when (phase) {
                        GamePhase.BIDDING -> "${currentTurnPlayerName ?: "Player"} is bidding..."
                        GamePhase.PLAYING -> "${currentTurnPlayerName ?: "Player"}'s turn to play"
                        GamePhase.TRICK_FINISHED -> "${lastWinner?.name ?: "Player"} won the trick!"
                        GamePhase.ROUND_FINISHED -> "Round Completed!"
                        GamePhase.GAME_OVER -> "Game Completed!"
                    }
                },
                activeEmotes = room.activeEmotes
            )
        }

        // Host coordinates AI bot turns automatically
        val isFirstHumanInRoom = room.players.firstOrNull { !it.startsWith("Bot ") && !it.contains("(Bot)") } == localPlayerName
        if ((isHost || isFirstHumanInRoom) && isBotTurn && currentTurnPlayerName != null) {
            triggerBotMultiplayerTurn(room, currentTurnPlayerName)
        }

        // In multiplayer, if trick is finished, auto-advance after 2 seconds if host
        if ((isHost || isFirstHumanInRoom) && phase == GamePhase.TRICK_FINISHED) {
            viewModelScope.launch {
                delay(2000L)
                val currentRoom = roomManager.getOrCreateLocalFlow(room.roomId).value
                if (currentRoom?.gameState == "TRICK_FINISHED") {
                    roomManager.nextTrickOrRound(room.roomId)
                }
            }
        }
    }

    private fun triggerBotMultiplayerTurn(room: GameRoom, botName: String) {
        botTurnJob?.cancel()
        botTurnJob = viewModelScope.launch {
            delay(1000L)
            val currentFlowRoom = roomManager.getOrCreateLocalFlow(room.roomId).value ?: return@launch
            if (currentFlowRoom.players.getOrNull(currentFlowRoom.currentTurnIndex) != botName) return@launch

            val isDealer = currentFlowRoom.currentTurnIndex == currentFlowRoom.dealerIndex
            val otherBidsSum = currentFlowRoom.bids.filterKeys { it != botName }.values.sum()
            val roundCardCount = currentFlowRoom.rounds.getOrElse(currentFlowRoom.currentRoundIndex) { 1 }
            val botCards = (currentFlowRoom.dealtHands[botName] ?: "").split(",")
                .mapNotNull { Card.fromId(it) }

            if (currentFlowRoom.gameState == "BIDDING" && currentFlowRoom.bids[botName] == null) {
                val trump = Suit.values().find { it.name == currentFlowRoom.trumpSuit } ?: Suit.SPADES
                val botBid = KaachuPhoolEngine.calculateBotBid(
                    hand = botCards,
                    trumpSuit = trump,
                    totalCards = roundCardCount,
                    isDealer = isDealer,
                    otherBidsSum = otherBidsSum
                )
                roomManager.submitBid(room.roomId, botName, botBid)
            } else if (currentFlowRoom.gameState == "PLAYING" && !currentFlowRoom.trickCards.containsKey(botName)) {
                val trump = Suit.values().find { it.name == currentFlowRoom.trumpSuit } ?: Suit.SPADES
                val leadSuit = Suit.values().find { it.name == currentFlowRoom.leadSuit }
                val currentPlayedCards = currentFlowRoom.trickOrder.mapNotNull { entry ->
                    val parts = entry.split(":")
                    if (parts.size == 2) {
                        val p = Player(id = parts[0], name = parts[0])
                        val c = Card.fromId(parts[1])
                        if (c != null) PlayedCard(p, c) else null
                    } else null
                }
                val tricksWon = currentFlowRoom.tricksWon[botName] ?: 0
                val targetBid = currentFlowRoom.bids[botName] ?: 0

                val chosenCard = KaachuPhoolEngine.chooseBotCard(
                    hand = botCards,
                    leadSuit = leadSuit,
                    trumpSuit = trump,
                    currentTrick = currentPlayedCards,
                    tricksWon = tricksWon,
                    targetBid = targetBid
                )
                roomManager.playCard(room.roomId, botName, chosenCard.id)
            }
        }
    }

    // ==========================================
    // LOCAL SINGLE PLAYER MATCH
    // ==========================================

    fun startNewGame(
        gameMode: GameMode = GameMode.QUICK,
        scoringRule: ScoringRule = ScoringRule.STANDARD,
        userName: String = "You",
        botCount: Int = 3,
        botDifficulty: BotDifficulty = BotDifficulty.MEDIUM
    ) {
        roomJob?.cancel()
        botTurnJob?.cancel()
        currentRoomId = null
        isHost = false
        localPlayerName = userName.trim().replace(Regex("[.#$\\[\\]/]"), "").ifBlank { "You" }

        val user = Player(
            id = "user",
            name = localPlayerName,
            isBot = false,
            avatarEmoji = "👑",
            colorHex = 0xFFF59E0B
        )

        val bots = Player.DEFAULT_BOTS.take(botCount)
        val allPlayers = listOf(user) + bots
        val roundConfigs = gameMode.generateRounds(allPlayers.size)

        val initialStates = allPlayers.map { player ->
            PlayerRoundState(player = player, totalScore = 0)
        }

        _uiState.update {
            it.copy(
                isMultiplayer = false,
                localPlayerName = localPlayerName,
                roomCode = null,
                gameMode = gameMode,
                scoringRule = scoringRule,
                botDifficulty = botDifficulty,
                rounds = roundConfigs,
                currentRoundIndex = 0,
                players = allPlayers,
                playerStates = initialStates,
                dealerIndex = 0
            )
        }

        startRound(0)
    }

    private fun startRound(roundIndex: Int) {
        val state = _uiState.value
        val roundCardCount = state.rounds[roundIndex]
        val trump = Suit.forRound(roundIndex)
        val nextTrump = if (roundIndex < state.rounds.size - 1) Suit.forRound(roundIndex + 1) else null
        val dealerIdx = roundIndex % state.players.size
        val firstBidderIdx = (dealerIdx + 1) % state.players.size

        val dealtHands = KaachuPhoolEngine.dealHands(state.players, roundCardCount)

        val newPlayerStates = state.playerStates.map { pState ->
            pState.copy(
                cards = dealtHands[pState.player.id] ?: emptyList(),
                bid = null,
                tricksWon = 0,
                roundScore = 0
            )
        }

        val userHand = dealtHands["user"] ?: emptyList()

        soundEffectsManager?.playCardDeal()
        soundEffectsManager?.playTrumpAnnounce()

        _uiState.update {
            it.copy(
                currentRoundIndex = roundIndex,
                currentRoundCardCount = roundCardCount,
                currentTrump = trump,
                nextTrump = nextTrump,
                dealerIndex = dealerIdx,
                currentTurnIndex = firstBidderIdx,
                phase = GamePhase.BIDDING,
                playerStates = newPlayerStates,
                userHand = userHand,
                currentTrick = emptyList(),
                leadSuit = null,
                lastTrickWinner = null,
                statusMessage = "Bidding Phase: ${state.players[firstBidderIdx].name} bids first"
            )
        }

        checkBiddingTurn()
    }

    private fun checkBiddingTurn() {
        val state = _uiState.value
        val currentIdx = state.currentTurnIndex
        if (currentIdx !in state.players.indices) return
        val currentPlayer = state.players[currentIdx]

        if (!currentPlayer.isBot) {
            val isDealer = currentIdx == state.dealerIndex
            val otherBidsSum = state.playerStates.filter { it.player.id != currentPlayer.id }
                .mapNotNull { it.bid }.sum()
            val forbidden = if (isDealer) {
                KaachuPhoolEngine.getForbiddenBidForDealer(otherBidsSum, state.currentRoundCardCount)
            } else null

            _uiState.update {
                it.copy(
                    isUserBiddingTurn = true,
                    userForbiddenBid = forbidden,
                    statusMessage = if (isDealer && forbidden != null) {
                        "Your turn to bid (Dealer Hook: cannot bid $forbidden)"
                    } else {
                        "Your turn to bid!"
                    }
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isUserBiddingTurn = false,
                    isProcessingBot = true,
                    statusMessage = "${currentPlayer.name} is deciding their bid..."
                )
            }

            viewModelScope.launch {
                delay(700)
                executeBotBid(currentIdx)
            }
        }
    }

    fun submitUserBid(bid: Int) {
        val state = _uiState.value
        if (state.isMultiplayer && currentRoomId != null) {
            viewModelScope.launch {
                roomManager.submitBid(currentRoomId!!, localPlayerName, bid)
            }
            return
        }

        val updatedStates = state.playerStates.map {
            if (it.player.id == "user") it.copy(bid = bid) else it
        }

        _uiState.update {
            it.copy(
                playerStates = updatedStates,
                isUserBiddingTurn = false
            )
        }

        advanceBidding(state.currentTurnIndex)
    }

    private fun executeBotBid(botIndex: Int) {
        val state = _uiState.value
        if (botIndex !in state.players.indices) return
        val botPlayer = state.players[botIndex]
        val botState = state.playerStates.find { it.player.id == botPlayer.id } ?: return
        val isDealer = botIndex == state.dealerIndex

        val otherBidsSum = state.playerStates.filter { it.player.id != botPlayer.id }
            .mapNotNull { it.bid }.sum()

        val calculatedBid = KaachuPhoolEngine.calculateBotBid(
            hand = botState.cards,
            trumpSuit = state.currentTrump,
            totalCards = state.currentRoundCardCount,
            isDealer = isDealer,
            otherBidsSum = otherBidsSum,
            difficulty = state.botDifficulty
        )

        val updatedStates = state.playerStates.map {
            if (it.player.id == botPlayer.id) it.copy(bid = calculatedBid) else it
        }

        _uiState.update {
            it.copy(
                playerStates = updatedStates,
                isProcessingBot = false
            )
        }

        advanceBidding(botIndex)
    }

    private fun advanceBidding(completedIndex: Int) {
        val state = _uiState.value
        val allBidsIn = state.playerStates.all { it.bid != null }

        if (allBidsIn) {
            val firstLeader = (state.dealerIndex + 1) % state.players.size
            _uiState.update {
                it.copy(
                    phase = GamePhase.PLAYING,
                    currentTurnIndex = firstLeader,
                    statusMessage = "All bids placed! ${state.players[firstLeader].name} leads the first trick"
                )
            }
            checkPlayingTurn()
        } else {
            val nextIdx = (completedIndex + 1) % state.players.size
            _uiState.update {
                it.copy(currentTurnIndex = nextIdx)
            }
            checkBiddingTurn()
        }
    }

    private fun checkPlayingTurn() {
        val state = _uiState.value
        if (state.phase != GamePhase.PLAYING) return

        val currentIdx = state.currentTurnIndex
        if (currentIdx !in state.players.indices) return
        val currentPlayer = state.players[currentIdx]

        if (!currentPlayer.isBot) {
            _uiState.update {
                it.copy(
                    isProcessingBot = false,
                    statusMessage = if (state.leadSuit != null) {
                        "Your turn! Follow ${state.leadSuit.displayName} (${state.leadSuit.symbol}) if you have it"
                    } else {
                        "Your turn to lead!"
                    }
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    isProcessingBot = true,
                    statusMessage = "${currentPlayer.name}'s turn..."
                )
            }

            viewModelScope.launch {
                delay(800)
                executeBotCardPlay(currentIdx)
            }
        }
    }

    fun playUserCard(card: Card) {
        val state = _uiState.value
        if (state.isMultiplayer && currentRoomId != null) {
            viewModelScope.launch {
                roomManager.playCard(currentRoomId!!, localPlayerName, card.id)
            }
            return
        }

        if (state.phase != GamePhase.PLAYING || state.currentTurnIndex !in state.players.indices || state.players[state.currentTurnIndex].id != "user") return

        val playable = KaachuPhoolEngine.getPlayableCards(state.userHand, state.leadSuit)
        if (!playable.contains(card)) return

        val playedCard = PlayedCard(player = state.players[state.currentTurnIndex], card = card)
        val updatedHand = state.userHand.filter { it.id != card.id }
        val updatedUserCards = state.playerStates.find { it.player.id == "user" }?.cards?.filter { it.id != card.id } ?: emptyList()

        val updatedStates = state.playerStates.map {
            if (it.player.id == "user") it.copy(cards = updatedUserCards) else it
        }

        val updatedTrick = state.currentTrick + playedCard
        val leadSuit = state.leadSuit ?: card.suit

        soundEffectsManager?.playCardPlay()

        _uiState.update {
            it.copy(
                userHand = updatedHand,
                playerStates = updatedStates,
                currentTrick = updatedTrick,
                leadSuit = leadSuit
            )
        }

        advancePlaying(state.currentTurnIndex, updatedTrick, leadSuit)
    }

    private fun executeBotCardPlay(botIndex: Int) {
        val state = _uiState.value
        if (botIndex !in state.players.indices) return
        val botPlayer = state.players[botIndex]
        val botState = state.playerStates.find { it.player.id == botPlayer.id } ?: return

        if (botState.cards.isEmpty()) {
            _uiState.update { it.copy(isProcessingBot = false) }
            return
        }

        val chosenCard = KaachuPhoolEngine.chooseBotCard(
            hand = botState.cards,
            leadSuit = state.leadSuit,
            trumpSuit = state.currentTrump,
            currentTrick = state.currentTrick,
            tricksWon = botState.tricksWon,
            targetBid = botState.bid ?: 0,
            difficulty = state.botDifficulty
        )

        val playedCard = PlayedCard(player = botPlayer, card = chosenCard)
        val updatedCards = botState.cards.filter { it.id != chosenCard.id }

        val updatedStates = state.playerStates.map {
            if (it.player.id == botPlayer.id) it.copy(cards = updatedCards) else it
        }

        val updatedTrick = state.currentTrick + playedCard
        val leadSuit = state.leadSuit ?: chosenCard.suit

        soundEffectsManager?.playCardPlay()

        _uiState.update {
            it.copy(
                playerStates = updatedStates,
                currentTrick = updatedTrick,
                leadSuit = leadSuit,
                isProcessingBot = false
            )
        }

        advancePlaying(botIndex, updatedTrick, leadSuit)
    }

    private fun advancePlaying(
        completedIndex: Int,
        currentTrick: List<PlayedCard>,
        leadSuit: Suit
    ) {
        val state = _uiState.value

        if (currentTrick.size == state.players.size) {
            val winningPlayedCard = KaachuPhoolEngine.determineTrickWinner(
                cards = currentTrick,
                trumpSuit = state.currentTrump,
                leadSuit = leadSuit
            )

            val winner = winningPlayedCard.player
            val updatedStates = state.playerStates.map {
                if (it.player.id == winner.id) it.copy(tricksWon = it.tricksWon + 1) else it
            }

            soundEffectsManager?.playTrickWin()

            _uiState.update {
                it.copy(
                    phase = GamePhase.TRICK_FINISHED,
                    lastTrickWinner = winner,
                    playerStates = updatedStates,
                    statusMessage = "${winner.name} wins the trick with ${winningPlayedCard.card}!"
                )
            }
        } else {
            val nextIdx = (completedIndex + 1) % state.players.size
            _uiState.update {
                it.copy(currentTurnIndex = nextIdx)
            }
            checkPlayingTurn()
        }
    }

    fun continueNextTrick() {
        val state = _uiState.value
        if (state.isMultiplayer && currentRoomId != null) {
            viewModelScope.launch {
                roomManager.nextTrickOrRound(currentRoomId!!)
            }
            return
        }

        val winner = state.lastTrickWinner ?: return
        val winnerIndex = state.players.indexOfFirst { it.id == winner.id }

        val remainingCards = state.playerStates.firstOrNull()?.cards?.size ?: 0

        if (remainingCards == 0) {
            val updatedStates = state.playerStates.map { pState ->
                val roundScore = state.scoringRule.calculateScore(
                    bid = pState.bid ?: 0,
                    tricksWon = pState.tricksWon
                )
                pState.copy(
                    roundScore = roundScore,
                    totalScore = pState.totalScore + roundScore
                )
            }

            val isGameOver = state.currentRoundIndex >= state.rounds.size - 1

            soundEffectsManager?.playRoundWin()

            _uiState.update {
                it.copy(
                    phase = if (isGameOver) GamePhase.GAME_OVER else GamePhase.ROUND_FINISHED,
                    playerStates = updatedStates,
                    currentTrick = emptyList(),
                    leadSuit = null,
                    lastTrickWinner = null,
                    statusMessage = if (isGameOver) "Game Completed!" else "Round Completed!"
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    phase = GamePhase.PLAYING,
                    currentTrick = emptyList(),
                    leadSuit = null,
                    lastTrickWinner = null,
                    currentTurnIndex = if (winnerIndex != -1) winnerIndex else 0,
                    statusMessage = "${winner.name} leads the next trick"
                )
            }
            checkPlayingTurn()
        }
    }

    fun continueNextRound() {
        val state = _uiState.value
        if (state.isMultiplayer && currentRoomId != null) {
            viewModelScope.launch {
                roomManager.nextRound(currentRoomId!!)
            }
            return
        }

        val nextRound = state.currentRoundIndex + 1
        if (nextRound < state.rounds.size) {
            startRound(nextRound)
        } else {
            _uiState.update { it.copy(phase = GamePhase.GAME_OVER) }
        }
    }

    fun restartCurrentGame() {
        val state = _uiState.value
        if (state.isMultiplayer && currentRoomId != null) {
            viewModelScope.launch {
                roomManager.startMultiplayerMatch(currentRoomId!!, state.gameMode, state.scoringRule)
            }
            return
        }

        startNewGame(
            gameMode = state.gameMode,
            scoringRule = state.scoringRule,
            userName = state.players.firstOrNull { !it.isBot }?.name ?: "You",
            botCount = state.players.filter { it.isBot }.size
        )
    }

    // ==========================================
    // EMOTE SYSTEM INTEGRATION
    // ==========================================

    fun sendEmote(emote: Emote) {
        soundEffectsManager?.playEmoteSound()
        val sender = _uiState.value.localPlayerName
        val isMulti = _uiState.value.isMultiplayer
        val roomId = currentRoomId

        _uiState.update { state ->
            val updated = state.activeEmotes + (sender to emote.emoji)
            state.copy(activeEmotes = updated)
        }

        // Clear local emote after 3 seconds
        viewModelScope.launch {
            delay(3000)
            _uiState.update { state ->
                if (state.activeEmotes[sender] == emote.emoji) {
                    state.copy(activeEmotes = state.activeEmotes - sender)
                } else state
            }
        }

        if (isMulti && !roomId.isNullOrBlank()) {
            viewModelScope.launch {
                roomManager.sendEmote(roomId, sender, emote.emoji)
            }
        } else {
            // Trigger bot reaction in single player match
            triggerBotReaction(emote)
        }
    }

    private fun triggerBotReaction(userEmote: Emote) {
        val bots = _uiState.value.players.filter { it.isBot }
        if (bots.isEmpty()) return

        viewModelScope.launch {
            delay((600..1200).random().toLong())
            val respondingBot = bots.random()
            val botEmote = when (userEmote) {
                Emote.THUMBS_UP -> listOf(Emote.THUMBS_UP, Emote.CLAPPING, Emote.HEART).random()
                Emote.LAUGHING -> listOf(Emote.LAUGHING, Emote.FIRE, Emote.SURPRISED).random()
                Emote.THINKING -> listOf(Emote.THINKING, Emote.SURPRISED).random()
                Emote.CLAPPING -> listOf(Emote.CLAPPING, Emote.HEART, Emote.FIRE).random()
                Emote.ANGRY -> listOf(Emote.LAUGHING, Emote.SURPRISED, Emote.THINKING).random()
                Emote.HEART -> listOf(Emote.HEART, Emote.THUMBS_UP, Emote.CLAPPING).random()
                Emote.SURPRISED -> listOf(Emote.SURPRISED, Emote.LAUGHING).random()
                Emote.FIRE -> listOf(Emote.FIRE, Emote.CLAPPING).random()
            }

            soundEffectsManager?.playEmoteSound()
            _uiState.update { state ->
                state.copy(activeEmotes = state.activeEmotes + (respondingBot.name to botEmote.emoji))
            }

            delay(3000)
            _uiState.update { state ->
                if (state.activeEmotes[respondingBot.name] == botEmote.emoji) {
                    state.copy(activeEmotes = state.activeEmotes - respondingBot.name)
                } else state
            }
        }
    }
}
