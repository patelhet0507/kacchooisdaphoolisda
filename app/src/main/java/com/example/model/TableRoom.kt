package com.example.model

import kotlin.random.Random

const val MAX_TABLE_SEATS = 6
const val MIN_TABLE_SEATS = 2

/** One human seat at a table. Host always uses id "user". */
data class TablePlayer(
    val id: String,
    val name: String
)

/**
 * A creatable table: anyone creates one, shares the [code],
 * up to [MAX_TABLE_SEATS] humans can join. Empty seats are
 * auto-filled with bots when the game starts.
 */
data class TableRoom(
    val code: String,
    val name: String,
    val mode: GameMode,
    val scoringRule: ScoringRule = ScoringRule.STANDARD,
    val maxSeats: Int = 4,
    val humans: List<TablePlayer> = emptyList(),
    val isStarted: Boolean = false
) {
    /** Bots needed to fill the table. */
    fun botFillCount(): Int = (maxSeats - humans.size).coerceAtLeast(0)
}

/** In-memory table registry (same-device lobby; no backend). */
object TableRegistry {
    private val rooms = mutableMapOf<String, TableRoom>()

    fun create(
        hostName: String,
        tableName: String,
        maxSeats: Int,
        mode: GameMode,
        scoringRule: ScoringRule = ScoringRule.STANDARD
    ): TableRoom {
        var code: String
        do {
            code = generateRoomCode()
        } while (rooms.containsKey(code))
        val room = TableRoom(
            code = code,
            name = tableName.ifBlank { "Table $code" },
            mode = mode,
            scoringRule = scoringRule,
            maxSeats = maxSeats.coerceIn(MIN_TABLE_SEATS, MAX_TABLE_SEATS),
            humans = listOf(TablePlayer("user", hostName.ifBlank { "You" }))
        )
        rooms[code] = room
        return room
    }

    /** Returns null for unknown/full/already-started tables. */
    fun join(code: String, name: String): TableRoom? {
        val room = find(code) ?: return null
        if (room.isStarted || room.humans.size >= room.maxSeats) return null
        val seat = TablePlayer(
            id = "human_${room.humans.size}_${Random.nextInt(10000)}",
            name = name.ifBlank { "Player ${room.humans.size + 1}" }
        )
        val updated = room.copy(humans = room.humans + seat)
        rooms[room.code] = updated
        return updated
    }

    fun find(code: String): TableRoom? = rooms[normalize(code)]

    fun refresh(code: String): TableRoom? = find(code)

    fun markStarted(code: String) {
        val room = rooms[normalize(code)] ?: return
        rooms[room.code] = room.copy(isStarted = true)
    }
}

fun generateRoomCode(): String {
    val alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
    return (1..6).map { alphabet[Random.nextInt(alphabet.length)] }.joinToString("")
}

private fun normalize(code: String): String =
    code.trim().uppercase().removePrefix("KP-").filter { it.isLetterOrDigit() }
