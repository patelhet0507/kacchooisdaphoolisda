package com.example.engine

import android.util.Log
import com.example.model.Card
import com.example.model.ChatMessage
import com.example.model.GameMode
import com.example.model.GameRoom
import com.example.model.PlayedCard
import com.example.model.Player
import com.example.model.ScoringRule
import com.example.model.Suit
import com.example.model.VoiceNote
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

enum class JoinRoomStatus {
    SUCCESS,
    ROOM_FULL,
    ROOM_NOT_FOUND,
    ERROR
}

class RoomManager {
    private val localRooms = ConcurrentHashMap<String, MutableStateFlow<GameRoom?>>()

    private val database: FirebaseDatabase? by lazy {
        val rtdbUrl = "https://gen-lang-client-0782479965-default-rtdb.asia-southeast1.firebasedatabase.app"
        try {
            FirebaseDatabase.getInstance(rtdbUrl)
        } catch (e: Exception) {
            Log.w("RoomManager", "FirebaseDatabase regional instance warning: ${e.message}")
            try {
                FirebaseDatabase.getInstance()
            } catch (e2: Exception) {
                Log.w("RoomManager", "FirebaseDatabase default fallback warning: ${e2.message}")
                null
            }
        }
    }

    private val roomsRef: DatabaseReference?
        get() = try {
            database?.getReference("rooms")
        } catch (e: Exception) {
            Log.w("RoomManager", "Error getting rooms reference", e)
            null
        }

    fun getOrCreateLocalFlow(roomId: String): MutableStateFlow<GameRoom?> {
        return localRooms.computeIfAbsent(roomId) {
            MutableStateFlow(null)
        }
    }

    suspend fun createRoom(roomId: String, player: String): Boolean = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val initialRoom = GameRoom(
            roomId = cleanRoomId,
            hostName = player,
            players = listOf(player),
            gameState = "WAITING",
            messages = mapOf(
                "msg_welcome" to ChatMessage(
                    id = "msg_welcome",
                    senderName = "System",
                    text = "Room $cleanRoomId created! Share the 6-digit code with friends to join.",
                    timestamp = System.currentTimeMillis(),
                    isSystem = true
                )
            )
        )

        val flow = getOrCreateLocalFlow(cleanRoomId)
        flow.value = initialRoom

        try {
            val ref = roomsRef?.child(cleanRoomId)
            if (ref != null) {
                try {
                    ref.keepSynced(true)
                } catch (e: Exception) {
                    // non-critical
                }
                try {
                    withTimeoutOrNull(4000L) {
                        ref.setValue(initialRoom).await()
                    }
                } catch (e: Exception) {
                    ref.setValue(initialRoom)
                }
                Log.d("RoomManager", "Room $cleanRoomId created and pushed to Firebase")
            }
        } catch (e: Exception) {
            Log.w("RoomManager", "Firebase room creation sync notice: ${e.message}")
        }
        true
    }

    private suspend fun fetchRoomSnapshot(ref: DatabaseReference): DataSnapshot? {
        val fastSnapshot = try {
            withTimeoutOrNull(3500L) {
                ref.get().await()
            }
        } catch (e: Exception) {
            null
        }

        if (fastSnapshot != null && fastSnapshot.exists()) {
            return fastSnapshot
        }

        return try {
            withTimeoutOrNull(3500L) {
                suspendCancellableCoroutine { cont ->
                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (cont.isActive) {
                                try {
                                    cont.resume(snapshot)
                                } catch (e: Exception) {}
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            if (cont.isActive) {
                                try {
                                    cont.resume(null)
                                } catch (e: Exception) {}
                            }
                        }
                    }
                    try {
                        ref.addListenerForSingleValueEvent(listener)
                    } catch (e: Exception) {
                        if (cont.isActive) {
                            try { cont.resume(null) } catch (re: Exception) {}
                        }
                    }
                    cont.invokeOnCancellation {
                        try {
                            ref.removeEventListener(listener)
                        } catch (e: Exception) {}
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun joinRoom(roomId: String, player: String): JoinRoomStatus = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()

        try {
            val ref = roomsRef?.child(cleanRoomId)
            if (ref != null) {
                try {
                    ref.keepSynced(true)
                } catch (e: Exception) {}
                val snapshot = fetchRoomSnapshot(ref)
                if (snapshot != null && snapshot.exists()) {
                    val room = parseRoomFromSnapshot(snapshot)
                    if (room != null) {
                        if (room.players.size >= 6 && !room.players.contains(player)) {
                            return@withContext JoinRoomStatus.ROOM_FULL
                        }
                        val updatedPlayers = (room.players + player).distinct()
                        val updatedMessages = room.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
                            id = "msg_${System.currentTimeMillis()}",
                            senderName = "System",
                            text = "$player joined the room!",
                            timestamp = System.currentTimeMillis(),
                            isSystem = true
                        ))
                        val updated = room.copy(players = updatedPlayers, messages = updatedMessages)

                        getOrCreateLocalFlow(cleanRoomId).value = updated

                        try {
                            ref.setValue(updated)
                        } catch (e: Exception) {
                            Log.w("RoomManager", "Failed to update joined room: ${e.message}")
                        }
                        return@withContext JoinRoomStatus.SUCCESS
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RoomManager", "Firebase join query notice: ${e.message}")
        }

        val currentLocal = getOrCreateLocalFlow(cleanRoomId).value
        if (currentLocal != null) {
            if (currentLocal.players.size >= 6 && !currentLocal.players.contains(player)) {
                return@withContext JoinRoomStatus.ROOM_FULL
            }
            val updatedPlayers = (currentLocal.players + player).distinct()
            val updatedMessages = currentLocal.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
                id = "msg_${System.currentTimeMillis()}",
                senderName = "System",
                text = "$player joined the room!",
                timestamp = System.currentTimeMillis(),
                isSystem = true
            ))
            val updated = currentLocal.copy(players = updatedPlayers, messages = updatedMessages)
            getOrCreateLocalFlow(cleanRoomId).value = updated

            try {
                roomsRef?.child(cleanRoomId)?.setValue(updated)
            } catch (e: Exception) {
                // non-critical
            }
            return@withContext JoinRoomStatus.SUCCESS
        }

        // If room does not exist yet (e.g. friend gave this code or first time joining code), initialize it seamlessly
        val autoCreatedRoom = GameRoom(
            roomId = cleanRoomId,
            hostName = player,
            players = listOf(player),
            gameState = "WAITING",
            messages = mapOf(
                "msg_welcome" to ChatMessage(
                    id = "msg_welcome",
                    senderName = "System",
                    text = "Room $cleanRoomId ready! Share this 6-digit code with friends to join.",
                    timestamp = System.currentTimeMillis(),
                    isSystem = true
                )
            )
        )
        getOrCreateLocalFlow(cleanRoomId).value = autoCreatedRoom
        try {
            roomsRef?.child(cleanRoomId)?.setValue(autoCreatedRoom)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync auto-created room: ${e.message}")
        }

        JoinRoomStatus.SUCCESS
    }

    suspend fun leaveRoom(roomId: String, player: String) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext
        val updatedPlayers = current.players.filter { it != player }
        val updatedMessages = current.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "System",
            text = "$player left the room.",
            timestamp = System.currentTimeMillis(),
            isSystem = true
        ))
        val updated = current.copy(players = updatedPlayers, messages = updatedMessages)
        getOrCreateLocalFlow(cleanRoomId).value = updated

        try {
            roomsRef?.child(cleanRoomId)?.setValue(updated)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync leaveRoom: ${e.message}")
        }
    }

    suspend fun addBotToRoom(roomId: String, botName: String): Boolean = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext false
        if (current.players.size >= 6) return@withContext false
        val updatedPlayers = (current.players + botName).distinct()
        val updatedMessages = current.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "System",
            text = "$botName (Bot) joined the room.",
            timestamp = System.currentTimeMillis(),
            isSystem = true
        ))
        val updated = current.copy(players = updatedPlayers, messages = updatedMessages)
        getOrCreateLocalFlow(cleanRoomId).value = updated

        try {
            roomsRef?.child(cleanRoomId)?.setValue(updated)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync addBot: ${e.message}")
        }
        true
    }

    suspend fun sendChatMessage(roomId: String, message: ChatMessage) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value
        if (current != null) {
            val updatedMessages = current.messages + (message.id to message)
            getOrCreateLocalFlow(cleanRoomId).value = current.copy(messages = updatedMessages)
        }

        try {
            roomsRef?.child(cleanRoomId)?.child("messages")?.child(message.id)?.setValue(message)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync chat message: ${e.message}")
        }
    }

    suspend fun sendVoiceNote(roomId: String, note: VoiceNote) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value
        if (current != null) {
            val updatedVoice = current.voiceNotes + (note.id to note)
            val updatedMessages = current.messages + (note.id to ChatMessage(
                id = note.id,
                senderName = note.senderName,
                text = "🎤 Voice Note (${(note.durationMs / 1000).coerceAtLeast(1)}s)",
                timestamp = note.timestamp
            ))
            getOrCreateLocalFlow(cleanRoomId).value = current.copy(voiceNotes = updatedVoice, messages = updatedMessages)
        }

        try {
            roomsRef?.child(cleanRoomId)?.child("voiceNotes")?.child(note.id)?.setValue(note)
            val chatMsg = ChatMessage(
                id = note.id,
                senderName = note.senderName,
                text = "🎤 Voice Note (${(note.durationMs / 1000).coerceAtLeast(1)}s)",
                timestamp = note.timestamp
            )
            roomsRef?.child(cleanRoomId)?.child("messages")?.child(note.id)?.setValue(chatMsg)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync voice note: ${e.message}")
        }
    }

    suspend fun setSpeakerStatus(roomId: String, playerName: String, isSpeaking: Boolean) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value
        if (current != null) {
            val updatedSpeakers = current.activeSpeakers + (playerName to isSpeaking)
            getOrCreateLocalFlow(cleanRoomId).value = current.copy(activeSpeakers = updatedSpeakers)
        }

        try {
            roomsRef?.child(cleanRoomId)?.child("activeSpeakers")?.child(playerName)?.setValue(isSpeaking)
        } catch (e: Exception) {
            // non-critical
        }
    }

    // ==========================================
    // MULTIPLAYER MATCH GAME SYNCHRONIZATION
    // ==========================================

    suspend fun startMultiplayerMatch(
        roomId: String,
        gameMode: GameMode = GameMode.QUICK,
        scoringRule: ScoringRule = ScoringRule.STANDARD
    ) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext
        val players = current.players
        if (players.isEmpty()) return@withContext

        val rounds = gameMode.generateRounds(players.size)
        val roundIndex = 0
        val roundCardCount = rounds.getOrElse(0) { 1 }
        val trump = Suit.forRound(0)
        val dealerIdx = 0
        val firstBidderIdx = (dealerIdx + 1) % players.size

        val playersAsModel = players.map { name ->
            Player(
                id = name,
                name = name,
                isBot = name.startsWith("Bot ") || name.contains("(Bot)")
            )
        }
        val dealtHandsMap = KaachuPhoolEngine.dealHands(playersAsModel, roundCardCount)
        val serializedHands = dealtHandsMap.mapValues { (_, cards) ->
            cards.joinToString(",") { it.id }
        }

        val initialScores = players.associateWith { 0 }

        val updated = current.copy(
            gameState = "BIDDING",
            gameMode = gameMode.name,
            scoringRule = scoringRule.name,
            rounds = rounds,
            currentRoundIndex = 0,
            dealerIndex = dealerIdx,
            currentTurnIndex = firstBidderIdx,
            trumpSuit = trump.name,
            leadSuit = null,
            dealtHands = serializedHands,
            bids = emptyMap(),
            tricksWon = players.associateWith { 0 },
            scores = initialScores,
            trickCards = emptyMap(),
            trickOrder = emptyList(),
            lastTrickWinner = null,
            lastWinningCard = null,
            statusMessage = "Bidding Phase: ${players[firstBidderIdx]} bids first"
        )

        getOrCreateLocalFlow(cleanRoomId).value = updated
        try {
            roomsRef?.child(cleanRoomId)?.setValue(updated)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync startMultiplayerMatch: ${e.message}")
        }
    }

    suspend fun submitBid(roomId: String, playerName: String, bid: Int) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext
        val players = current.players
        val updatedBids = current.bids + (playerName to bid)

        val allBidsIn = players.all { updatedBids.containsKey(it) }
        val updated: GameRoom = if (allBidsIn) {
            val firstLeader = (current.dealerIndex + 1) % players.size
            current.copy(
                gameState = "PLAYING",
                bids = updatedBids,
                currentTurnIndex = firstLeader,
                leadSuit = null,
                trickCards = emptyMap(),
                trickOrder = emptyList(),
                statusMessage = "All bids placed! ${players[firstLeader]} leads the first trick"
            )
        } else {
            val currentIdx = players.indexOf(playerName)
            val nextIdx = (if (currentIdx != -1) currentIdx + 1 else current.currentTurnIndex + 1) % players.size
            current.copy(
                bids = updatedBids,
                currentTurnIndex = nextIdx,
                statusMessage = "${players[nextIdx]}'s turn to bid"
            )
        }

        getOrCreateLocalFlow(cleanRoomId).value = updated
        try {
            roomsRef?.child(cleanRoomId)?.setValue(updated)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync submitBid: ${e.message}")
        }
    }

    suspend fun playCard(roomId: String, playerName: String, cardId: String) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext
        val players = current.players
        val card = Card.fromId(cardId) ?: return@withContext

        // Remove card from player's hand in dealtHands
        val currentHandCards = current.dealtHands[playerName]?.split(",")?.filter { it.isNotBlank() && it != cardId } ?: emptyList()
        val updatedDealtHands = current.dealtHands + (playerName to currentHandCards.joinToString(","))

        val updatedTrickCards = current.trickCards + (playerName to cardId)
        val updatedTrickOrder = current.trickOrder + "$playerName:$cardId"
        val leadSuit = current.leadSuit ?: card.suit.name

        val isTrickComplete = updatedTrickCards.size >= players.size

        val updated: GameRoom = if (isTrickComplete) {
            val playedCardsList = updatedTrickOrder.mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2) {
                    val pName = parts[0]
                    val c = Card.fromId(parts[1])
                    if (c != null) {
                        PlayedCard(Player(id = pName, name = pName), c)
                    } else null
                } else null
            }

            val trumpSuit = Suit.values().find { it.name == current.trumpSuit } ?: Suit.SPADES
            val leadSuitObj = Suit.values().find { it.name == leadSuit } ?: card.suit

            val winningPlayedCard = if (playedCardsList.isNotEmpty()) {
                KaachuPhoolEngine.determineTrickWinner(playedCardsList, trumpSuit, leadSuitObj)
            } else {
                PlayedCard(Player(id = playerName, name = playerName), card)
            }

            val winnerName = winningPlayedCard.player.name
            val currentWon = current.tricksWon[winnerName] ?: 0
            val updatedTricksWon = current.tricksWon + (winnerName to (currentWon + 1))

            current.copy(
                gameState = "TRICK_FINISHED",
                dealtHands = updatedDealtHands,
                trickCards = updatedTrickCards,
                trickOrder = updatedTrickOrder,
                leadSuit = leadSuit,
                tricksWon = updatedTricksWon,
                lastTrickWinner = winnerName,
                lastWinningCard = winningPlayedCard.card.id,
                statusMessage = "$winnerName won the trick with ${winningPlayedCard.card}!"
            )
        } else {
            val currentIdx = players.indexOf(playerName)
            val nextIdx = (if (currentIdx != -1) currentIdx + 1 else current.currentTurnIndex + 1) % players.size
            current.copy(
                dealtHands = updatedDealtHands,
                trickCards = updatedTrickCards,
                trickOrder = updatedTrickOrder,
                leadSuit = leadSuit,
                currentTurnIndex = nextIdx,
                statusMessage = "${players[nextIdx]}'s turn"
            )
        }

        getOrCreateLocalFlow(cleanRoomId).value = updated
        try {
            roomsRef?.child(cleanRoomId)?.setValue(updated)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync playCard: ${e.message}")
        }
    }

    suspend fun nextTrickOrRound(roomId: String) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext
        val players = current.players
        val winner = current.lastTrickWinner ?: players.firstOrNull() ?: return@withContext
        val winnerIndex = players.indexOf(winner).coerceAtLeast(0)

        // Check if players have remaining cards
        val hasRemainingCards = current.dealtHands.values.any { it.isNotBlank() }

        val updated: GameRoom = if (hasRemainingCards) {
            current.copy(
                gameState = "PLAYING",
                trickCards = emptyMap(),
                trickOrder = emptyList(),
                leadSuit = null,
                lastTrickWinner = null,
                lastWinningCard = null,
                currentTurnIndex = winnerIndex,
                statusMessage = "$winner leads the next trick"
            )
        } else {
            val scoringRule = try {
                ScoringRule.valueOf(current.scoringRule)
            } catch (e: Exception) {
                ScoringRule.STANDARD
            }

            val updatedScores = current.scores.toMutableMap()
            players.forEach { p ->
                val bid = current.bids[p] ?: 0
                val won = current.tricksWon[p] ?: 0
                val rScore = scoringRule.calculateScore(bid, won)
                updatedScores[p] = (updatedScores[p] ?: 0) + rScore
            }

            val isGameOver = current.currentRoundIndex >= current.rounds.size - 1
            current.copy(
                gameState = if (isGameOver) "GAME_OVER" else "ROUND_FINISHED",
                scores = updatedScores,
                trickCards = emptyMap(),
                trickOrder = emptyList(),
                leadSuit = null,
                statusMessage = if (isGameOver) "Game Completed!" else "Round ${current.currentRoundIndex + 1} completed!"
            )
        }

        getOrCreateLocalFlow(cleanRoomId).value = updated
        try {
            roomsRef?.child(cleanRoomId)?.setValue(updated)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync nextTrickOrRound: ${e.message}")
        }
    }

    suspend fun nextRound(roomId: String) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext
        val nextRoundIdx = current.currentRoundIndex + 1
        if (nextRoundIdx >= current.rounds.size) {
            val updated = current.copy(gameState = "GAME_OVER")
            getOrCreateLocalFlow(cleanRoomId).value = updated
            roomsRef?.child(cleanRoomId)?.setValue(updated)
            return@withContext
        }

        val players = current.players
        val roundCardCount = current.rounds[nextRoundIdx]
        val trump = Suit.forRound(nextRoundIdx)
        val dealerIdx = nextRoundIdx % players.size
        val firstBidderIdx = (dealerIdx + 1) % players.size

        val playersAsModel = players.map { name ->
            Player(id = name, name = name, isBot = name.startsWith("Bot ") || name.contains("(Bot)"))
        }
        val dealtHandsMap = KaachuPhoolEngine.dealHands(playersAsModel, roundCardCount)
        val serializedHands = dealtHandsMap.mapValues { (_, cards) ->
            cards.joinToString(",") { it.id }
        }

        val updated = current.copy(
            gameState = "BIDDING",
            currentRoundIndex = nextRoundIdx,
            dealerIndex = dealerIdx,
            currentTurnIndex = firstBidderIdx,
            trumpSuit = trump.name,
            leadSuit = null,
            dealtHands = serializedHands,
            bids = emptyMap(),
            tricksWon = players.associateWith { 0 },
            trickCards = emptyMap(),
            trickOrder = emptyList(),
            lastTrickWinner = null,
            lastWinningCard = null,
            statusMessage = "Round ${nextRoundIdx + 1}: ${players[firstBidderIdx]} bids first"
        )

        getOrCreateLocalFlow(cleanRoomId).value = updated
        try {
            roomsRef?.child(cleanRoomId)?.setValue(updated)
        } catch (e: Exception) {
            Log.w("RoomManager", "Failed to sync nextRound: ${e.message}")
        }
    }

    fun getRoomUpdates(roomId: String): Flow<GameRoom?> = callbackFlow {
        val cleanRoomId = roomId.trim()
        val localFlow = getOrCreateLocalFlow(cleanRoomId)

        trySend(localFlow.value)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val room = parseRoomFromSnapshot(snapshot)
                    if (room != null) {
                        localFlow.value = room
                        trySend(room)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("RoomManager", "Firebase listener cancelled: ${error.message}")
            }
        }

        val ref = roomsRef?.child(cleanRoomId)
        ref?.addValueEventListener(listener)

        val scope = CoroutineScope(Dispatchers.Default)
        val localJob = scope.launch {
            localFlow.collect { localRoom ->
                if (localRoom != null) {
                    trySend(localRoom)
                }
            }
        }

        awaitClose {
            ref?.removeEventListener(listener)
            localJob.cancel()
        }
    }

    private fun parseRoomFromSnapshot(snapshot: DataSnapshot): GameRoom? {
        try {
            val direct = snapshot.getValue(GameRoom::class.java)
            if (direct != null && direct.roomId.isNotEmpty()) return direct
        } catch (e: Exception) {
            // Fallback to manual parsing
        }

        return try {
            val roomId = snapshot.child("roomId").value?.toString() ?: snapshot.key ?: return null
            val hostName = snapshot.child("hostName").value?.toString() ?: "Host"
            val gameState = snapshot.child("gameState").value?.toString() ?: "WAITING"
            val gameMode = snapshot.child("gameMode").value?.toString() ?: "QUICK"
            val scoringRule = snapshot.child("scoringRule").value?.toString() ?: "STANDARD"
            val currentRoundIndex = (snapshot.child("currentRoundIndex").value as? Number)?.toInt() ?: 0
            val currentTurnIndex = (snapshot.child("currentTurnIndex").value as? Number)?.toInt() ?: 0
            val dealerIndex = (snapshot.child("dealerIndex").value as? Number)?.toInt() ?: 0
            val trumpSuit = snapshot.child("trumpSuit").value?.toString()
            val leadSuit = snapshot.child("leadSuit").value?.toString()
            val lastTrickWinner = snapshot.child("lastTrickWinner").value?.toString()
            val lastWinningCard = snapshot.child("lastWinningCard").value?.toString()
            val statusMessage = snapshot.child("statusMessage").value?.toString() ?: ""

            val playersList = mutableListOf<String>()
            val playersVal = snapshot.child("players").value
            when (playersVal) {
                is List<*> -> playersVal.forEach { (it as? String)?.let { s -> playersList.add(s) } }
                is Map<*, *> -> playersVal.values.forEach { (it as? String)?.let { s -> playersList.add(s) } }
                else -> {
                    snapshot.child("players").children.forEach { child ->
                        (child.value as? String ?: child.getValue(String::class.java))?.let { playersList.add(it) }
                    }
                }
            }

            val roundsList = mutableListOf<Int>()
            val roundsVal = snapshot.child("rounds").value
            when (roundsVal) {
                is List<*> -> roundsVal.forEach { (it as? Number)?.toInt()?.let { r -> roundsList.add(r) } }
                is Map<*, *> -> roundsVal.values.forEach { (it as? Number)?.toInt()?.let { r -> roundsList.add(r) } }
                else -> {
                    snapshot.child("rounds").children.forEach { child ->
                        ((child.value as? Number)?.toInt())?.let { roundsList.add(it) }
                    }
                }
            }

            val dealtHandsMap = mutableMapOf<String, String>()
            snapshot.child("dealtHands").children.forEach { child ->
                val pName = child.key ?: return@forEach
                val h = child.value?.toString() ?: return@forEach
                dealtHandsMap[pName] = h
            }

            val trickOrderList = mutableListOf<String>()
            val trickOrderVal = snapshot.child("trickOrder").value
            when (trickOrderVal) {
                is List<*> -> trickOrderVal.forEach { (it as? String)?.let { s -> trickOrderList.add(s) } }
                is Map<*, *> -> trickOrderVal.values.forEach { (it as? String)?.let { s -> trickOrderList.add(s) } }
                else -> {
                    snapshot.child("trickOrder").children.forEach { child ->
                        child.value?.toString()?.let { trickOrderList.add(it) }
                    }
                }
            }

            val messagesMap = mutableMapOf<String, ChatMessage>()
            snapshot.child("messages").children.forEach { child ->
                val id = child.child("id").value?.toString() ?: child.key ?: ""
                val sender = child.child("senderName").value?.toString() ?: ""
                val text = child.child("text").value?.toString() ?: ""
                val timestamp = (child.child("timestamp").value as? Number)?.toLong() ?: 0L
                val isSystem = (child.child("isSystem").value as? Boolean) ?: (child.child("isSystem").value?.toString() == "true")
                if (id.isNotEmpty() || text.isNotEmpty()) {
                    messagesMap[id.ifEmpty { "msg_${System.currentTimeMillis()}" }] = ChatMessage(
                        id = id,
                        senderName = sender,
                        text = text,
                        timestamp = timestamp,
                        isSystem = isSystem
                    )
                }
            }

            val voiceMap = mutableMapOf<String, VoiceNote>()
            snapshot.child("voiceNotes").children.forEach { child ->
                val id = child.child("id").value?.toString() ?: child.key ?: ""
                val sender = child.child("senderName").value?.toString() ?: ""
                val audio = child.child("audioBase64").value?.toString() ?: ""
                val dur = (child.child("durationMs").value as? Number)?.toLong() ?: 0L
                val timestamp = (child.child("timestamp").value as? Number)?.toLong() ?: 0L
                if (id.isNotEmpty()) {
                    voiceMap[id] = VoiceNote(id, sender, audio, dur, timestamp)
                }
            }

            val trickCardsMap = mutableMapOf<String, String>()
            snapshot.child("trickCards").children.forEach { child ->
                val pId = child.key ?: return@forEach
                val c = child.value?.toString() ?: return@forEach
                trickCardsMap[pId] = c
            }

            val bidsMap = mutableMapOf<String, Int>()
            snapshot.child("bids").children.forEach { child ->
                val key = child.key ?: return@forEach
                val value = (child.value as? Number)?.toInt() ?: 0
                bidsMap[key] = value
            }

            val tricksWonMap = mutableMapOf<String, Int>()
            snapshot.child("tricksWon").children.forEach { child ->
                val key = child.key ?: return@forEach
                val value = (child.value as? Number)?.toInt() ?: 0
                tricksWonMap[key] = value
            }

            val scoresMap = mutableMapOf<String, Int>()
            snapshot.child("scores").children.forEach { child ->
                val key = child.key ?: return@forEach
                val value = (child.value as? Number)?.toInt() ?: 0
                scoresMap[key] = value
            }

            val activeSpeakersMap = mutableMapOf<String, Boolean>()
            snapshot.child("activeSpeakers").children.forEach { child ->
                val pName = child.key ?: return@forEach
                val isSpk = (child.value as? Boolean) ?: (child.value?.toString() == "true")
                activeSpeakersMap[pName] = isSpk
            }

            GameRoom(
                roomId = roomId,
                hostName = hostName,
                players = if (playersList.isEmpty()) listOf(hostName) else playersList,
                gameState = gameState,
                gameMode = gameMode,
                scoringRule = scoringRule,
                rounds = roundsList,
                currentRoundIndex = currentRoundIndex,
                currentTurnIndex = currentTurnIndex,
                dealerIndex = dealerIndex,
                trumpSuit = trumpSuit,
                leadSuit = leadSuit,
                dealtHands = dealtHandsMap,
                bids = bidsMap,
                tricksWon = tricksWonMap,
                scores = scoresMap,
                trickCards = trickCardsMap,
                trickOrder = trickOrderList,
                lastTrickWinner = lastTrickWinner,
                lastWinningCard = lastWinningCard,
                statusMessage = statusMessage,
                messages = messagesMap,
                voiceNotes = voiceMap,
                activeSpeakers = activeSpeakersMap
            )
        } catch (e: Exception) {
            Log.e("RoomManager", "Error in manual DataSnapshot parsing", e)
            null
        }
    }
}
