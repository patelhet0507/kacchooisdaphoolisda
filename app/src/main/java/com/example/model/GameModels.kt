package com.example.model

import androidx.compose.ui.graphics.Color

data class Player(
    val id: String,
    val name: String,
    val isBot: Boolean = false,
    val avatarEmoji: String = "👤",
    val colorHex: Long = 0xFF10B981
) {
    companion object {
        val DEFAULT_BOTS = listOf(
            Player("bot_1", "Aarav", isBot = true, avatarEmoji = "🦁", colorHex = 0xFFF59E0B),
            Player("bot_2", "Priya", isBot = true, avatarEmoji = "🌸", colorHex = 0xFFEC4899),
            Player("bot_3", "Rohan", isBot = true, avatarEmoji = "⚡", colorHex = 0xFF3B82F6),
            Player("bot_4", "Meera", isBot = true, avatarEmoji = "💎", colorHex = 0xFF8B5CF6),
            Player("bot_5", "Vikram", isBot = true, avatarEmoji = "🦅", colorHex = 0xFF10B981)
        )
    }
}

data class PlayerRoundState(
    val player: Player,
    val cards: List<Card> = emptyList(),
    val bid: Int? = null,
    val tricksWon: Int = 0,
    val roundScore: Int = 0,
    val totalScore: Int = 0
)

data class PlayedCard(
    val player: Player,
    val card: Card
)

data class Trick(
    val trickIndex: Int,
    val cards: List<PlayedCard> = emptyList(),
    val leadSuit: Suit? = null,
    val winner: Player? = null
)

enum class GamePhase {
    BIDDING,
    PLAYING,
    TRICK_FINISHED,
    ROUND_FINISHED,
    GAME_OVER
}

enum class GameMode(val title: String, val description: String) {
    QUICK("Quick Match (1 → 5 → 1)", "Starts at 1 card, goes up to 5, and back to 1 (9 Rounds)"),
    CLASSIC("Classic Standard (1 → 8 → 1)", "Starts at 1 card, goes up to 8, and back to 1 (15 Rounds)"),
    FULL("Full Ladder (1 → 10 → 1)", "Starts at 1 card, goes up to 10, and back to 1 (19 Rounds)"),
    LADDER_13("Grand Ladder (1 → 13 → 1)", "Starts at 1 card, goes up to 13, and back to 1 (25 Rounds)"),
    ASCENDING_8("Ascending (1 → 8 Cards)", "Starts at 1 card and ascends up to 8 cards (8 Rounds)"),
    ASCENDING_5("Short Ascending (1 → 5 Cards)", "Starts at 1 card and ascends up to 5 cards (5 Rounds)"),
    SINGLE_DOWN("Single Drop (8 → 1 Cards)", "8 cards descending to 1 card (8 Rounds)");

    fun generateRounds(playerCount: Int = 4): List<Int> {
        val maxAvailable = (52 / playerCount.coerceAtLeast(1)).coerceAtLeast(1)
        val maxCards = when (this) {
            QUICK -> minOf(5, maxAvailable)
            CLASSIC -> minOf(8, maxAvailable)
            FULL -> minOf(10, maxAvailable)
            LADDER_13 -> minOf(13, maxAvailable)
            ASCENDING_8 -> minOf(8, maxAvailable)
            ASCENDING_5 -> minOf(5, maxAvailable)
            SINGLE_DOWN -> minOf(8, maxAvailable)
        }

        return when (this) {
            QUICK, CLASSIC, FULL, LADDER_13 -> {
                if (maxCards <= 1) listOf(1)
                else (1..maxCards).toList() + ((maxCards - 1) downTo 1).toList()
            }
            ASCENDING_8, ASCENDING_5 -> {
                (1..maxCards).toList()
            }
            SINGLE_DOWN -> {
                (maxCards downTo 1).toList()
            }
        }
    }
}

enum class ScoringRule(val title: String, val description: String) {
    STANDARD(
        "Option 1: 0 Points on Miss",
        "Exact bid scores 10 + n (where n is the bid, e.g. Bid 3 made = 13 pts). Missed bid (Bid ≠ Won) scores 0 pts."
    ),
    PENALTY(
        "Option 2: -10 + n Penalty on Miss",
        "Exact bid scores 10 + n. Missed bid (Bid ≠ Won) scores -10 + n pts (where n is your bid, e.g. Bid 3 missed = -7 pts, Bid 0 missed = -10 pts)."
    ),
    BONUS(
        "Option 3: 1 pt/Trick + 10 Bonus",
        "Earn 1 point per trick won, plus a +10 point bonus if you hit your exact bid."
    );

    fun calculateScore(bid: Int, tricksWon: Int): Int {
        return when (this) {
            STANDARD -> if (bid == tricksWon) 10 + bid else 0
            PENALTY -> if (bid == tricksWon) 10 + bid else -10 + bid
            BONUS -> {
                val trickPts = tricksWon
                val bonus = if (bid == tricksWon) 10 else 0
                trickPts + bonus
            }
        }
    }
}

enum class BotDifficulty(val title: String, val description: String) {
    EASY("Easy AI", "Casual bots with relaxed bidding and occasional sub-optimal card play."),
    MEDIUM("Medium AI", "Standard balanced AI with logical card evaluation and bidding."),
    HARD("Hard AI", "Expert bots with precise card tracking, strategic trumps, and sharp bidding.")
}


