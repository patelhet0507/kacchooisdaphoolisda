package com.example

import com.example.engine.GameDiagnosticLogger
import com.example.engine.KaachuPhoolEngine
import com.example.model.Card
import com.example.model.GameMode
import com.example.model.GamePhase
import com.example.model.Player
import com.example.model.PlayerRoundState
import com.example.model.Rank
import com.example.model.Suit
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameStateInitializationTest {

    @Test
    fun testDeckInitializationContains52UniqueCards() {
        val deck = Card.createStandardDeck()
        // Standard deck must have exactly 52 cards
        assertEquals(52, deck.size)

        // All 52 cards must have unique IDs
        val uniqueIds = deck.map { it.id }.toSet()
        assertEquals(52, uniqueIds.size)

        // Each suit must have exactly 13 cards
        for (suit in Suit.values()) {
            val suitCards = deck.filter { it.suit == suit }
            assertEquals("Suit $suit must have 13 cards", 13, suitCards.size)
        }

        // Each rank must appear exactly 4 times (one per suit)
        for (rank in Rank.values()) {
            val rankCards = deck.filter { it.rank == rank }
            assertEquals("Rank $rank must appear in all 4 suits", 4, rankCards.size)
        }
    }

    @Test
    fun testDealCardsDistributesHandsToLocalPlayerAndBots() {
        val user = Player(id = "user", name = "You", isBot = false)
        val bots = Player.DEFAULT_BOTS.take(3)
        val allPlayers = listOf(user) + bots

        val cardsPerPlayer = 5
        val dealtHands = KaachuPhoolEngine.dealCards(allPlayers, cardsPerPlayer)

        // Hand map must have an entry for each player
        assertEquals(allPlayers.size, dealtHands.size)

        val allDealtCards = mutableListOf<Card>()

        for (player in allPlayers) {
            val hand = dealtHands[player.id]
            assertNotNull("Hand for player ${player.name} must not be null", hand)
            assertEquals("Hand for player ${player.name} must have $cardsPerPlayer cards", cardsPerPlayer, hand!!.size)
            allDealtCards.addAll(hand)
        }

        // Verify no duplicate cards are dealt among any players
        assertEquals(
            "All dealt cards across all players must be unique",
            allPlayers.size * cardsPerPlayer,
            allDealtCards.map { it.id }.toSet().size
        )
    }

    @Test
    fun testGameViewModelStateInitializationInAIMode() {
        val viewModel = GameViewModel()

        viewModel.startNewGame(
            gameMode = GameMode.QUICK,
            userName = "Captain",
            botCount = 3
        )

        val state = viewModel.uiState.value

        // Verify single-player game mode flags
        assertFalse(state.isMultiplayer)
        assertEquals(4, state.players.size)

        // Verify local user and bots
        val localUser = state.players.first()
        assertEquals("user", localUser.id)
        assertFalse(localUser.isBot)
        assertEquals("Captain", localUser.name)

        val bots = state.players.drop(1)
        assertEquals(3, bots.size)
        assertTrue(bots.all { it.isBot })

        // Verify round 0 card count
        val expectedCardsInRound0 = state.rounds[0]
        assertEquals(expectedCardsInRound0, state.currentRoundCardCount)

        // Verify local player received expected number of cards
        assertEquals(expectedCardsInRound0, state.userHand.size)

        // Verify each bot received expected number of cards
        for (pState in state.playerStates) {
            assertEquals(
                "Player ${pState.player.name} hand size",
                expectedCardsInRound0,
                pState.cards.size
            )
        }

        // Verify the game loop is at BIDDING phase initially before any card play begins
        assertEquals(GamePhase.BIDDING, state.phase)

        // Verify playerSeats and userHand exposed by viewModel are populated
        assertEquals(expectedCardsInRound0, viewModel.userHand.value.size)
        assertEquals(4, viewModel.playerSeats.value.size)
    }

    @Test
    fun testDiagnosticLoggingTracksDeckStateShufflingAndDistribution() {
        GameDiagnosticLogger.clearHistory()

        val players = listOf(
            Player(id = "user", name = "TestUser", isBot = false),
            Player(id = "bot_1", name = "Aarav", isBot = true),
            Player(id = "bot_2", name = "Priya", isBot = true),
            Player(id = "bot_3", name = "Rohan", isBot = true)
        )

        val cardsPerPlayer = 3
        val dealt = KaachuPhoolEngine.dealCards(players, cardsPerPlayer)
        assertEquals(4, dealt.size)

        val logs = GameDiagnosticLogger.getLogHistory()
        assertTrue("Diagnostic logs must not be empty", logs.isNotEmpty())

        val hasDeckStateLog = logs.any { it.contains("[DECK STATE]") }
        assertTrue("Must log deck state", hasDeckStateLog)

        val hasShuffleLog = logs.any { it.contains("[DECK SHUFFLE]") }
        assertTrue("Must log deck shuffling", hasShuffleLog)

        val hasDistributionLog = logs.any { it.contains("[CARD DISTRIBUTION]") }
        assertTrue("Must log card distribution", hasDistributionLog)
    }

    @Test
    fun testPreTrickDiagnosticsPassesForValidHands() {
        val viewModel = GameViewModel()
        viewModel.startNewGame(
            gameMode = GameMode.QUICK,
            userName = "Captain",
            botCount = 3
        )

        val result = viewModel.performPreTrickDiagnostics()
        assertTrue("Diagnostic verification must pass when hands are correctly populated: ${result.issues}", result.isValid)
        assertTrue("User hand must be valid", result.userHandValid)
        assertTrue("Bot hands must be valid", result.botHandsValid)
        assertTrue("There must be no duplicate card collisions across hands", result.duplicateCardIds.isEmpty())
        assertEquals(0, result.issues.size)
    }

    @Test
    fun testPreTrickDiagnosticsDetectsMissingOrMismatchedHands() {
        val players = listOf(
            Player(id = "user", name = "Player 1", isBot = false),
            Player(id = "bot_1", name = "Bot 1", isBot = true)
        )

        val playerStates = listOf(
            PlayerRoundState(player = players[0], cards = emptyList()), // empty user hand!
            PlayerRoundState(player = players[1], cards = emptyList()) // empty bot hand!
        )

        val result = GameDiagnosticLogger.verifyHandsBeforeFirstTrick(
            roundIndex = 0,
            expectedCardsPerPlayer = 4,
            trump = Suit.SPADES,
            players = players,
            playerStates = playerStates,
            userHand = emptyList()
        )

        assertFalse("Verification must fail when hands are empty before trick 1", result.isValid)
        assertFalse("User hand must be marked invalid", result.userHandValid)
        assertFalse("Bot hand must be marked invalid", result.botHandsValid)
        assertTrue("Issues list must detail the failure", result.issues.isNotEmpty())
    }
}
