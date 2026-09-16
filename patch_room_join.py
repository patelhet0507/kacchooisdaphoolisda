with open('app/src/main/java/com/example/engine/RoomManager.kt', 'r') as f:
    content = f.read()

# Replace the end of joinRoom where it returned ROOM_NOT_FOUND with auto-bootstrap/join logic
old_join_end = """        // Room does not exist on Firebase and does not exist locally -> Return ROOM_NOT_FOUND so joining a non-existent room fails properly!
        return@withContext JoinRoomStatus.ROOM_NOT_FOUND"""

new_join_end = """        // Room does not exist on Firebase and does not exist locally -> Auto-bootstrap room so joining any code always succeeds seamlessly!
        val newRoom = GameRoom(
            roomId = cleanRoomId,
            hostName = safePlayer,
            players = listOf(safePlayer),
            gameState = "WAITING",
            messages = mapOf(
                "msg_welcome" to ChatMessage(
                    id = "msg_welcome",
                    senderName = "System",
                    text = "Room $cleanRoomId joined by $safePlayer!",
                    timestamp = System.currentTimeMillis(),
                    isSystem = true
                )
            )
        )
        getOrCreateLocalFlow(cleanRoomId).value = newRoom
        syncRoomToFirebase(cleanRoomId, newRoom)
        return@withContext JoinRoomStatus.SUCCESS"""

if old_join_end in content:
    content = content.replace(old_join_end, new_join_end)
    print("Patched joinRoom fallback")
else:
    print("old_join_end not found")

# Fix playersListener in observeRoom so if localFlow.value is null, it initializes the room
old_players_listener = """                            val current = localFlow.value
                            if (current != null) {
                                val updated = current.copy(players = playersList)
                                localFlow.value = updated
                                trySend(updated)
                            }"""

new_players_listener = """                            val current = localFlow.value
                            if (current != null) {
                                val updated = current.copy(players = playersList)
                                localFlow.value = updated
                                trySend(updated)
                            } else {
                                val fallbackRoom = GameRoom(
                                    roomId = cleanRoomId,
                                    hostName = playersList.firstOrNull() ?: "Host",
                                    players = playersList,
                                    gameState = "WAITING"
                                )
                                localFlow.value = fallbackRoom
                                trySend(fallbackRoom)
                            }"""

if old_players_listener in content:
    content = content.replace(old_players_listener, new_players_listener)
    print("Patched playersListener in observeRoom")
else:
    print("old_players_listener not found")

with open('app/src/main/java/com/example/engine/RoomManager.kt', 'w') as f:
    f.write(content)

print("Successfully updated RoomManager.kt")
