package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SuitClub
import com.example.ui.theme.SuitDiamond
import com.example.ui.theme.SuitHeart
import com.example.ui.theme.SuitSpade

enum class Suit(
    val displayName: String,
    val localName: String,
    val mnemonic: String,
    val symbol: String,
    val isRed: Boolean
) {
    SPADES(
        displayName = "Spades",
        localName = "Kali",
        mnemonic = "Ka",
        symbol = "♠",
        isRed = false
    ),
    DIAMONDS(
        displayName = "Diamonds",
        localName = "Chokat",
        mnemonic = "Chu",
        symbol = "♦",
        isRed = true
    ),
    CLUBS(
        displayName = "Clubs",
        localName = "Fuli",
        mnemonic = "Fu",
        symbol = "♣",
        isRed = false
    ),
    HEARTS(
        displayName = "Hearts",
        localName = "Laal",
        mnemonic = "L",
        symbol = "♥",
        isRed = true
    );

    val suitColor: Color
        get() = when (this) {
            SPADES -> SuitSpade
            DIAMONDS -> SuitDiamond
            CLUBS -> SuitClub
            HEARTS -> SuitHeart
        }

    companion object {
        // Strict KACHUFUL rotation sequence
        val ROTATION_ORDER = listOf(SPADES, DIAMONDS, CLUBS, HEARTS)

        fun forRound(roundIndex: Int): Suit {
            return ROTATION_ORDER[roundIndex % ROTATION_ORDER.size]
        }
    }
}

enum class Rank(val value: Int, val symbol: String, val displayName: String) {
    TWO(2, "2", "2"),
    THREE(3, "3", "3"),
    FOUR(4, "4", "4"),
    FIVE(5, "5", "5"),
    SIX(6, "6", "6"),
    SEVEN(7, "7", "7"),
    EIGHT(8, "8", "8"),
    NINE(9, "9", "9"),
    TEN(10, "10", "10"),
    JACK(11, "J", "Jack"),
    QUEEN(12, "Q", "Queen"),
    KING(13, "K", "King"),
    ACE(14, "A", "Ace");
}

data class Card(
    val suit: Suit,
    val rank: Rank
) : Comparable<Card> {
    val id: String = "${rank.symbol}_${suit.name}"

    override fun compareTo(other: Card): Int {
        return this.rank.value.compareTo(other.rank.value)
    }

    override fun toString(): String {
        return "${rank.symbol}${suit.symbol}"
    }

    companion object {
        fun createStandardDeck(): List<Card> {
            val deck = mutableListOf<Card>()
            for (suit in Suit.values()) {
                for (rank in Rank.values()) {
                    deck.add(Card(suit, rank))
                }
            }
            return deck
        }

        fun fromId(id: String?): Card? {
            if (id.isNullOrBlank()) return null
            val parts = id.split("_")
            if (parts.size != 2) return null
            val rankSymbol = parts[0]
            val suitName = parts[1]
            val rank = Rank.values().find { it.symbol == rankSymbol } ?: return null
            val suit = Suit.values().find { it.name == suitName } ?: return null
            return Card(suit, rank)
        }
    }
}
