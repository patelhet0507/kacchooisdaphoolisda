package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ChatMessage
import com.example.model.GameMode
import com.example.model.ScoringRule
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldFelt
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.viewmodel.MultiplayerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplayerLobbyScreen(
    viewModel: MultiplayerViewModel,
    onStartGame: (userName: String, mode: GameMode, scoringRule: ScoringRule, playerCount: Int) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val roomCode by viewModel.roomCode.collectAsStateWithLifecycle()

    // Intercept hardware / system back gesture to cleanly leave room
    BackHandler {
        viewModel.leaveRoom()
        onBackClick()
    }
    val currentRoom by viewModel.currentRoom.collectAsStateWithLifecycle()
    val isRoomDisbanded by viewModel.isRoomDisbanded.collectAsStateWithLifecycle()
    val isKicked by viewModel.isKicked.collectAsStateWithLifecycle()
    val localPlayerName by viewModel.localPlayerName.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    var chatInputText by remember { mutableStateOf("") }
    var playerToKick by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            Toast.makeText(context, "Microphone permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Mic permission needed for Voice Chat", Toast.LENGTH_SHORT).show()
        }
    }

    var selectedMode by remember { mutableStateOf(GameMode.QUICK) }
    var selectedScoringRule by remember { mutableStateOf(ScoringRule.STANDARD) }

    val room = currentRoom
    val isHost = room?.hostName == localPlayerName || (room?.players?.firstOrNull() == localPlayerName)
    val players = room?.players ?: listOf(localPlayerName)
    val messages = remember(room?.messages) {
        room?.messages?.values?.sortedBy { it.timestamp } ?: emptyList()
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // If host starts game, navigate all players
    LaunchedEffect(room?.gameState) {
        if (room?.gameState in listOf("BIDDING", "PLAYING", "TRICK_FINISHED", "ROUND_FINISHED")) {
            val mode = try {
                GameMode.valueOf(room?.gameMode ?: "QUICK")
            } catch (e: Exception) {
                selectedMode
            }
            val rule = try {
                ScoringRule.valueOf(room?.scoringRule ?: "STANDARD")
            } catch (e: Exception) {
                selectedScoringRule
            }
            onStartGame(localPlayerName, mode, rule, players.size)
        }
    }

    if (isRoomDisbanded) {
        AlertDialog(
            onDismissRequest = {
                viewModel.acknowledgeDisband()
                onBackClick()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Room Disbanded",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "The host has left or closed the room. You are being returned to the home screen.",
                    color = TextLight,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.acknowledgeDisband()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("Return to Home", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (isKicked) {
        AlertDialog(
            onDismissRequest = {
                viewModel.acknowledgeKicked()
                onBackClick()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Removed from Room",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "You have been removed from the room by the host.",
                    color = TextLight,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.acknowledgeKicked()
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text("OK", color = EmeraldDeep, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (playerToKick != null) {
        val target = playerToKick!!
        AlertDialog(
            onDismissRequest = { playerToKick = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PersonRemove,
                        contentDescription = null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kick Player?",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to kick $target from the room?",
                    color = TextLight,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.kickPlayer(target)
                        playerToKick = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Kick Player", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { playerToKick = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Multiplayer Lobby",
                            color = GoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Room: ${roomCode ?: "Connecting..."}",
                            color = EmeraldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.leaveRoom()
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Leave Room",
                            tint = TextLight
                        )
                    }
                },
                actions = {
                    // Copy Room Code Button
                    IconButton(onClick = {
                        roomCode?.let { code ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Kaachu Phool Room Code", code))
                            Toast.makeText(context, "Room Code $code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Code",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Room Code Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(GoldPrimary, EmeraldLight))
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ROOM CODE",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = roomCode ?: "------",
                            color = GoldLight,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp
                        )
                        Text(
                            text = "${players.size}/6 Players Joined",
                            color = if (players.size >= 4) EmeraldLight else GoldPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (isHost && players.size < 6) {
                            Button(
                                onClick = {
                                    val botNames = listOf("Bot Aarav", "Bot Priya", "Bot Rohan", "Bot Ananya", "Bot Kabir")
                                    val nextBot = botNames.firstOrNull { !players.contains(it) } ?: "Bot Player"
                                    viewModel.addBot(nextBot)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_bot_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = TextLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Bot", fontSize = 12.sp, color = TextLight)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Players Grid / List
            Text(
                text = "PLAYERS IN ROOM",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(players) { playerName ->
                    val isPlayerHost = playerName == room?.hostName || playerName == players.firstOrNull()
                    val isSpeaking = room?.activeSpeakers?.get(playerName) == true
                    val canKickPlayer = isHost && !isPlayerHost && playerName != localPlayerName
                    PlayerLobbyChip(
                        name = playerName,
                        isHost = isPlayerHost,
                        isSelf = playerName == localPlayerName,
                        isSpeaking = isSpeaking,
                        canKick = canKickPlayer,
                        onKickClick = { playerToKick = playerName }
                    )
                }

                // Empty slots
                val emptySlots = (6 - players.size).coerceAtLeast(0)
                items(emptySlots) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(72.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated.copy(alpha = 0.5f))
                            .border(1.dp, EmeraldBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Waiting...",
                            color = TextMuted,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // In-App Voice Chat Bar
            VoiceChatBar(
                isRecording = isRecording,
                isPlaying = isPlaying,
                hasMicPermission = hasMicPermission,
                onRequestMicPermission = {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                onStartRecording = {
                    if (hasMicPermission) {
                        viewModel.startVoiceRecording()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecording = {
                    viewModel.stopVoiceRecordingAndSend()
                },
                onQuickVoiceReaction = { text ->
                    viewModel.sendChatMessage("🎙️ $text")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Messages Feed
            Text(
                text = "IN-ROOM LIVE CHAT",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(listOf(EmeraldBorder.copy(alpha = 0.5f), DarkSurface))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No messages yet. Say hi to your fellow players!",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            items(messages) { message ->
                                ChatBubble(
                                    message = message,
                                    isSelf = message.senderName == localPlayerName,
                                    onPlayVoiceNote = {
                                        room?.voiceNotes?.get(message.id)?.let { note ->
                                            viewModel.playVoiceNote(note)
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Chat Input Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("Type a message...", color = TextMuted, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_field"),
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = EmeraldBorder,
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated
                            )
                        )

                        IconButton(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    viewModel.sendChatMessage(chatInputText)
                                    chatInputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldPrimary)
                                .testTag("send_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = EmeraldDeep,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Host Start Game / Waiting Button
            if (isHost) {
                Button(
                    onClick = {
                        viewModel.startMultiplayerMatch(selectedMode, selectedScoringRule)
                        onStartGame(localPlayerName, selectedMode, selectedScoringRule, players.size)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("host_start_game_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = EmeraldDeep,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (players.size >= 2) "Start Match (${players.size} Players)" else "Waiting for Players to Join...",
                        color = EmeraldDeep,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(EmeraldBorder, GoldPrimary.copy(alpha = 0.5f)))
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = GoldPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Waiting for host to start the game...",
                            color = GoldLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerLobbyChip(
    name: String,
    isHost: Boolean,
    isSelf: Boolean,
    isSpeaking: Boolean,
    canKick: Boolean = false,
    onKickClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speaking_wave")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isSpeaking) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSpeaking -> EmeraldLight
            isHost -> GoldPrimary
            else -> EmeraldBorder
        },
        label = "border_color"
    )

    Box(
        modifier = Modifier
            .width(86.dp)
            .height(76.dp)
            .scale(if (isSpeaking) pulseScale else 1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelf) DarkSurfaceElevated else DarkSurface)
            .border(if (isSpeaking) 2.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isSpeaking) EmeraldLight.copy(alpha = 0.3f) else GoldPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (isSpeaking) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Speaking",
                        tint = EmeraldLight,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isHost) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Host",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = name.take(1).uppercase(),
                        color = TextLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isSelf) "$name (You)" else name,
                color = if (isSpeaking) EmeraldLight else if (isHost) GoldLight else TextLight,
                fontSize = 10.sp,
                fontWeight = if (isHost || isSelf) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }

        if (canKick) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { onKickClick() }
                    .testTag("kick_player_button_$name"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kick $name",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceChatBar(
    isRecording: Boolean,
    isPlaying: Boolean,
    hasMicPermission: Boolean,
    onRequestMicPermission: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onQuickVoiceReaction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(EmeraldBorder, GoldPrimary.copy(alpha = 0.4f)))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = null,
                        tint = if (isRecording) Color(0xFFEF4444) else EmeraldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRecording) "RECORDING VOICE NOTE..." else if (isPlaying) "PLAYING VOICE AUDIO..." else "IN-APP VOICE CHAT",
                        color = if (isRecording) Color(0xFFEF4444) else GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Push To Talk Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isRecording) Color(0xFFEF4444) else EmeraldDeep)
                        .border(1.dp, if (isRecording) Color.Red else EmeraldLight, RoundedCornerShape(12.dp))
                        .pointerInput(hasMicPermission) {
                            detectTapGestures(
                                onPress = {
                                    if (!hasMicPermission) {
                                        onRequestMicPermission()
                                    } else {
                                        onStartRecording()
                                        tryAwaitRelease()
                                        onStopRecording()
                                    }
                                }
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("push_to_talk_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Hold to Speak",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isRecording) "Release to Send" else "Hold to Speak",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Voice & Reaction Phrases
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val quickPhrases = listOf(
                    "Good Luck! 🍀",
                    "Kaachu Phool! 🌸",
                    "♠ Kali Trump!",
                    "♦ Chokat Trump!",
                    "♣ Fuli Trump!",
                    "♥ Laal Trump!",
                    "Nice Trick! 👏",
                    "Watch out Dealer Hook! 🪝"
                )
                items(quickPhrases) { phrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, EmeraldBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .clickable { onQuickVoiceReaction(phrase) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = phrase,
                            color = TextLight,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isSelf: Boolean,
    onPlayVoiceNote: () -> Unit
) {
    val isSystem = message.isSystem
    val isVoice = message.text.startsWith("🎤")

    if (isSystem) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message.text,
                color = EmeraldLight.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomStart = if (isSelf) 12.dp else 2.dp,
                            bottomEnd = if (isSelf) 2.dp else 12.dp
                        )
                    )
                    .background(if (isSelf) GoldPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated)
                    .border(
                        1.dp,
                        if (isSelf) GoldPrimary.copy(alpha = 0.5f) else EmeraldBorder.copy(alpha = 0.4f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column {
                    Text(
                        text = message.senderName,
                        color = if (isSelf) GoldLight else EmeraldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    if (isVoice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onPlayVoiceNote() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Play voice note",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = message.text,
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(
                            text = message.text,
                            color = TextLight,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
