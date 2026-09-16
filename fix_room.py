with open('app/src/main/java/com/example/engine/RoomManager.kt', 'r') as f:
    content = f.read()

old_code = """        // Room does not exist on Firebase and does not exist locally -> Auto-create / bootstrap room so joining any code always succeeds!
        val newRoom = GameRoom(
            roomId = cleanRoomId,
            hostName = safePlayer,
            players = listOf(safePlayer),
            gameState = "WAITING",
            messages = mapOf(
                "msg_welcome" to ChatMessage(
                    id = "msg_welcome",
                    senderName = "System",
                    text = "Room $cleanRoomId created by $safePlayer!",
                    timestamp = System.currentTimeMillis(),
                    isSystem = true
                )
            )
        )
        getOrCreateLocalFlow(cleanRoomId).value = newRoom
        syncRoomToFirebase(cleanRoomId, newRoom)
        return@withContext JoinRoomStatus.SUCCESS"""

new_code = """        // Room does not exist on Firebase and does not exist locally -> Return ROOM_NOT_FOUND so joining a non-existent room fails properly!
        return@withContext JoinRoomStatus.ROOM_NOT_FOUND"""

if old_code in content:
    content = content.replace(old_code, new_code)
    with open('app/src/main/java/com/example/engine/RoomManager.kt', 'w') as f:
        f.write(content)
    print("Successfully patched RoomManager.kt")
else:
    print("Old code block not found in RoomManager.kt")
