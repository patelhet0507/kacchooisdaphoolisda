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

    private var roomObservationJob: Job? = null

    val isRecording = audioVoiceManager.isRecording
    val isPlaying = audioVoiceManager.isPlaying

    fun createRoom(hostName: String) {
        val trimmed = hostName.ifBlank { "Host" }
        _localPlayerName.value = trimmed
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val code = (100000..999999).random().toString()
                _roomCode.value = code
                observeRoom(code)
                val success = roomManager.createRoom(code, trimmed)
                if (success) {
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

        val trimmedName = playerName.ifBlank { "Player" }
        _localPlayerName.value = trimmedName
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                when (val result = roomManager.joinRoom(cleanRoomId, trimmedName)) {
                    com.example.engine.JoinRoomStatus.SUCCESS -> {
                        _roomCode.value = cleanRoomId
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
        roomObservationJob?.cancel()
        roomObservationJob = viewModelScope.launch {
            roomManager.getRoomUpdates(roomId).collect { room ->
                _currentRoom.value = room
            }
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
        roomObservationJob?.cancel()
        audioVoiceManager.release()
        _currentRoom.value = null
        _roomCode.value = null
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioVoiceManager.release()
    }
}
