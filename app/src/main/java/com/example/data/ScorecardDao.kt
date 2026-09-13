package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScorecardDao {
    @Query("SELECT * FROM scorecard_games ORDER BY createdAt DESC")
    fun getAllGames(): Flow<List<ScorecardGameEntity>>

    @Query("SELECT * FROM scorecard_games WHERE id = :id LIMIT 1")
    fun getGameById(id: Long): Flow<ScorecardGameEntity?>

    @Query("SELECT * FROM scorecard_games WHERE id = :id LIMIT 1")
    suspend fun getGameByIdSync(id: Long): ScorecardGameEntity?

    @Query("SELECT * FROM scorecard_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC")
    fun getRoundsForGame(gameId: Long): Flow<List<ScorecardRoundEntity>>

    @Query("SELECT * FROM scorecard_rounds WHERE gameId = :gameId ORDER BY roundIndex ASC")
    suspend fun getRoundsForGameSync(gameId: Long): List<ScorecardRoundEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: ScorecardGameEntity): Long

    @Update
    suspend fun updateGame(game: ScorecardGameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: ScorecardRoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRounds(rounds: List<ScorecardRoundEntity>)

    @Update
    suspend fun updateRound(round: ScorecardRoundEntity)

    @Query("DELETE FROM scorecard_games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)

    @Query("DELETE FROM scorecard_rounds WHERE gameId = :gameId")
    suspend fun deleteRoundsForGame(gameId: Long)
}
