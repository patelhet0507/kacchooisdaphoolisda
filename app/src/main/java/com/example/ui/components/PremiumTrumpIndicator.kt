package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Suit
import com.example.ui.theme.*

@Composable
fun PremiumTrumpIndicator(
    suit: Suit?,
    round: Int,
    totalRounds: Int,
    cardsInRound: Int,
    modifier: Modifier = Modifier
) {
    var isMinimized by remember { mutableStateOf(false) }
    
    val width by animateDpAsState(
        targetValue = if (isMinimized) 48.dp else 100.dp,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "width"
    )

    GlassCard(
        modifier = modifier
            .width(width)
            .clickable { isMinimized = !isMinimized }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            if (!isMinimized) {
                Text(
                    text = "TRUMP",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Trump Symbol
            Box(
                modifier = Modifier
                    .size(if (isMinimized) 32.dp else 44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = suit?.symbol ?: "?",
                    color = suit?.suitColor ?: Color.Gray,
                    fontSize = if (isMinimized) 20.sp else 28.sp,
                    fontWeight = FontWeight.Black
                )
            }

            if (!isMinimized) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "R$round/$totalRounds",
                        color = GoldLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "•",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "${cardsInRound}c",
                        color = TextLight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
