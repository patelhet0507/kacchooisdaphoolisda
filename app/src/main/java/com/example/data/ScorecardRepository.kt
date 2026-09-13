package com.example.data

import com.example.model.GameMode
import com.example.model.ScoringRule
import com.example.model.Suit
import kotlinx.coroutines.flow.Flow

class ScorecardRepository(private val dao: ScorecardDao) {

    val allGames: Flow<List<ScorecardGameEntity>> = dao.getAllGames()

    fun getGame(gameId: Long): Flow<ScorecardGameEntity?> = dao.getGameById(gameId)

    fun getRounds(gameId: Long): Flow<List<ScorecardRoundEntity>> = dao.getRoundsForGame(gameId)

    suspend fun createNewGame(
        title: String,
        playerNames: List<String>,
        gameMode: GameMode,
        scoringRule: ScoringRule,
        customRounds: List<Int>? = null
    ): Long {
        val roundsConfig = customRounds ?: gameMode.generateRounds(playerNames.size)
        val gameEntity = ScorecardGameEntity(
            title = title.ifBlank { "Game (${playerNames.size} Players)" },
            playerNamesRaw = playerNames.joinToString(","),
            roundsConfigRaw = roundsConfig.joinToString(","),
            currentRoundIndex = 0,
            scoringRuleName = scoringRule.name
        )

        val gameId = dao.insertGame(gameEntity)

        // Pre-create all rounds with trump suits and dealer rotations
        val roundEntities = roundsConfig.mapIndexed { index, cardCount ->
            val trump = Suit.forRound(index)
            val dealerIndex = index % playerNames.size
            ScorecardRoundEntity(
                gameId = gameId,
                roundIndex = index,
                cardCount = cardCount,
                trumpSuitName = trump.name,
                dealerIndex = dealerIndex,
                bidsRaw = playerNames.map { "" }.joinToString(","),
                tricksWonRaw = playerNames.map { "" }.joinToString(","),
                roundScoresRaw = playerNames.map { "0" }.joinToString(",")
            )
        }

        dao.insertRounds(roundEntities)
        return gameId
    }

    suspend fun updatePlayerName(gameId: Long, playerIndex: Int, newName: String) {
        val game = dao.getGameByIdSync(gameId) ?: return
        val names = game.getPlayerNames().toMutableList()
        if (playerIndex in names.indices && newName.isNotBlank()) {
            names[playerIndex] = newName.trim()
            val updatedGame = game.copy(playerNamesRaw = names.joinToString(","))
            dao.updateGame(updatedGame)
        }
    }


    suspend fun updateRound(
        round: ScorecardRoundEntity,
        bids: List<Int?>,
        tricksWon: List<Int?>,
        scoringRule: ScoringRule,
        markGameNextRound: Boolean = false,
        gameId: Long = round.gameId
    ) {
        val scores = bids.indices.map { i ->
            val b = bids.getOrNull(i)
            val w = tricksWon.getOrNull(i)
            if (b != null && w != null) {
                scoringRule.calculateScore(b, w)
            } else {
                0
            }
        }

        val updatedRound = round.copy(
            bidsRaw = bids.joinToString(",") { it?.toString() ?: "" },
            tricksWonRaw = tricksWon.joinToString(",") { it?.toString() ?: "" },
            roundScoresRaw = scores.joinToString(",") { it.toString() }
        )

        dao.updateRound(updatedRound)

        if (markGameNextRound) {
            val rounds = dao.getRoundsForGameSync(gameId)
            val isLast = round.roundIndex >= rounds.size - 1
            // Fetch game
            // Update game current round
            // Dao query
        }
    }

    suspend fun updateGameProgress(game: ScorecardGameEntity) {
        dao.updateGame(game)
    }

    suspend fun deleteGame(gameId: Long) {
        dao.deleteRoundsForGame(gameId)
        dao.deleteGame(gameId)
    }
}
