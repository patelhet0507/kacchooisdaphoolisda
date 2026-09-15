package com.example.model

data class ChatMessage(
    val id: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L,
    val isSystem: Boolean = false
)

data class VoiceNote(
    val id: String = "",
    val senderName: String = "",
    val audioBase64: String = "",
    val durationMs: Long = 0L,
    val timestamp: Long = 0L
)

data class GameRoom(
    val roomId: String = "",
    val hostName: String = "Host",
    val players: List<String> = emptyList(),
    val gameState: String = "WAITING", // WAITING, BIDDING, PLAYING, TRICK_FINISHED, ROUND_FINISHED, GAME_OVER
    val gameMode: String = "QUICK",
    val scoringRule: String = "STANDARD",
    val rounds: List<Int> = emptyList(),
    val currentRoundIndex: Int = 0,
    val currentTurnIndex: Int = 0,
    val dealerIndex: Int = 0,
    val trumpSuit: String? = null,
    val leadSuit: String? = null,
    val dealtHands: Map<String, String> = emptyMap(), // playerName -> "A_SPADES,K_HEARTS,..."
    val bids: Map<String, Int> = emptyMap(), // playerName -> bid
    val tricksWon: Map<String, Int> = emptyMap(), // playerName -> tricks count
    val scores: Map<String, Int> = emptyMap(), // playerName -> cumulative score
    val trickCards: Map<String, String> = emptyMap(), // playerName -> cardId in current trick
    val trickOrder: List<String> = emptyList(), // "playerName:cardId" in play order
    val playedCardsInRound: List<String> = emptyList(), // "cardId" list for AI tracking
    val lastTrickWinner: String? = null,
    val lastWinningCard: String? = null,
    val statusMessage: String = "",
    val messages: Map<String, ChatMessage> = emptyMap(),
    val voiceNotes: Map<String, VoiceNote> = emptyMap(),
    val activeSpeakers: Map<String, Boolean> = emptyMap(),
    val activeEmotes: Map<String, String> = emptyMap(),
    val kickedPlayers: List<String> = emptyList(),
    val completedAt: Long = 0L
)
