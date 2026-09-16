with open('app/src/main/java/com/example/ui/screens/MultiplayerLobbyScreen.kt', 'r') as f:
    content = f.read()

# Add necessary imports if missing
if "import android.content.Intent" not in content:
    content = content.replace("import android.content.Context", "import android.content.Context\nimport android.content.Intent")

if "import androidx.compose.material.icons.filled.Share" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.PersonRemove", "import androidx.compose.material.icons.filled.PersonRemove\nimport androidx.compose.material.icons.filled.Share\nimport androidx.compose.material.icons.filled.Check")

# Add state variable inside MultiplayerLobbyScreen
target_state_insertion = "var chatInputText by remember { mutableStateOf(\"\") }"
replacement_state = """var chatInputText by remember { mutableStateOf("") }
    var isCopiedAnimActive by remember { mutableStateOf(false) }
    LaunchedEffect(isCopiedAnimActive) {
        if (isCopiedAnimActive) {
            kotlinx.coroutines.delay(2000)
            isCopiedAnimActive = false
        }
    }"""

content = content.replace(target_state_insertion, replacement_state)

# Replace the Room Code Banner card with enhanced banner including Copy & Share buttons + animated toast feedback
old_card = """            // Room Code Banner Card
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
            }"""

new_card = """            // Room Code Banner Card with Copy & Share Utilities
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(GoldPrimary, EmeraldLight))
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Utility Action Buttons (Copy & Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Copy Code Button with Success Toast / Animation
                        Button(
                            onClick = {
                                roomCode?.let { code ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Kaachu Phool Room Code", code))
                                    isCopiedAnimActive = true
                                    Toast.makeText(context, "Room Code $code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCopiedAnimActive) EmeraldLight else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isCopiedAnimActive) EmeraldBorder else GoldPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("copy_room_code_button")
                        ) {
                            Icon(
                                imageVector = if (isCopiedAnimActive) Icons.Default.Check else Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (isCopiedAnimActive) EmeraldDeep else GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isCopiedAnimActive) "Copied!" else "Copy Code",
                                fontSize = 12.sp,
                                color = if (isCopiedAnimActive) EmeraldDeep else GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share Invite Link Button (opens app or falls back to Vercel web URL)
                        Button(
                            onClick = {
                                roomCode?.let { code ->
                                    val shareUrl = "https://kacchooisdaphoolisda.vercel.app/?room=$code"
                                    val shareText = "Join my Kaachu Phool multiplayer game! Room Code: $code\\nPlay now or download the app: $shareUrl"
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Room Invite")
                                    context.startActivity(shareIntent)
                                    Toast.makeText(context, "Share invite generated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_room_link_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = EmeraldDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Share Link",
                                fontSize = 12.sp,
                                color = EmeraldDeep,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }"""

if old_card in content:
    content = content.replace(old_card, new_card)
    with open('app/src/main/java/com/example/ui/screens/MultiplayerLobbyScreen.kt', 'w') as f:
        f.write(content)
    print("Successfully patched MultiplayerLobbyScreen.kt")
else:
    print("Old card block not found in MultiplayerLobbyScreen.kt")
