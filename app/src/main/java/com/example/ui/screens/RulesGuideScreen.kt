package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Suit
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldFelt
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextLight
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesGuideScreen(
    onBackClick: () -> Unit
) {
    BackHandler {
        onBackClick()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "How to Play Kaachu Phool",
                        color = GoldLight,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("rules_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = GoldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Overview Card
                GuideSectionCard(
                    title = "The Origin & Meaning: K-A-C-H-U-F-U-L",
                    icon = Icons.Default.Star,
                    iconColor = GoldPrimary
                ) {
                    Text(
                        text = "Kaachu Phool (also called Kachuful or Judgement) is an iconic Indian trick-taking card game. The name is a Gujarati mnemonic for the strict rotation of trump suits:",
                        color = TextLight,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MnemonicCard(code = "Ka", name = "Kali", suit = "Spades", symbol = "♠", isRed = false)
                        MnemonicCard(code = "Chu", name = "Chokat", suit = "Diamonds", symbol = "♦", isRed = true)
                        MnemonicCard(code = "Fu", name = "Fuli", suit = "Clubs", symbol = "♣", isRed = false)
                        MnemonicCard(code = "L", name = "Laal", suit = "Hearts", symbol = "♥", isRed = true)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "In each round, the trump suit cycles sequentially: Spades ♠ → Diamonds ♦ → Clubs ♣ → Hearts ♥, then loops back!",
                        color = GoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                // The Hook Rule
                GuideSectionCard(
                    title = "The Hook Rule (Bandi / Nasti)",
                    icon = Icons.Default.Block,
                    iconColor = ErrorRed
                ) {
                    Text(
                        text = "The dealer always bids last. A golden rule of Kaachu Phool states:\n\n" +
                                "The dealer CANNOT bid a number that causes the total bids of all players to equal the total cards dealt in that round.",
                        color = TextLight,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ErrorRed.copy(alpha = 0.12f))
                            .border(1.dp, ErrorRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Example (5 cards dealt):",
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Player 1 bids 1, Player 2 bids 2, Player 3 bids 1.\n" +
                                        "Sum of bids = 4.\n" +
                                        "Dealer CANNOT bid 1 (because 4 + 1 = 5 cards)!\n" +
                                        "Dealer must bid 0, 2, 3, 4, or 5.",
                                color = TextLight,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Why? This ensures that at least one player will always fail their bid, keeping the game thrilling and unpredictable!",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            item {
                // Game Structure & Rounds
                GuideSectionCard(
                    title = "Game Structure & Rounds",
                    icon = Icons.Default.CheckCircle,
                    iconColor = EmeraldLight
                ) {
                    Text(
                        text = "The game is played in a descending and ascending pyramid pattern:",
                        color = TextLight,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        RoundTypeRow(title = "Classic Mode", desc = "8 → 7 → 6 → ... → 1 card, then back up 2 → ... → 8 cards (15 rounds)")
                        RoundTypeRow(title = "Quick Match", desc = "5 → 4 → 3 → 2 → 1, then back up to 5 cards (9 rounds)")
                        RoundTypeRow(title = "Tournament", desc = "10 down to 1, then up to 10 cards (19 rounds)")
                    }
                }
            }

            item {
                // Trick Taking Rules
                GuideSectionCard(
                    title = "Trick-Taking Rules",
                    icon = Icons.Default.Lightbulb,
                    iconColor = GoldPrimary
                ) {
                    Text(
                        text = "1. Lead: The player to the left of the dealer leads the first trick with any card.\n\n" +
                                "2. Must Follow Suit: Players must play a card of the lead suit if they hold one.\n\n" +
                                "3. Voiding / Cutting: If a player has no cards of the lead suit, they may play ANY card—including a Trump card to cut and win, or discard an unwanted card.\n\n" +
                                "4. Who Wins: The highest trump played wins the trick. If no trumps were played, the highest card of the lead suit wins.\n\n" +
                                "5. Hierarchy: Ace (highest) down to 2 (lowest).",
                        color = TextLight,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }

            item {
                // Scoring
                GuideSectionCard(
                    title = "Scoring (10 + Bid)",
                    icon = Icons.Default.Star,
                    iconColor = SuccessGreen
                ) {
                    Text(
                        text = "Accuracy is everything in Kaachu Phool:\n\n" +
                                "• Exact Bid Made: You score 10 points + your bid.\n" +
                                "   - Bid 0 and won 0 tricks = 10 points.\n" +
                                "   - Bid 4 and won 4 tricks = 14 points.\n\n" +
                                "• Failed Bid: If you win even ONE trick more or less than your bid, you score 0 points for that round!\n\n" +
                                "The player with the highest cumulative score at the end of all rounds wins!",
                        color = TextLight,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun GuideSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(EmeraldBorder.copy(alpha = 0.5f))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = title,
                    color = GoldLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun MnemonicCard(
    code: String,
    name: String,
    suit: String,
    symbol: String,
    isRed: Boolean
) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, EmeraldBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = symbol,
                fontSize = 22.sp,
                color = if (isRed) Color(0xFFEF4444) else TextLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = code,
                color = GoldPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = name,
                color = TextLight,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = suit,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun RoundTypeRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceElevated)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(text = "•", color = TextMuted, fontSize = 10.sp)
        Text(text = desc, color = TextMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
    }
}
