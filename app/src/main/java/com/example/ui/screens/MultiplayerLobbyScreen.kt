package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ChatMessage
import com.example.model.GameMode
import com.example.model.ScoringRule
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DeepEmerald
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldFelt
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted
import com.example.ui.components.OvalTableCanvas
import com.example.ui.components.PremiumButton
import com.example.ui.components.GlassCard
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
    var isCopiedAnimActive by remember { mutableStateOf(false) }
    LaunchedEffect(isCopiedAnimActive) {
        if (isCopiedAnimActive) {
            kotlinx.coroutines.delay(2000)
            isCopiedAnimActive = false
        }
    }
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
        containerColor = DeepEmerald,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MULTIPLAYER LOBBY",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "ROOM: ${roomCode ?: "CONNECTING..."}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
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
                            tint = GoldPrimary
                        )
                    }
                },
                actions = {
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Room Header
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ROOM CODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = roomCode ?: "------",
                            style = MaterialTheme.typography.headlineLarge,
                            color = GoldLight,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "${players.size}/6 PLAYERS JOINED",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (players.size >= 4) SuccessGreen else GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (isHost && players.size < 6) {
                        PremiumButton(
                            text = "ADD BOT",
                            isPrimary = false,
                            onClick = {
                                val botNames = listOf("Bot Aarav", "Bot Priya", "Bot Rohan", "Bot Ananya", "Bot Kabir")
                                val nextBot = botNames.firstOrNull { !players.contains(it) } ?: "Bot Player"
                                viewModel.addBot(nextBot)
                            },
                            modifier = Modifier.width(110.dp),
                            icon = { Icon(Icons.Default.PersonAdd, null, tint = GoldPrimary, modifier = Modifier.size(14.dp)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PremiumButton(
                        text = if (isCopiedAnimActive) "COPIED" else "COPY CODE",
                        isPrimary = false,
                        onClick = {
                            roomCode?.let { code ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Kaachu Phool Room Code", code))
                                isCopiedAnimActive = true
                                Toast.makeText(context, "Room Code $code copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        icon = { Icon(if (isCopiedAnimActive) Icons.Default.Check else Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp)) }
                    )

                    PremiumButton(
                        text = "INVITE",
                        onClick = {
                            roomCode?.let { code ->
                                val shareUrl = "https://kacchooisdaphoolisda.vercel.app/?room=$code"
                                val shareText = "Join my Kaachu Phool multiplayer game! Room Code: $code\nPlay now: $shareUrl"
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Room Invite"))
                            }
                        },
                        modifier = Modifier.weight(1f),
                        icon = { Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp)) }
                    )
                }
            }

            // Table Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.03f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                OvalTableCanvas(modifier = Modifier.fillMaxSize(), is3DMode = false)
                
                // Room info in middle of table
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "TABLE",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = roomCode ?: "...",
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight,
                        fontWeight = FontWeight.Black
                    )
                }

                // Players around table
                val totalSlots = 6
                for (i in 0 until totalSlots) {
                    val playerName = players.getOrNull(i)
                    val isPlayerHost = playerName != null && (playerName == room?.hostName || playerName == players.firstOrNull())
                    val isSpeaking = playerName != null && room?.activeSpeakers?.get(playerName) == true
                    val isSelf = playerName != null && playerName == localPlayerName
                    
                    val angle = (i * (360f / totalSlots)) - 90f
                    val radiusX = 140.dp
                    val radiusY = 75.dp
                    
                    val xOffset = (kotlin.math.cos(Math.toRadians(angle.toDouble())) * radiusX.value).dp
                    val yOffset = (kotlin.math.sin(Math.toRadians(angle.toDouble())) * radiusY.value).dp
                    
                    Box(modifier = Modifier.offset(x = xOffset, y = yOffset)) {
                        if (playerName != null) {
                            PlayerLobbyChip(
                                name = playerName,
                                isHost = isPlayerHost,
                                isSelf = isSelf,
                                isSpeaking = isSpeaking,
                                canKick = isHost && !isPlayerHost && !isSelf,
                                onKickClick = { playerToKick = playerName }
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, GoldPrimary.copy(alpha = 0.05f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("+", color = TextMuted, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // Chat & Voice area
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Voice Bar
                    VoiceChatBar(
                        isRecording = isRecording,
                        isPlaying = isPlaying,
                        hasMicPermission = hasMicPermission,
                        onRequestMicPermission = { micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onStartRecording = { if (hasMicPermission) viewModel.startVoiceRecording() else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                        onStopRecording = { viewModel.stopVoiceRecordingAndSend() },
                        onQuickVoiceReaction = { viewModel.sendChatMessage("🎙️ $it") }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chat List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        if (messages.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("NO MESSAGES YET", style = MaterialTheme.typography.labelSmall, color = TextMuted, letterSpacing = 2.sp)
                                }
                            }
                        } else {
                            items(messages) { message ->
                                ChatBubble(
                                    message = message,
                                    isSelf = message.senderName == localPlayerName,
                                    onPlayVoiceNote = { room?.voiceNotes?.get(message.id)?.let { viewModel.playVoiceNote(it) } }
                                )
                            }
                        }
                    }

                    // Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("TYPE MESSAGE...", style = MaterialTheme.typography.labelSmall, color = TextMuted) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = GoldPrimary.copy(alpha = 0.2f),
                                focusedTextColor = TextLight,
                                unfocusedTextColor = TextLight,
                                cursorColor = GoldPrimary,
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.02f)
                            )
                        )

                        IconButton(
                            onClick = { if (chatInputText.isNotBlank()) { viewModel.sendChatMessage(chatInputText); chatInputText = "" } },
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(GoldPrimary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, null, tint = DeepEmerald, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Start Game Footer
            if (isHost) {
                PremiumButton(
                    text = if (players.size >= 2) "START MATCH (${players.size} PLAYERS)" else "WAITING FOR PLAYERS...",
                    onClick = {
                        viewModel.startMultiplayerMatch(selectedMode, selectedScoringRule)
                        onStartGame(localPlayerName, selectedMode, selectedScoringRule, players.size)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("WAITING FOR HOST TO START...", style = MaterialTheme.typography.labelLarge, color = GoldLight, letterSpacing = 1.sp)
                    }
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
        targetValue = if (isSpeaking) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                2.dp,
                when {
                    isSpeaking -> SuccessGreen
                    isHost -> GoldPrimary
                    isSelf -> GoldLight.copy(alpha = 0.5f)
                    else -> Color.White.copy(alpha = 0.1f)
                },
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isHost) GoldPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                if (isHost) {
                    Icon(Icons.Default.Star, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                } else {
                    Text(name.take(1).uppercase(), color = GoldLight, fontWeight = FontWeight.Black, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isSelf) "YOU" else name.uppercase(),
                color = if (isHost) GoldLight else TextLight,
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (canKick) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .clickable { onKickClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(12.dp))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, GoldPrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(12.dp)
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
                    tint = if (isRecording) Color.Red else SuccessGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRecording) "RECORDING..." else if (isPlaying) "PLAYING..." else "VOICE CHAT",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldLight,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isRecording) Color.Red.copy(alpha = 0.2f) else GoldPrimary.copy(alpha = 0.1f))
                    .border(1.dp, if (isRecording) Color.Red else GoldPrimary, RoundedCornerShape(8.dp))
                    .pointerInput(hasMicPermission) {
                        detectTapGestures(
                            onPress = {
                                if (!hasMicPermission) onRequestMicPermission()
                                else {
                                    onStartRecording()
                                    tryAwaitRelease()
                                    onStopRecording()
                                }
                            }
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Mic, null, tint = if (isRecording) Color.Red else GoldPrimary, modifier = Modifier.size(14.dp))
                    Text(
                        text = if (isRecording) "RELEASE" else "HOLD TO SPEAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isRecording) Color.Red else GoldLight,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            val quickPhrases = listOf("Good Luck! 🍀", "Kaachu Phool! 🌸", "Nice Trick! 👏", "Bad luck! 😅")
            items(quickPhrases) { phrase ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { onQuickVoiceReaction(phrase) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(phrase, color = TextLight, fontSize = 9.sp, fontWeight = FontWeight.Bold)
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
    val isVoice = message.text.startsWith("🎙️") || message.text.startsWith("🎤")

    if (isSystem) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Text(
                text = message.text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = GoldPrimary.copy(alpha = 0.6f),
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
        ) {
            Column(horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start) {
                Text(
                    text = message.senderName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelf) GoldPrimary else SuccessGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(
                            topStart = if (isSelf) 12.dp else 2.dp,
                            topEnd = if (isSelf) 2.dp else 12.dp,
                            bottomStart = 12.dp,
                            bottomEnd = 12.dp
                        ))
                        .background(if (isSelf) GoldPrimary.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.05f))
                        .border(1.dp, if (isSelf) GoldPrimary.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .clickable(enabled = isVoice) { if (isVoice) onPlayVoiceNote() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isVoice) Icon(Icons.AutoMirrored.Filled.VolumeUp, null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = message.text,
                            color = if (isVoice) GoldLight else TextLight,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isVoice) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
