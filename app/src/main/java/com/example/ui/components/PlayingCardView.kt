package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.Suit
import com.example.ui.theme.CardBackground
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardWhite
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.EmeraldFelt
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun PlayingCardView(
    card: Card,
    modifier: Modifier = Modifier,
    isTrump: Boolean = false,
    isPlayable: Boolean = true,
    isSelected: Boolean = false,
    width: Dp = 68.dp,
    height: Dp = 98.dp,
    onClick: (() -> Unit)? = null
) {
    val yOffset by animateDpAsState(
        targetValue = if (isSelected) (-16).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "card_offset"
    )

    val elevation = if (isSelected) 12.dp else if (isPlayable) 5.dp else 1.dp
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .offset(y = yOffset)
            .width(width)
            .height(height)
            .shadow(elevation, shape, spotColor = if (isTrump) GoldPrimary else Color.Black)
            .clip(shape)
            .alpha(if (isPlayable) 1f else 0.45f)
            .background(
                Brush.verticalGradient(
                    listOf(
                        CardWhite,
                        Color(0xFFFBFBFB),
                        Color(0xFFF3F4F6)
                    )
                )
            )
            .border(
                width = if (isTrump) 2.5.dp else 1.dp,
                brush = if (isTrump) Brush.linearGradient(listOf(GoldLight, GoldPrimary, GoldDark)) else Brush.linearGradient(listOf(CardBorder, Color(0xFFD1D5DB))),
                shape = shape
            )
            .clickable(enabled = isPlayable && onClick != null) {
                onClick?.invoke()
            }
            .testTag("card_${card.id}")
            .padding(horizontal = 4.dp, vertical = 3.dp)
    ) {
        // Top-left rank & suit
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.symbol,
                fontSize = (width.value * 0.22f).sp,
                fontWeight = FontWeight.Black,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.22f).sp
            )
            Text(
                text = card.suit.symbol,
                fontSize = (width.value * 0.20f).sp,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.20f).sp
            )
        }

        // Center suit icon
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.suit.symbol,
                fontSize = (width.value * 0.54f).sp,
                color = card.suit.suitColor.copy(alpha = if (card.suit.isRed) 0.95f else 0.90f),
                textAlign = TextAlign.Center
            )
        }

        // Trump star badge
        if (isTrump) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.linearGradient(listOf(GoldLight, GoldPrimary))
                    )
                    .padding(horizontal = 3.dp, vertical = 1.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = EmeraldDeep,
                        modifier = Modifier.size(8.dp)
                    )
                    Text(
                        text = "TRUMP",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = EmeraldDeep
                    )
                }
            }
        }

        // Bottom-right inverted rank & suit
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .rotate(180f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.symbol,
                fontSize = (width.value * 0.22f).sp,
                fontWeight = FontWeight.Black,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.22f).sp
            )
            Text(
                text = card.suit.symbol,
                fontSize = (width.value * 0.20f).sp,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.20f).sp
            )
        }
    }
}

@Composable
fun CardBackView(
    modifier: Modifier = Modifier,
    width: Dp = 68.dp,
    height: Dp = 98.dp
) {
    val shape = RoundedCornerShape(10.dp)

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .shadow(4.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(EmeraldSurface, EmeraldDeep, Color(0xFF04180C))
                )
            )
            .border(2.dp, Brush.linearGradient(listOf(GoldLight, GoldPrimary, GoldDark)), shape)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, GoldLight.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(EmeraldFelt, EmeraldDeep)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "🎴",
                    fontSize = (width.value * 0.32f).sp
                )
                Text(
                    text = "K-P",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldLight.copy(alpha = 0.8f),
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

