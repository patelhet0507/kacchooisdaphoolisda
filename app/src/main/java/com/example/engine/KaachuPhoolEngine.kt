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
     * Improved with card tracking and heuristics.
     */
    fun chooseBotCard(
        hand: List<Card>,
        leadSuit: Suit?,
        trumpSuit: Suit,
        currentTrick: List<PlayedCard>,
        tricksWon: Int,
        targetBid: Int,
        playedCardsInRound: List<Card>,
        difficulty: BotDifficulty = BotDifficulty.MEDIUM
    ): Card {
        val playableCards = getPlayableCards(hand, leadSuit)
        if (playableCards.size == 1) return playableCards.first()

        // Easy difficulty still has some randomness
        if (difficulty == BotDifficulty.EASY && (0..99).random() < 35) {
            return playableCards.random()
        }

        val needsTricks = tricksWon < targetBid
        val hasEnoughTricks = tricksWon >= targetBid
        
        val currentWinningCard = if (currentTrick.isNotEmpty() && leadSuit != null) {
            determineTrickWinner(currentTrick, trumpSuit, leadSuit).card
        } else null

        // Tracking: What's still out there?
        val cardsNotSeen = Card.createStandardDeck().filter { card ->
            !playedCardsInRound.contains(card) && !hand.contains(card)
        }

        // Helper to check if a card is currently the highest of its suit in the game
        fun isHighestRemaining(card: Card): Boolean {
            return cardsNotSeen.none { it.suit == card.suit && it.rank.value > card.rank.value }
        }

        // If bot is leading the trick (first to play)
        if (leadSuit == null) {
            return if (needsTricks) {
                // 1. Try to lead a "Master" card (highest remaining in its suit)
                val masterCards = playableCards.filter { isHighestRemaining(it) }
                
                // Prefer non-trump masters first to save trumps
                masterCards.filter { it.suit != trumpSuit }.maxByOrNull { it.rank.value }
                    ?: masterCards.maxByOrNull { it.rank.value }
                    // 2. Otherwise lead a high non-trump
                    ?: playableCards.filter { it.suit != trumpSuit && it.rank >= Rank.TEN }
                        .maxByOrNull { it.rank.value }
                    // 3. Otherwise lead a trump
                    ?: playableCards.filter { it.suit == trumpSuit }.maxByOrNull { it.rank.value }
                    // 4. Default to highest card
                    ?: playableCards.maxByOrNull { it.rank.value }!!
            } else {
                // Already have enough tricks! Avoid winning.
                // 1. Lead a low non-trump card
                playableCards.filter { it.suit != trumpSuit }
                    .minByOrNull { it.rank.value }
                    // 2. If only trumps, lead the lowest
                    ?: playableCards.minByOrNull { it.rank.value }!!
            }
        }

        // Bot is following suit or playing after leader
        val canFollowSuit = playableCards.all { it.suit == leadSuit }

        if (canFollowSuit) {
            if (needsTricks) {
                // Try to win the trick
                val winningCards = playableCards.filter { card ->
                    isCardBetterThan(card, currentWinningCard, trumpSuit, leadSuit)
                }
                
                if (winningCards.isNotEmpty()) {
                    // Strategy: Win with the SMALLEST card possible to save high cards
                    return winningCards.minByOrNull { it.rank.value }!!
                } else {
                    // Cannot win: play lowest card to save higher cards for later
                    return playableCards.minByOrNull { it.rank.value }!!
                }
            } else {
                // Do not want to win: play the highest card that still LOSES (to bleed high cards safely)
                val losingCards = playableCards.filter { card ->
                    !isCardBetterThan(card, currentWinningCard, trumpSuit, leadSuit)
                }
                return losingCards.maxByOrNull { it.rank.value }
                    ?: playableCards.minByOrNull { it.rank.value }!!
            }
        } else {
            // Cannot follow suit: can play trump or discard
            val trumps = playableCards.filter { it.suit == trumpSuit }
            val nonTrumps = playableCards.filter { it.suit != trumpSuit }

            if (needsTricks && trumps.isNotEmpty()) {
                // Strategic Trumping
                val winningTrumps = trumps.filter { card ->
                    isCardBetterThan(card, currentWinningCard, trumpSuit, leadSuit)
                }
                
                if (winningTrumps.isNotEmpty()) {
                    // Only trump if the trick is "valuable" or we are desperate
                    // For now, if we need tricks, we just take it with the lowest winning trump
                    return winningTrumps.minByOrNull { it.rank.value }!!
                }
            }

            // Discard Strategy
            if (hasEnoughTricks) {
                // Discard highest non-trump to get rid of risky winners
                return nonTrumps.maxByOrNull { it.rank.value }
                    ?: playableCards.minByOrNull { it.rank.value }!!
            } else {
                // Discard lowest non-trump
                return nonTrumps.minByOrNull { it.rank.value }
                    ?: playableCards.minByOrNull { it.rank.value }!!
            }
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
