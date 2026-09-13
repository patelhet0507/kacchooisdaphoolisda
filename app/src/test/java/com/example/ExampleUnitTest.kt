package com.example

import com.example.engine.KaachuPhoolEngine
import com.example.model.Card
import com.example.model.PlayedCard
import com.example.model.Player
import com.example.model.Rank
import com.example.model.ScoringRule
import com.example.model.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KaachuPhoolGameTest {

    @Test
    fun verifyKachufulTrumpRotation() {
        // Strict rotation: Ka (Spades), Chu (Diamonds), Fu (Clubs), L (Hearts)
        assertEquals(Suit.SPADES, Suit.forRound(0))
        assertEquals(Suit.DIAMONDS, Suit.forRound(1))
        assertEquals(Suit.CLUBS, Suit.forRound(2))
        assertEquals(Suit.HEARTS, Suit.forRound(3))
        assertEquals(Suit.SPADES, Suit.forRound(4))
    }

    @Test
    fun verifyDealerHookRule() {
        // 5 cards dealt, other players bid 1, 2, 1 = 4.
        // Dealer cannot bid 5 - 4 = 1.
        val forbidden = KaachuPhoolEngine.getForbiddenBidForDealer(otherBidsSum = 4, totalCards = 5)
        assertEquals(1, forbidden)

        // If other bids already exceed total cards, no hook collision inside 0..totalCards
        val noForbidden = KaachuPhoolEngine.getForbiddenBidForDealer(otherBidsSum = 6, totalCards = 5)
        assertNull(noForbidden)
    }

    @Test
    fun verifyTrickWinnerTrumpVsLeadSuit() {
        val playerA = Player("a", "Aarav")
        val playerB = Player("b", "Priya")
        val playerC = Player("c", "Rohan")

        // Lead suit is Clubs, Trump is Spades (Ka)
        val trick = listOf(
            PlayedCard(playerA, Card(Suit.CLUBS, Rank.KING)), // Lead Ace/King of Clubs
            PlayedCard(playerB, Card(Suit.SPADES, Rank.TWO)), // Cut with 2 of Spades (Trump!)
            PlayedCard(playerC, Card(Suit.CLUBS, Rank.ACE))
        )

        val winner = KaachuPhoolEngine.determineTrickWinner(
            cards = trick,
            trumpSuit = Suit.SPADES,
            leadSuit = Suit.CLUBS
        )

        // Player B cut with Trump 2♠, beating Ace of Clubs!
        assertEquals(playerB.id, winner.player.id)
    }

    @Test
    fun verifyScoringCalculations() {
        val standard = ScoringRule.STANDARD

        // Bid 3, won 3 -> 13 pts
        assertEquals(13, standard.calculateScore(bid = 3, tricksWon = 3))

        // Bid 0, won 0 -> 10 pts
        assertEquals(10, standard.calculateScore(bid = 0, tricksWon = 0))

        // Bid 2, won 1 -> 0 pts
        assertEquals(0, standard.calculateScore(bid = 2, tricksWon = 1))

        // Bid 2, won 3 -> 0 pts
        assertEquals(0, standard.calculateScore(bid = 2, tricksWon = 3))
    }
}
