package com.example.engine

import com.example.model.BotDifficulty
import com.example.model.Card
import com.example.model.PlayedCard
import com.example.model.Player
import com.example.model.Rank
import com.example.model.Suit

object KaachuPhoolEngine {

    /**
     * Shuffles a standard 52-card deck and deals [cardsPerPlayer] to each player.
     * Hands are sorted cleanly by suit and descending rank.
     */
    fun dealHands(players: List<Player>, cardsPerPlayer: Int): Map<String, List<Card>> {
        val deck = Card.createStandardDeck().shuffled().toMutableList()
        val hands = mutableMapOf<String, MutableList<Card>>()

        for (player in players) {
            hands[player.id] = mutableListOf()
        }

        repeat(cardsPerPlayer) {
            for (player in players) {
                if (deck.isNotEmpty()) {
                    hands[player.id]?.add(deck.removeAt(0))
                }
            }
        }

        // Sort hands by suit order and rank descending
        return hands.mapValues { (_, cardList) ->
            cardList.sortedWith(
                compareBy<Card> { card ->
                    when (card.suit) {
                        Suit.SPADES -> 0
                        Suit.DIAMONDS -> 1
                        Suit.CLUBS -> 2
                        Suit.HEARTS -> 3
                    }
                }.thenByDescending { it.rank.value }
            )
        }
    }

    /**
     * Hook Rule (Bandi / Nasti):
     * The dealer cannot bid a number of tricks that causes the sum of all bids
     * to equal the total cards dealt in the round.
     */
    fun getForbiddenBidForDealer(
        otherBidsSum: Int,
        totalCards: Int
    ): Int? {
        val forbidden = totalCards - otherBidsSum
        return if (forbidden in 0..totalCards) forbidden else null
    }

    /**
     * Determines which cards in a hand can legally be played.
     * Player MUST follow lead suit if they possess at least one card of that suit.
     */
    fun getPlayableCards(hand: List<Card>, leadSuit: Suit?): List<Card> {
        if (leadSuit == null) return hand
        val matching = hand.filter { it.suit == leadSuit }
        return if (matching.isNotEmpty()) matching else hand
    }

    /**
     * Determines the winner of a trick according to standard trick-taking rules:
     * 1. If any cards of the trump suit were played, the highest trump card wins.
     * 2. Otherwise, the highest card of the lead suit wins.
     */
    fun determineTrickWinner(
        cards: List<PlayedCard>,
        trumpSuit: Suit,
        leadSuit: Suit
    ): PlayedCard {
        require(cards.isNotEmpty()) { "Trick cannot be empty to determine winner" }

        val trumpsPlayed = cards.filter { it.card.suit == trumpSuit }
        return if (trumpsPlayed.isNotEmpty()) {
            trumpsPlayed.maxByOrNull { it.card.rank.value }!!
        } else {
            val leadSuitCards = cards.filter { it.card.suit == leadSuit }
            leadSuitCards.maxByOrNull { it.card.rank.value } ?: cards.first()
        }
    }

    /**
     * AI Bot Bidding Logic
     */
    fun calculateBotBid(
        hand: List<Card>,
        trumpSuit: Suit,
        totalCards: Int,
        isDealer: Boolean,
        otherBidsSum: Int,
        difficulty: BotDifficulty = BotDifficulty.MEDIUM
    ): Int {
        var estimatedTricks = 0.0

        for (card in hand) {
            val isTrump = card.suit == trumpSuit
            if (isTrump) {
                when (card.rank) {
                    Rank.ACE -> estimatedTricks += 1.0
                    Rank.KING -> estimatedTricks += 0.9
                    Rank.QUEEN -> estimatedTricks += 0.75
                    Rank.JACK -> estimatedTricks += 0.55
                    Rank.TEN -> estimatedTricks += 0.35
                    else -> estimatedTricks += 0.15
                }
            } else {
                when (card.rank) {
                    Rank.ACE -> estimatedTricks += 0.8
                    Rank.KING -> estimatedTricks += 0.5
                    Rank.QUEEN -> estimatedTricks += 0.25
                    else -> {}
                }
            }
        }

        var bid = kotlin.math.round(estimatedTricks).toInt()

        if (difficulty == BotDifficulty.EASY) {
            val variance = (-1..1).random()
            bid += variance
        } else if (difficulty == BotDifficulty.HARD) {
            if (estimatedTricks - bid > 0.35) bid += 1
            else if (bid - estimatedTricks > 0.35 && bid > 0) bid -= 1
        }

        bid = bid.coerceIn(0, totalCards)

        // Enforce hook rule for dealer
        if (isDealer) {
            val forbidden = getForbiddenBidForDealer(otherBidsSum, totalCards)
            if (forbidden != null && bid == forbidden) {
                // If forced away from estimated bid:
                bid = if (estimatedTricks > bid && bid < totalCards) {
                    bid + 1
                } else if (bid > 0) {
                    bid - 1
                } else {
                    1
                }
                bid = bid.coerceIn(0, totalCards)
            }
        }

        return bid
    }

    /**
     * AI Bot Card Selection Logic
     */
    fun chooseBotCard(
        hand: List<Card>,
        leadSuit: Suit?,
        trumpSuit: Suit,
        currentTrick: List<PlayedCard>,
        tricksWon: Int,
        targetBid: Int,
        difficulty: BotDifficulty = BotDifficulty.MEDIUM
    ): Card {
        val playableCards = getPlayableCards(hand, leadSuit)
        if (playableCards.size == 1) return playableCards.first()

        if (difficulty == BotDifficulty.EASY && (0..99).random() < 35) {
            return playableCards.random()
        }

        val needsTricks = tricksWon < targetBid
        val currentWinningCard = if (currentTrick.isNotEmpty() && leadSuit != null) {
            determineTrickWinner(currentTrick, trumpSuit, leadSuit).card
        } else null

        // If bot is leading the trick (first to play)
        if (leadSuit == null) {
            return if (needsTricks) {
                // Try to lead a high non-trump Ace/King or high trump to win
                playableCards.filter { it.suit != trumpSuit && it.rank >= Rank.KING }
                    .maxByOrNull { it.rank.value }
                    ?: playableCards.filter { it.suit == trumpSuit && it.rank >= Rank.JACK }
                        .maxByOrNull { it.rank.value }
                    ?: playableCards.maxByOrNull { it.rank.value }!!
            } else {
                // Bot already has enough tricks! Lead lowest card to duck
                playableCards.filter { it.suit != trumpSuit }
                    .minByOrNull { it.rank.value }
                    ?: playableCards.minByOrNull { it.rank.value }!!
            }
        }

        // Bot is following suit or playing after leader
        val canFollowSuit = playableCards.all { it.suit == leadSuit }

        if (canFollowSuit) {
            if (needsTricks) {
                // Try to win with smallest card that beats current winning card
                val winningCards = playableCards.filter { card ->
                    isCardBetterThan(card, currentWinningCard, trumpSuit, leadSuit)
                }
                return winningCards.minByOrNull { it.rank.value }
                    ?: playableCards.minByOrNull { it.rank.value }!!
            } else {
                // Do not want to win: play highest card that still loses, or lowest card
                val losingCards = playableCards.filter { card ->
                    !isCardBetterThan(card, currentWinningCard, trumpSuit, leadSuit)
                }
                return losingCards.maxByOrNull { it.rank.value }
                    ?: playableCards.minByOrNull { it.rank.value }!!
            }
        } else {
            // Cannot follow suit: can play trump or discard other suit
            val trumps = playableCards.filter { it.suit == trumpSuit }
            val nonTrumps = playableCards.filter { it.suit != trumpSuit }

            if (needsTricks && trumps.isNotEmpty()) {
                // Check if current trick is already trumped
                val winningCards = trumps.filter { card ->
                    isCardBetterThan(card, currentWinningCard, trumpSuit, leadSuit)
                }
                if (winningCards.isNotEmpty()) {
                    // Win with lowest winning trump!
                    return winningCards.minByOrNull { it.rank.value }!!
                }
            }

            // Otherwise discard lowest non-trump card or lowest card overall
            return nonTrumps.minByOrNull { it.rank.value }
                ?: playableCards.minByOrNull { it.rank.value }!!
        }
    }

    private fun isCardBetterThan(
        candidate: Card,
        currentWinner: Card?,
        trumpSuit: Suit,
        leadSuit: Suit
    ): Boolean {
        if (currentWinner == null) return true

        val candidateIsTrump = candidate.suit == trumpSuit
        val winnerIsTrump = currentWinner.suit == trumpSuit

        return when {
            candidateIsTrump && !winnerIsTrump -> true
            !candidateIsTrump && winnerIsTrump -> false
            candidateIsTrump && winnerIsTrump -> candidate.rank.value > currentWinner.rank.value
            candidate.suit == leadSuit && currentWinner.suit == leadSuit -> candidate.rank.value > currentWinner.rank.value
            else -> false
        }
    }
}
