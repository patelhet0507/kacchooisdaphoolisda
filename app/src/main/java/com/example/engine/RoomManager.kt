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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
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
    companion object {
        private val localRooms = ConcurrentHashMap<String, MutableStateFlow<GameRoom?>>()
        private val roomFlows = ConcurrentHashMap<String, Flow<GameRoom?>>()
        private val activeRoomListeners = ConcurrentHashMap<String, ValueEventListener>()
        private val activePlayersListeners = ConcurrentHashMap<String, ValueEventListener>()

        fun gameRoomToMap(room: GameRoom): Map<String, Any?> = mapOf(
            "roomId" to room.roomId,
            "hostName" to room.hostName,
            "players" to room.players,
            "gameState" to room.gameState,
            "gameMode" to room.gameMode,
            "scoringRule" to room.scoringRule,
            "rounds" to room.rounds,
            "currentRoundIndex" to room.currentRoundIndex,
            "currentTurnIndex" to room.currentTurnIndex,
            "dealerIndex" to room.dealerIndex,
            "trumpSuit" to room.trumpSuit,
            "leadSuit" to room.leadSuit,
            "dealtHands" to room.dealtHands,
            "bids" to room.bids,
            "tricksWon" to room.tricksWon,
            "scores" to room.scores,
            "trickCards" to room.trickCards,
            "trickOrder" to room.trickOrder,
            "playedCardsInRound" to room.playedCardsInRound,
            "lastTrickWinner" to room.lastTrickWinner,
            "lastWinningCard" to room.lastWinningCard,
            "statusMessage" to room.statusMessage,
            "messages" to room.messages.mapValues { (_, msg) ->
                mapOf(
                    "id" to msg.id,
                    "senderName" to msg.senderName,
                    "text" to msg.text,
                    "timestamp" to msg.timestamp,
                    "isSystem" to msg.isSystem
                )
            },
            "voiceNotes" to room.voiceNotes.mapValues { (_, note) ->
                mapOf(
                    "id" to note.id,
                    "senderName" to note.senderName,
                    "audioBase64" to note.audioBase64,
                    "durationMs" to note.durationMs,
                    "timestamp" to note.timestamp
                )
            },
            "activeSpeakers" to room.activeSpeakers,
            "activeEmotes" to room.activeEmotes,
            "kickedPlayers" to room.kickedPlayers,
            "completedAt" to room.completedAt
        )
    }

    private val database: FirebaseDatabase? by lazy {
        val rtdbUrl = "https://gen-lang-client-0782479965-default-rtdb.asia-southeast1.firebasedatabase.app"
        try {
            FirebaseDatabase.getInstance(rtdbUrl)
        } catch (t: Throwable) {
            Log.w("RoomManager", "FirebaseDatabase regional instance warning: ${t.message}")
            try {
                FirebaseDatabase.getInstance()
            } catch (t2: Throwable) {
                Log.w("RoomManager", "FirebaseDatabase default fallback warning: ${t2.message}")
                null
            }
        }
    }

    private val roomsRef: DatabaseReference?
        get() = try {
            database?.getReference("rooms")
        } catch (t: Throwable) {
            Log.w("RoomManager", "Error getting rooms reference", t)
            null
        }

    fun syncRoomToFirebase(roomId: String, room: GameRoom) {
        try {
            val map = gameRoomToMap(room)
            roomsRef?.child(roomId)?.setValue(map)?.addOnFailureListener { e ->
                Log.w("RoomManager", "Firebase sync failed for room $roomId: ${e.message}")
            }
        } catch (t: Throwable) {
            Log.w("RoomManager", "Error syncing room to Firebase: ${t.message}")
        }
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
            syncRoomToFirebase(cleanRoomId, initialRoom)
            Log.d("RoomManager", "Room $cleanRoomId created and pushed to Firebase")
        } catch (t: Throwable) {
            Log.w("RoomManager", "Firebase room creation sync notice: ${t.message}")
        }
        true
    }

    private suspend fun fetchRoomSnapshot(ref: DatabaseReference): DataSnapshot? {
        return try {
            withTimeoutOrNull(4000L) {
                suspendCancellableCoroutine { continuation ->
                    val listener = object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (continuation.isActive) {
                                continuation.resume(snapshot)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                        }
                    }
                    ref.addListenerForSingleValueEvent(listener)
                    continuation.invokeOnCancellation {
                        try {
                            ref.removeEventListener(listener)
                        } catch (e: Exception) {}
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RoomManager", "Error fetching room snapshot: ${e.message}")
            null
        }
    }

    suspend fun joinRoom(roomId: String, player: String): JoinRoomStatus = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val safePlayer = player.trim().replace(Regex("[.#$\\[\\]/]"), "").ifBlank { "Player" }

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
                        if (room.players.size >= 6 && !room.players.contains(safePlayer)) {
                            return@withContext JoinRoomStatus.ROOM_FULL
                        }
                        val isAlreadyInRoom = room.players.contains(safePlayer)
                        val updatedPlayers = if (isAlreadyInRoom) room.players else (room.players + safePlayer)
                        val updatedKicked = room.kickedPlayers.filter { it != safePlayer }
                        val updatedMessages = if (isAlreadyInRoom) {
                            room.messages
                        } else {
                            room.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
                                id = "msg_${System.currentTimeMillis()}",
                                senderName = "System",
                                text = "$safePlayer joined the room!",
                                timestamp = System.currentTimeMillis(),
                                isSystem = true
                            ))
                        }
                        val updated = room.copy(
                            players = updatedPlayers,
                            kickedPlayers = updatedKicked,
                            messages = updatedMessages
                        )

                        getOrCreateLocalFlow(cleanRoomId).value = updated
                        syncRoomToFirebase(cleanRoomId, updated)
                        return@withContext JoinRoomStatus.SUCCESS
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RoomManager", "Firebase join query notice: ${e.message}")
        }

        val currentLocal = getOrCreateLocalFlow(cleanRoomId).value
        if (currentLocal != null) {
            if (currentLocal.players.size >= 6 && !currentLocal.players.contains(safePlayer)) {
                return@withContext JoinRoomStatus.ROOM_FULL
            }
            val isAlreadyInRoom = currentLocal.players.contains(safePlayer)
            val updatedPlayers = if (isAlreadyInRoom) currentLocal.players else (currentLocal.players + safePlayer)
            val updatedKicked = currentLocal.kickedPlayers.filter { it != safePlayer }
            val updatedMessages = if (isAlreadyInRoom) {
                currentLocal.messages
            } else {
                val msgId = "msg_${System.currentTimeMillis()}"
                currentLocal.messages + (msgId to ChatMessage(
                    id = msgId,
                    senderName = "System",
                    text = "$safePlayer joined the room!",
                    timestamp = System.currentTimeMillis(),
                    isSystem = true
                ))
            }
            val updated = currentLocal.copy(
                players = updatedPlayers,
                kickedPlayers = updatedKicked,
                messages = updatedMessages
            )
            getOrCreateLocalFlow(cleanRoomId).value = updated
            syncRoomToFirebase(cleanRoomId, updated)
            return@withContext JoinRoomStatus.SUCCESS
        }

        // Room does not exist on Firebase and does not exist locally -> Auto-bootstrap room so joining any code always succeeds seamlessly!
        val newRoom = GameRoom(
            roomId = cleanRoomId,
            hostName = safePlayer,
            players = listOf(safePlayer),
            gameState = "WAITING",
            messages = mapOf(
                "msg_welcome" to ChatMessage(
                    id = "msg_welcome",
                    senderName = "System",
                    text = "Room $cleanRoomId joined by $safePlayer!",
                    timestamp = System.currentTimeMillis(),
                    isSystem = true
                )
            )
        )
        getOrCreateLocalFlow(cleanRoomId).value = newRoom
        syncRoomToFirebase(cleanRoomId, newRoom)
        return@withContext JoinRoomStatus.SUCCESS
    }

    suspend fun leaveRoom(roomId: String, player: String) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: run {
            val ref = roomsRef?.child(cleanRoomId)
            val snap = if (ref != null) fetchRoomSnapshot(ref) else null
            if (snap != null && snap.exists()) parseRoomFromSnapshot(snap) else null
        } ?: return@withContext

        val isHostLeaving = (current.hostName == player) || (current.players.firstOrNull() == player)

        if (isHostLeaving) {
            val remainingPlayers = current.players.filter { it != player }
            if (remainingPlayers.size == 1 && (current.gameState == "PLAYING" || current.gameState == "BIDDING" || current.gameState == "TRICK_FINISHED" || current.gameState == "ROUND_FINISHED")) {
                val winner = remainingPlayers.first()
                val winningRoom = current.copy(
                    players = remainingPlayers,
                    gameState = "GAME_OVER",
                    statusMessage = "$winner wins! ($player left the game)",
                    completedAt = System.currentTimeMillis()
                )
                getOrCreateLocalFlow(cleanRoomId).value = winningRoom
                syncRoomToFirebase(cleanRoomId, winningRoom)
            } else {
                // Disband the room: Notify remaining players that room is disbanded and delete from Firebase
                val disbandedRoom = current.copy(
                    gameState = "DISBANDED",
                    statusMessage = "Room has been disbanded by host ($player)."
                )
                getOrCreateLocalFlow(cleanRoomId).value = disbandedRoom

                try {
                    // First push DISBANDED state so active listeners get notified immediately
                    roomsRef?.child(cleanRoomId)?.child("gameState")?.setValue("DISBANDED")
                    roomsRef?.child(cleanRoomId)?.child("statusMessage")?.setValue("Room has been disbanded by host ($player).")
                    // Delete the room node from Firebase
                    roomsRef?.child(cleanRoomId)?.removeValue()
                } catch (e: Exception) {
                    Log.w("RoomManager", "Failed to disband room: ${e.message}")
                }
            }
        } else {
            // Guest leaving: remove guest from player list
            val updatedPlayers = current.players.filter { it != player }
            if (updatedPlayers.isEmpty()) {
                getOrCreateLocalFlow(cleanRoomId).value = null
                try {
                    roomsRef?.child(cleanRoomId)?.removeValue()
                } catch (e: Exception) {}
            } else {
                val updatedMessages = current.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
                    id = "msg_${System.currentTimeMillis()}",
                    senderName = "System",
                    text = "$player left the room.",
                    timestamp = System.currentTimeMillis(),
                    isSystem = true
                ))
                
                var updated = current.copy(players = updatedPlayers, messages = updatedMessages)
                
                // NEW: If only 1 player left in an active game, they win
                if (updatedPlayers.size == 1 && (current.gameState == "PLAYING" || current.gameState == "BIDDING" || current.gameState == "TRICK_FINISHED" || current.gameState == "ROUND_FINISHED")) {
                    val winner = updatedPlayers.first()
                    updated = updated.copy(
                        gameState = "GAME_OVER",
                        statusMessage = "$winner wins! ($player left the game)",
                        completedAt = System.currentTimeMillis()
                    )
                }
                
                getOrCreateLocalFlow(cleanRoomId).value = updated
                syncRoomToFirebase(cleanRoomId, updated)
            }
        }
    }

    suspend fun kickPlayerFromRoom(roomId: String, playerToKick: String): Boolean = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: run {
            val ref = roomsRef?.child(cleanRoomId)
            val snap = if (ref != null) fetchRoomSnapshot(ref) else null
            if (snap != null && snap.exists()) parseRoomFromSnapshot(snap) else null
        } ?: return@withContext false

        if (current.hostName == playerToKick) return@withContext false // Cannot kick host

        val updatedPlayers = current.players.filter { it != playerToKick }
        val updatedKicked = (current.kickedPlayers + playerToKick).distinct()
        val updatedMessages = current.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "System",
            text = "$playerToKick was kicked from the room by the host.",
            timestamp = System.currentTimeMillis(),
            isSystem = true
        ))

        var updated = current.copy(
            players = updatedPlayers,
            kickedPlayers = updatedKicked,
            messages = updatedMessages
        )
        
        // NEW: If only 1 player left in an active game, they win
        if (updatedPlayers.size == 1 && (current.gameState == "PLAYING" || current.gameState == "BIDDING" || current.gameState == "TRICK_FINISHED" || current.gameState == "ROUND_FINISHED")) {
            val winner = updatedPlayers.first()
            updated = updated.copy(
                gameState = "GAME_OVER",
                statusMessage = "$winner wins! ($playerToKick was kicked from the room)",
                completedAt = System.currentTimeMillis()
            )
        }
        
        getOrCreateLocalFlow(cleanRoomId).value = updated
        syncRoomToFirebase(cleanRoomId, updated)
        true
    }

    suspend fun cleanExpiredCompletedRooms() = withContext(Dispatchers.IO) {
        try {
            val ref = roomsRef ?: return@withContext
            val snap = fetchRoomSnapshot(ref) ?: return@withContext
            val cutoff = System.currentTimeMillis() - 5 * 60 * 1000L // 5 minutes
            for (child in snap.children) {
                val gState = child.child("gameState").value?.toString()
                val compAt = (child.child("completedAt").value as? Number)?.toLong()
                    ?: child.child("completedAt").value?.toString()?.toLongOrNull() ?: 0L
                if (gState == "GAME_OVER" && compAt > 0L && compAt <= cutoff) {
                    val rId = child.key ?: continue
                    ref.child(rId).removeValue()
                    localRooms.remove(rId)
                    Log.d("RoomManager", "Auto-deleted completed room after 5 minutes: $rId")
                }
            }
        } catch (e: Exception) {
            Log.w("RoomManager", "cleanExpiredCompletedRooms notice: ${e.message}")
        }
    }

    suspend fun addBotToRoom(roomId: String, botName: String): Boolean = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value
        val resolvedCurrent = current ?: run {
            val ref = roomsRef?.child(cleanRoomId)
            val snap = if (ref != null) fetchRoomSnapshot(ref) else null
            val parsed = if (snap != null && snap.exists()) parseRoomFromSnapshot(snap) else null
            parsed ?: GameRoom(
                roomId = cleanRoomId,
                hostName = botName,
                players = listOf(botName),
                gameState = "WAITING"
            )
        }
        if (resolvedCurrent.players.size >= 6) return@withContext false
        val updatedPlayers = (resolvedCurrent.players + botName).distinct()
        val updatedMessages = resolvedCurrent.messages + ("msg_${System.currentTimeMillis()}" to ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = "System",
            text = "$botName (Bot) joined the room.",
            timestamp = System.currentTimeMillis(),
            isSystem = true
        ))
        val updated = resolvedCurrent.copy(players = updatedPlayers, messages = updatedMessages)
        getOrCreateLocalFlow(cleanRoomId).value = updated
        syncRoomToFirebase(cleanRoomId, updated)
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
        val safePlayer = playerName.trim().replace(Regex("[.#$\\[\\]/]"), "").ifBlank { "Player" }
        val current = getOrCreateLocalFlow(cleanRoomId).value
        if (current != null) {
            val updatedSpeakers = current.activeSpeakers + (safePlayer to isSpeaking)
            getOrCreateLocalFlow(cleanRoomId).value = current.copy(activeSpeakers = updatedSpeakers)
        }

        try {
            roomsRef?.child(cleanRoomId)?.child("activeSpeakers")?.child(safePlayer)?.setValue(isSpeaking)
        } catch (e: Exception) {
            // non-critical
        }
    }

    suspend fun sendEmote(roomId: String, playerName: String, emoteEmoji: String) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val safePlayer = playerName.trim().replace(Regex("[.#$\\[\\]/]"), "").ifBlank { "Player" }
        val current = getOrCreateLocalFlow(cleanRoomId).value
        if (current != null) {
            val updatedEmotes = current.activeEmotes + (safePlayer to emoteEmoji)
            getOrCreateLocalFlow(cleanRoomId).value = current.copy(activeEmotes = updatedEmotes)
        }

        try {
            roomsRef?.child(cleanRoomId)?.child("activeEmotes")?.child(safePlayer)?.setValue(emoteEmoji)
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
            playedCardsInRound = emptyList(),
            lastTrickWinner = null,
            lastWinningCard = null,
            statusMessage = "Bidding Phase: ${players[firstBidderIdx]} bids first"
        )

        getOrCreateLocalFlow(cleanRoomId).value = updated
        syncRoomToFirebase(cleanRoomId, updated)
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
        syncRoomToFirebase(cleanRoomId, updated)
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
        val updatedPlayedInRound = current.playedCardsInRound + cardId
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
                playedCardsInRound = updatedPlayedInRound,
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
                playedCardsInRound = updatedPlayedInRound,
                leadSuit = leadSuit,
                currentTurnIndex = nextIdx,
                statusMessage = "${players[nextIdx]}'s turn"
            )
        }

        getOrCreateLocalFlow(cleanRoomId).value = updated
        syncRoomToFirebase(cleanRoomId, updated)
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
                completedAt = if (isGameOver) System.currentTimeMillis() else current.completedAt,
                statusMessage = if (isGameOver) "Game Completed!" else "Round ${current.currentRoundIndex + 1} completed!"
            )
        }

        getOrCreateLocalFlow(cleanRoomId).value = updated
        syncRoomToFirebase(cleanRoomId, updated)
    }

    suspend fun nextRound(roomId: String) = withContext(Dispatchers.IO) {
        val cleanRoomId = roomId.trim()
        val current = getOrCreateLocalFlow(cleanRoomId).value ?: return@withContext
        val nextRoundIdx = current.currentRoundIndex + 1
        if (nextRoundIdx >= current.rounds.size) {
            val updated = current.copy(gameState = "GAME_OVER", completedAt = System.currentTimeMillis())
            getOrCreateLocalFlow(cleanRoomId).value = updated
            syncRoomToFirebase(cleanRoomId, updated)
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
            playedCardsInRound = emptyList(),
            lastTrickWinner = null,
            lastWinningCard = null,
            statusMessage = "Round ${nextRoundIdx + 1}: ${players[firstBidderIdx]} bids first"
        )

        getOrCreateLocalFlow(cleanRoomId).value = updated
        syncRoomToFirebase(cleanRoomId, updated)
    }

    fun getRoomUpdates(roomId: String): Flow<GameRoom?> {
        val cleanRoomId = roomId.trim()
        return roomFlows.computeIfAbsent(cleanRoomId) {
            callbackFlow {
                val localFlow = getOrCreateLocalFlow(cleanRoomId)
                trySend(localFlow.value)

                val listener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            if (snapshot.exists()) {
                                val room = parseRoomFromSnapshot(snapshot)
                                if (room != null) {
                                    // Check if game completed more than 5 minutes ago -> auto delete
                                    if (room.gameState == "GAME_OVER" && room.completedAt > 0L &&
                                        System.currentTimeMillis() - room.completedAt >= 5 * 60 * 1000L
                                    ) {
                                        try {
                                            roomsRef?.child(cleanRoomId)?.removeValue()
                                        } catch (e: Throwable) {}
                                        localFlow.value = null
                                        trySend(null)
                                        return
                                    }
                                    localFlow.value = room
                                    trySend(room)
                                }
                            } else {
                                // Snapshot not found on server yet:
                                // If local room exists and is active, sync it up to Firebase so other players see it!
                                val local = localFlow.value
                                if (local != null && local.gameState != "DISBANDED") {
                                    syncRoomToFirebase(cleanRoomId, local)
                                }
                            }
                        } catch (t: Throwable) {
                            Log.w("RoomManager", "Error processing onDataChange", t)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.w("RoomManager", "Firebase listener cancelled: ${error.message}")
                    }
                }

                val ref = roomsRef?.child(cleanRoomId)
                if (ref != null) {
                    try {
                        // Ensure any existing listener for this path is removed before adding a new one
                        val existing = activeRoomListeners.remove(cleanRoomId)
                        if (existing != null) {
                            ref.removeEventListener(existing)
                        }
                        
                        activeRoomListeners[cleanRoomId] = listener
                        ref.addValueEventListener(listener)
                        Log.d("RoomManager", "Attached listener for room: $cleanRoomId")
                    } catch (t: Throwable) {
                        Log.w("RoomManager", "Error registering Firebase listener", t)
                    }
                }

                val playersRef = ref?.child("players")
                val playersListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val playersList = mutableListOf<String>()
                            when (val v = snapshot.value) {
                                is List<*> -> v.forEach { item -> if (item != null) playersList.add(item.toString()) }
                                is Map<*, *> -> v.values.forEach { item -> if (item != null) playersList.add(item.toString()) }
                                else -> snapshot.children.forEach { c -> c.value?.toString()?.let { playersList.add(it) } }
                            }
                            Log.d("RoomManager", "Players node monitor updated for $cleanRoomId: count=${playersList.size}, players=$playersList")
                            val current = localFlow.value
                            if (current != null) {
                                val updated = current.copy(players = playersList)
                                localFlow.value = updated
                                trySend(updated)
                            } else {
                                val fallbackRoom = GameRoom(
                                    roomId = cleanRoomId,
                                    hostName = playersList.firstOrNull() ?: "Host",
                                    players = playersList,
                                    gameState = "WAITING"
                                )
                                localFlow.value = fallbackRoom
                                trySend(fallbackRoom)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.w("RoomManager", "Players listener cancelled: ${error.message}")
                    }
                }
                if (playersRef != null) {
                    try {
                        val existingPlayerListener = activePlayersListeners.remove(cleanRoomId)
                        if (existingPlayerListener != null) {
                            playersRef.removeEventListener(existingPlayerListener)
                        }
                        activePlayersListeners[cleanRoomId] = playersListener
                        playersRef.addValueEventListener(playersListener)
                        Log.d("RoomManager", "Attached dedicated players node listener for room: $cleanRoomId")
                    } catch (t: Throwable) {
                        Log.w("RoomManager", "Error registering players listener", t)
                    }
                }

                val scope = CoroutineScope(Dispatchers.Default)
                val localJob = scope.launch {
                    localFlow.collect { localRoom ->
                        trySend(localRoom)
                    }
                }

                awaitClose {
                    if (ref != null) {
                        try {
                            val active = activeRoomListeners[cleanRoomId]
                            if (active == listener) {
                                ref.removeEventListener(listener)
                                activeRoomListeners.remove(cleanRoomId)
                                Log.d("RoomManager", "Removed listener for room: $cleanRoomId")
                            }
                            val activePlayers = activePlayersListeners[cleanRoomId]
                            if (activePlayers != null && playersRef != null) {
                                playersRef.removeEventListener(activePlayers)
                                activePlayersListeners.remove(cleanRoomId)
                                Log.d("RoomManager", "Removed dedicated players node listener for room: $cleanRoomId")
                            }
                            // Also clear persistence sync when leaving room observation
                            ref.keepSynced(false)
                        } catch (t: Throwable) {
                            Log.w("RoomManager", "Error removing Firebase listener", t)
                        }
                    }
                    localJob.cancel()
                    // Remove from flow cache to ensure fresh start on next observation
                    roomFlows.remove(cleanRoomId)
                }
            }.shareIn(
                scope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 3000),
                replay = 1
            )
        }
    }

    private fun parseRoomFromSnapshot(snapshot: DataSnapshot): GameRoom? {
        return try {
            val roomId = snapshot.child("roomId").value?.toString() ?: snapshot.key ?: return null
            val hostName = snapshot.child("hostName").value?.toString() ?: "Host"
            val gameState = snapshot.child("gameState").value?.toString() ?: "WAITING"
            val gameMode = snapshot.child("gameMode").value?.toString() ?: "QUICK"
            val scoringRule = snapshot.child("scoringRule").value?.toString() ?: "STANDARD"
            
            val currentRoundIndex = runCatching { 
                (snapshot.child("currentRoundIndex").value as? Number)?.toInt() ?: snapshot.child("currentRoundIndex").value?.toString()?.toIntOrNull() ?: 0
            }.getOrDefault(0)
            
            val currentTurnIndex = runCatching { 
                (snapshot.child("currentTurnIndex").value as? Number)?.toInt() ?: snapshot.child("currentTurnIndex").value?.toString()?.toIntOrNull() ?: 0
            }.getOrDefault(0)
            
            val dealerIndex = runCatching { 
                (snapshot.child("dealerIndex").value as? Number)?.toInt() ?: snapshot.child("dealerIndex").value?.toString()?.toIntOrNull() ?: 0
            }.getOrDefault(0)

            val trumpSuit = snapshot.child("trumpSuit").value?.toString()
            val leadSuit = snapshot.child("leadSuit").value?.toString()
            val lastTrickWinner = snapshot.child("lastTrickWinner").value?.toString()
            val lastWinningCard = snapshot.child("lastWinningCard").value?.toString()
            val statusMessage = snapshot.child("statusMessage").value?.toString() ?: ""

            val playersList = mutableListOf<String>()
            val playersVal = snapshot.child("players").value
            when (playersVal) {
                is List<*> -> playersVal.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) playersList.add(s)
                }
                is Map<*, *> -> playersVal.values.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) playersList.add(s)
                }
                else -> {
                    snapshot.child("players").children.forEach { child ->
                        val p = child.value?.toString()
                        if (!p.isNullOrBlank()) playersList.add(p)
                    }
                }
            }

            val roundsList = mutableListOf<Int>()
            val roundsVal = snapshot.child("rounds").value
            when (roundsVal) {
                is List<*> -> roundsVal.forEach { item ->
                    val num = (item as? Number)?.toInt() ?: item?.toString()?.toIntOrNull()
                    if (num != null) roundsList.add(num)
                }
                is Map<*, *> -> roundsVal.values.forEach { item ->
                    val num = (item as? Number)?.toInt() ?: item?.toString()?.toIntOrNull()
                    if (num != null) roundsList.add(num)
                }
                else -> {
                    snapshot.child("rounds").children.forEach { child ->
                        val num = (child.value as? Number)?.toInt() ?: child.value?.toString()?.toIntOrNull()
                        if (num != null) roundsList.add(num)
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
                is List<*> -> trickOrderVal.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) trickOrderList.add(s)
                }
                is Map<*, *> -> trickOrderVal.values.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) trickOrderList.add(s)
                }
                else -> {
                    snapshot.child("trickOrder").children.forEach { child ->
                        val s = child.value?.toString()
                        if (!s.isNullOrBlank()) trickOrderList.add(s)
                    }
                }
            }

            val playedInRoundList = mutableListOf<String>()
            val playedInRoundVal = snapshot.child("playedCardsInRound").value
            when (playedInRoundVal) {
                is List<*> -> playedInRoundVal.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) playedInRoundList.add(s)
                }
                is Map<*, *> -> playedInRoundVal.values.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) playedInRoundList.add(s)
                }
                else -> {
                    snapshot.child("playedCardsInRound").children.forEach { child ->
                        val s = child.value?.toString()
                        if (!s.isNullOrBlank()) playedInRoundList.add(s)
                    }
                }
            }

            val messagesMap = mutableMapOf<String, ChatMessage>()
            snapshot.child("messages").children.forEach { child ->
                val id = child.child("id").value?.toString() ?: child.key ?: ""
                val sender = child.child("senderName").value?.toString() ?: ""
                val text = child.child("text").value?.toString() ?: ""
                val timestamp = (child.child("timestamp").value as? Number)?.toLong()
                    ?: child.child("timestamp").value?.toString()?.toLongOrNull() ?: System.currentTimeMillis()
                val isSysVal = child.child("isSystem").value
                val isSystem = (isSysVal as? Boolean) ?: (isSysVal?.toString() == "true")
                if (id.isNotBlank() || text.isNotBlank()) {
                    val safeId = id.ifBlank { "msg_${timestamp}_${child.key ?: ""}" }
                    messagesMap[safeId] = ChatMessage(
                        id = safeId,
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
                val dur = (child.child("durationMs").value as? Number)?.toLong()
                    ?: child.child("durationMs").value?.toString()?.toLongOrNull() ?: 0L
                val timestamp = (child.child("timestamp").value as? Number)?.toLong()
                    ?: child.child("timestamp").value?.toString()?.toLongOrNull() ?: System.currentTimeMillis()
                if (id.isNotBlank() && audio.isNotBlank()) {
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
                val value = (child.value as? Number)?.toInt()
                    ?: child.value?.toString()?.toIntOrNull() ?: 0
                bidsMap[key] = value
            }

            val tricksWonMap = mutableMapOf<String, Int>()
            snapshot.child("tricksWon").children.forEach { child ->
                val key = child.key ?: return@forEach
                val value = (child.value as? Number)?.toInt()
                    ?: child.value?.toString()?.toIntOrNull() ?: 0
                tricksWonMap[key] = value
            }

            val scoresMap = mutableMapOf<String, Int>()
            snapshot.child("scores").children.forEach { child ->
                val key = child.key ?: return@forEach
                val value = (child.value as? Number)?.toInt()
                    ?: child.value?.toString()?.toIntOrNull() ?: 0
                scoresMap[key] = value
            }

            val activeSpeakersMap = mutableMapOf<String, Boolean>()
            snapshot.child("activeSpeakers").children.forEach { child ->
                val pName = child.key ?: return@forEach
                val isSpk = (child.value as? Boolean) ?: (child.value?.toString() == "true")
                activeSpeakersMap[pName] = isSpk
            }

            val activeEmotesMap = mutableMapOf<String, String>()
            snapshot.child("activeEmotes").children.forEach { child ->
                val pName = child.key ?: return@forEach
                val emote = child.value?.toString() ?: return@forEach
                if (emote.isNotBlank()) {
                    activeEmotesMap[pName] = emote
                }
            }

            val kickedPlayersList = mutableListOf<String>()
            val kickedVal = snapshot.child("kickedPlayers").value
            when (kickedVal) {
                is List<*> -> kickedVal.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) kickedPlayersList.add(s)
                }
                is Map<*, *> -> kickedVal.values.forEach { item ->
                    val s = item?.toString()
                    if (!s.isNullOrBlank()) kickedPlayersList.add(s)
                }
                else -> {
                    snapshot.child("kickedPlayers").children.forEach { child ->
                        val p = child.value?.toString()
                        if (!p.isNullOrBlank()) kickedPlayersList.add(p)
                    }
                }
            }

            val completedAt = (snapshot.child("completedAt").value as? Number)?.toLong()
                ?: snapshot.child("completedAt").value?.toString()?.toLongOrNull() ?: 0L

            GameRoom(
                roomId = roomId,
                hostName = hostName,
                players = if (playersList.isEmpty()) listOf(hostName) else playersList.distinct(),
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
                playedCardsInRound = playedInRoundList,
                lastTrickWinner = lastTrickWinner,
                lastWinningCard = lastWinningCard,
                statusMessage = statusMessage,
                messages = messagesMap,
                voiceNotes = voiceMap,
                activeSpeakers = activeSpeakersMap,
                activeEmotes = activeEmotesMap,
                kickedPlayers = kickedPlayersList,
                completedAt = completedAt
            )
        } catch (e: Exception) {
            Log.e("RoomManager", "Error in manual DataSnapshot parsing: ${e.message}", e)
            null
        }
    }
}
