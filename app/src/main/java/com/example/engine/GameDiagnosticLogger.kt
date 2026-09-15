package com.example.engine

import android.util.Log
import com.example.model.Card
import com.example.model.Player
import com.example.model.PlayerRoundState
import com.example.model.Rank
import com.example.model.Suit

/**
 * Diagnostic logging and verification engine for tracking card deck state,
 * shuffling, dealing distribution, and hand integrity before trick execution.
 */
object GameDiagnosticLogger {

    private const val TAG = "GameLoopDiagnostics"
    private const val MAX_LOG_HISTORY = 200

    private val _logHistory = mutableListOf<String>()

    /**
     * Retrieve a snapshot of the recent diagnostic logs for testing or debugging.
     */
    @Synchronized
    fun getLogHistory(): List<String> = _logHistory.toList()

    @Synchronized
    fun clearHistory() {
        _logHistory.clear()
    }

    private fun emitLog(level: Int, message: String) {
        synchronized(_logHistory) {
            if (_logHistory.size >= MAX_LOG_HISTORY) {
                _logHistory.removeAt(0)
            }
            _logHistory.add(message)
        }

        try {
            when (level) {
                Log.ERROR -> Log.e(TAG, message)
                Log.WARN -> Log.w(TAG, message)
                Log.DEBUG -> Log.d(TAG, message)
                else -> Log.i(TAG, message)
            }
        } catch (_: Throwable) {
            // Fallback for non-Android JVM environments
            println("[$TAG] $message")
        }
    }

    data class DeckDiagnosticReport(
        val totalCards: Int,
        val is52CardDeck: Boolean,
        val suitCounts: Map<Suit, Int>,
        val rankCounts: Map<Rank, Int>,
        val hasDuplicateIds: Boolean,
        val issues: List<String>
    )

    data class HandVerificationResult(
        val isValid: Boolean,
        val roundIndex: Int,
        val expectedCardsPerPlayer: Int,
        val playerHandCounts: Map<String, Int>,
        val userHandValid: Boolean,
        val botHandsValid: Boolean,
        val duplicateCardIds: List<String>,
        val issues: List<String>,
        val summary: String
    )

    /**
     * Diagnostic step 1: Tracks the card deck state before shuffling.
     * Verifies that the standard 52-card deck is complete, contains 13 cards per suit,
     * 4 cards per rank, and zero duplicate IDs.
     */
    fun logDeckState(deck: List<Card>): DeckDiagnosticReport {
        val issues = mutableListOf<String>()
        val totalCards = deck.size
        if (totalCards != 52) {
            issues.add("Deck size is $totalCards (expected 52)")
        }

        val suitCounts = Suit.values().associateWith { suit -> deck.count { it.suit == suit } }
        for ((suit, count) in suitCounts) {
            if (count != 13) {
                issues.add("Suit ${suit.name} has $count cards (expected 13)")
            }
        }

        val rankCounts = Rank.values().associateWith { rank -> deck.count { it.rank == rank } }
        for ((rank, count) in rankCounts) {
            if (count != 4) {
                issues.add("Rank ${rank.name} has $count cards (expected 4)")
            }
        }

        val uniqueIds = deck.map { it.id }.toSet()
        val hasDuplicateIds = uniqueIds.size != totalCards
        if (hasDuplicateIds) {
            issues.add("Deck contains duplicate IDs (${uniqueIds.size} unique out of $totalCards)")
        }

        val is52CardDeck = issues.isEmpty()

        val logMessage = buildString {
            appendLine("[DECK STATE] Total: $totalCards | 52-Card Deck: $is52CardDeck | Duplicates: $hasDuplicateIds")
            appendLine("             Suits: ${suitCounts.entries.joinToString { "${it.key.symbol}=${it.value}" }}")
            if (issues.isNotEmpty()) {
                appendLine("             Issues Detected: ${issues.joinToString("; ")}")
            }
        }

        emitLog(if (is52CardDeck) Log.INFO else Log.WARN, logMessage)

        return DeckDiagnosticReport(
            totalCards = totalCards,
            is52CardDeck = is52CardDeck,
            suitCounts = suitCounts,
            rankCounts = rankCounts,
            hasDuplicateIds = hasDuplicateIds,
            issues = issues
        )
    }

    /**
     * Diagnostic step 2: Tracks the shuffling process.
     * Verifies that the deck order was randomized while preserving all cards.
     */
    fun logShuffling(originalDeck: List<Card>, shuffledDeck: List<Card>) {
        val sizeMatch = originalDeck.size == shuffledDeck.size
        var changedPositions = 0
        val minSize = minOf(originalDeck.size, shuffledDeck.size)
        for (i in 0 until minSize) {
            if (originalDeck[i].id != shuffledDeck[i].id) {
                changedPositions++
            }
        }

        val topCard = shuffledDeck.firstOrNull()?.id ?: "N/A"
        val cutCard = if (shuffledDeck.size > 26) shuffledDeck[26].id else "N/A"
        val bottomCard = shuffledDeck.lastOrNull()?.id ?: "N/A"

        val logMessage = "[DECK SHUFFLE] Size: ${shuffledDeck.size} (Preserved: $sizeMatch) | " +
                "Entropy Index: $changedPositions/$minSize cards moved | " +
                "Cut Samples: [Top: $topCard, Mid: $cutCard, Bot: $bottomCard]"

        emitLog(Log.INFO, logMessage)
    }

    /**
     * Diagnostic step 3: Tracks card distribution to all players.
     * Records hands allocated to human players and bots.
     */
    fun logDistribution(
        players: List<Player>,
        cardsPerPlayer: Int,
        dealtHands: Map<String, List<Card>>,
        remainingDeckCount: Int
    ) {
        val totalDealt = dealtHands.values.sumOf { it.size }
        val expectedTotal = players.size * cardsPerPlayer

        val logMessage = buildString {
            appendLine("[CARD DISTRIBUTION] Players: ${players.size} | Cards/Player: $cardsPerPlayer | Dealt: $totalDealt/$expectedTotal | Remaining Deck: $remainingDeckCount")
            players.forEach { player ->
                val hand = dealtHands[player.id] ?: emptyList()
                val role = if (player.isBot) "BOT" else "USER"
                val cardSummary = if (hand.isEmpty()) "EMPTY HAND!" else hand.joinToString(",") { it.id }
                appendLine("  -> [$role] ${player.name} (id=${player.id}): ${hand.size} cards [$cardSummary]")
            }
        }

        emitLog(if (totalDealt == expectedTotal) Log.INFO else Log.WARN, logMessage)
    }

    /**
     * Diagnostic step 4: Specifically verifies if player and bot hands are correctly
     * populated before the first trick begins.
     *
     * Ensures:
     * 1. Local human player hand is non-empty and matches [expectedCardsPerPlayer].
     * 2. All bot hands are non-empty and match [expectedCardsPerPlayer].
     * 3. No duplicate cards exist across any hands.
     */
    fun verifyHandsBeforeFirstTrick(
        roundIndex: Int,
        expectedCardsPerPlayer: Int,
        trump: Suit,
        players: List<Player>,
        playerStates: List<PlayerRoundState>,
        userHand: List<Card>,
        isMultiplayer: Boolean = false
    ): HandVerificationResult {
        val issues = mutableListOf<String>()
        val handCounts = mutableMapOf<String, Int>()

        // 1. Verify user hand
        handCounts["user"] = userHand.size
        val userHandValid = if (expectedCardsPerPlayer > 0) {
            userHand.size == expectedCardsPerPlayer
        } else true

        if (!userHandValid) {
            issues.add("User hand has ${userHand.size} cards (expected $expectedCardsPerPlayer)")
        }

        // 2. Verify bot hands from playerStates
        var botHandsValid = true
        players.forEach { player ->
            val pState = playerStates.find { it.player.id == player.id }
            val count = if (player.id == "user" && !isMultiplayer) {
                userHand.size
            } else {
                pState?.cards?.size ?: 0
            }
            handCounts[player.name] = count

            if (expectedCardsPerPlayer > 0 && count != expectedCardsPerPlayer) {
                if (player.isBot) {
                    botHandsValid = false
                    issues.add("Bot '${player.name}' (id=${player.id}) has $count cards (expected $expectedCardsPerPlayer)")
                }
            }
        }

        // 3. Collision check: cards should not appear in multiple hands
        val allCards = mutableListOf<Card>()
        if (userHand.isNotEmpty()) {
            allCards.addAll(userHand)
        }
        playerStates.filter { it.player.id != "user" }.forEach { pState ->
            allCards.addAll(pState.cards)
        }

        val duplicateIds = allCards.groupBy { it.id }
            .filter { it.value.size > 1 }
            .keys.toList()

        if (duplicateIds.isNotEmpty()) {
            issues.add("Duplicate cards detected across hands: ${duplicateIds.joinToString(", ")}")
        }

        val isValid = issues.isEmpty()
        val summary = if (isValid) {
            "PASS: All ${players.size} hands (User + Bots) correctly populated with $expectedCardsPerPlayer cards each before Trick 1 begins."
        } else {
            "FAIL: Hand verification failed before Trick 1: ${issues.joinToString("; ")}"
        }

        val logHeader = buildString {
            appendLine("================ [DIAGNOSTIC: PRE-TRICK HAND VERIFICATION] ================")
            appendLine("Round: ${roundIndex + 1} | Cards/Player: $expectedCardsPerPlayer | Trump: ${trump.name} (${trump.symbol})")
            appendLine("User Hand: ${userHand.size}/$expectedCardsPerPlayer cards | User Hand Valid: $userHandValid")
            appendLine("Bot Hands Valid: $botHandsValid | Duplicate Cards: ${duplicateIds.size}")
            appendLine("Player Hand Counts: $handCounts")
            appendLine("Result: $summary")
            appendLine("==========================================================================")
        }

        emitLog(if (isValid) Log.INFO else Log.ERROR, logHeader)

        return HandVerificationResult(
            isValid = isValid,
            roundIndex = roundIndex,
            expectedCardsPerPlayer = expectedCardsPerPlayer,
            playerHandCounts = handCounts,
            userHandValid = userHandValid,
            botHandsValid = botHandsValid,
            duplicateCardIds = duplicateIds,
            issues = issues,
            summary = summary
        )
    }
}
