package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.AudioVoiceManager
import com.example.engine.RoomManager
import com.example.model.ChatMessage
import com.example.model.GameRoom
import com.example.model.VoiceNote
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MultiplayerViewModel(application: Application) : AndroidViewModel(application) {
    private val roomManager = RoomManager()
    val audioVoiceManager = AudioVoiceManager(application.applicationContext)

    private val _currentRoom = MutableStateFlow<GameRoom?>(null)
    val currentRoom: StateFlow<GameRoom?> = _currentRoom.asStateFlow()

    private val _roomCode = MutableStateFlow<String?>(null)
    val roomCode: StateFlow<String?> = _roomCode.asStateFlow()

    private val _localPlayerName = MutableStateFlow("Player 1")
    val localPlayerName: StateFlow<String> = _localPlayerName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isRoomDisbanded = MutableStateFlow(false)
    val isRoomDisbanded: StateFlow<Boolean> = _isRoomDisbanded.asStateFlow()

    private val _isKicked = MutableStateFlow(false)
    val isKicked: StateFlow<Boolean> = _isKicked.asStateFlow()

    private var roomObservationJob: Job? = null

    val isRecording = audioVoiceManager.isRecording
    val isPlaying = audioVoiceManager.isPlaying

    fun createRoom(hostName: String) {
        val sanitized = hostName.replace(Regex("[.#$\\[\\]/]"), "").trim().ifBlank { "Host" }
        _localPlayerName.value = sanitized
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val code = (100000..999999).random().toString()
                val success = roomManager.createRoom(code, sanitized)
                if (success) {
                    _roomCode.value = code
                    _currentRoom.value = com.example.model.GameRoom(
                        roomId = code,
                        hostName = sanitized,
                        players = listOf(sanitized),
                        gameState = "WAITING"
                    )
                    observeRoom(code)
                    Log.d("MultiplayerViewModel", "Room created successfully: $code")
                } else {
                    _errorMessage.value = "Failed to create room. Please try again."
                }
            } catch (e: Exception) {
                Log.e("MultiplayerViewModel", "Error creating room", e)
                _errorMessage.value = "Error: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinRoom(roomId: String, playerName: String) {
        val cleanRoomId = roomId.trim()
        if (cleanRoomId.length != 6) {
            _errorMessage.value = "Room code must be 6 digits"
            return
        }

        val sanitizedName = playerName.replace(Regex("[.#$\\[\\]/]"), "").trim().ifBlank { "Player" }
        _localPlayerName.value = sanitizedName
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                when (val result = roomManager.joinRoom(cleanRoomId, sanitizedName)) {
                    com.example.engine.JoinRoomStatus.SUCCESS -> {
                        _roomCode.value = cleanRoomId
                        val existingRoom = roomManager.getOrCreateLocalFlow(cleanRoomId).value
                        _currentRoom.value = existingRoom ?: com.example.model.GameRoom(
                            roomId = cleanRoomId,
                            hostName = sanitizedName,
                            players = listOf(sanitizedName),
                            gameState = "WAITING"
                        )
                        observeRoom(cleanRoomId)
                        Log.d("MultiplayerViewModel", "Joined room: $cleanRoomId")
                    }
                    com.example.engine.JoinRoomStatus.ROOM_FULL -> {
                        _errorMessage.value = "Room $cleanRoomId is full! Maximum 6 players reached."
                    }
                    com.example.engine.JoinRoomStatus.ROOM_NOT_FOUND -> {
                        _errorMessage.value = "Room $cleanRoomId was not found. Please check the 6-digit code or create a room."
                    }
                    com.example.engine.JoinRoomStatus.ERROR -> {
                        _errorMessage.value = "Could not connect to room $cleanRoomId. Please check your network."
                    }
                }
            } catch (e: Exception) {
                Log.e("MultiplayerViewModel", "Error joining room", e)
                _errorMessage.value = "Error: ${e.localizedMessage ?: "Could not join"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeRoom(roomId: String) {
        _isRoomDisbanded.value = false
        _isKicked.value = false
        roomObservationJob?.cancel()
        roomObservationJob = viewModelScope.launch {
            roomManager.getRoomUpdates(roomId).collect { room ->
                if (room == null || room.gameState == "DISBANDED") {
                    val hadActiveRoom = _currentRoom.value != null
                    if (hadActiveRoom) {
                        _isRoomDisbanded.value = true
                    }
                    _currentRoom.value = null
                } else {
                    val myName = _localPlayerName.value
                    val isKickedFromList = room.kickedPlayers.contains(myName)
                    if (isKickedFromList) {
                        _isKicked.value = true
                        _currentRoom.value = null
                        return@collect
                    }
                    _currentRoom.value = room
                }
            }
        }
    }

    fun kickPlayer(playerName: String) {
        val code = _roomCode.value ?: return
        viewModelScope.launch {
            roomManager.kickPlayerFromRoom(code, playerName)
        }
    }

    fun addBot(botName: String) {
        val code = _roomCode.value ?: return
        viewModelScope.launch {
            roomManager.addBotToRoom(code, botName)
        }
    }

    fun sendChatMessage(text: String) {
        val code = _roomCode.value ?: return
        if (text.isBlank()) return
        val sender = _localPlayerName.value
        val message = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderName = sender,
            text = text.trim(),
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            roomManager.sendChatMessage(code, message)
        }
    }

    fun startVoiceRecording(): Boolean {
        val code = _roomCode.value ?: return false
        val started = audioVoiceManager.startRecording()
        if (started) {
            viewModelScope.launch {
                roomManager.setSpeakerStatus(code, _localPlayerName.value, true)
            }
        }
        return started
    }

    fun stopVoiceRecordingAndSend() {
        val code = _roomCode.value ?: return
        viewModelScope.launch {
            roomManager.setSpeakerStatus(code, _localPlayerName.value, false)
            val result = audioVoiceManager.stopRecording()
            if (result != null) {
                val (base64Audio, duration) = result
                val note = VoiceNote(
                    id = "voice_${System.currentTimeMillis()}",
                    senderName = _localPlayerName.value,
                    audioBase64 = base64Audio,
                    durationMs = duration,
                    timestamp = System.currentTimeMillis()
                )
                roomManager.sendVoiceNote(code, note)
            }
        }
    }

    fun playVoiceNote(note: VoiceNote) {
        audioVoiceManager.playVoiceAudio(note.audioBase64)
    }

    fun startMultiplayerMatch(
        gameMode: com.example.model.GameMode = com.example.model.GameMode.QUICK,
        scoringRule: com.example.model.ScoringRule = com.example.model.ScoringRule.STANDARD
    ) {
        val code = _roomCode.value ?: return
        viewModelScope.launch {
            roomManager.startMultiplayerMatch(code, gameMode, scoringRule)
        }
    }

    fun leaveRoom() {
        val code = _roomCode.value
        val player = _localPlayerName.value
        if (!code.isNullOrBlank() && player.isNotBlank()) {
            viewModelScope.launch {
                try {
                    roomManager.leaveRoom(code, player)
                } catch (e: Exception) {
                    Log.w("MultiplayerViewModel", "Error in leaveRoom: ${e.message}")
                }
            }
        }
        roomObservationJob?.cancel()
        audioVoiceManager.release()
        _currentRoom.value = null
        _roomCode.value = null
        _errorMessage.value = null
        _isRoomDisbanded.value = false
    }

    fun acknowledgeDisband() {
        _isRoomDisbanded.value = false
        _isKicked.value = false
        _currentRoom.value = null
        _roomCode.value = null
        _errorMessage.value = null
    }

    fun acknowledgeKicked() {
        _isKicked.value = false
        _isRoomDisbanded.value = false
        _currentRoom.value = null
        _roomCode.value = null
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        leaveRoom()
        roomObservationJob?.cancel()
        audioVoiceManager.release()
    }
}
