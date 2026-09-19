package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Card
import com.example.model.Suit
import com.example.ui.theme.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState

@Composable
fun PlayingCardView(
    card: Card,
    modifier: Modifier = Modifier,
    isTrump: Boolean = false,
    isPlayable: Boolean = true,
    isSelected: Boolean = false,
    width: Dp = 68.dp,
    height: Dp = 98.dp,
    is3DMode: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    val isInteracting = isHovered || isPressed

    val tiltX by animateFloatAsState(
        targetValue = if (isInteracting && isPlayable) -5f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "tilt_x"
    )
    val tiltY by animateFloatAsState(
        targetValue = if (isInteracting && isPlayable) 5f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
        label = "tilt_y"
    )
    val scale by animateFloatAsState(
        targetValue = if (isInteracting && isPlayable) 1.05f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "scale"
    )

    val yOffset by animateDpAsState(
        targetValue = if (isSelected) (-20).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "card_offset"
    )
    val elevation by animateDpAsState(
        targetValue = if (isInteracting && isPlayable) 10.dp else 2.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "elevation"
    )
    val shape = RoundedCornerShape(8.dp)

    Box(
        modifier = modifier
            .offset(y = yOffset)
            .graphicsLayer {
                rotationX = tiltX
                rotationY = tiltY
                scaleX = scale
                scaleY = scale
                cameraDistance = 12f * density
            }
            .width(width)
            .height(height)
            .shadow(
                elevation = elevation,
                shape = shape,
                spotColor = if (isTrump) GoldPrimary else Color.Black
            )
            .clip(shape)
            .alpha(if (isPlayable) 1f else 0.6f)
            .background(Color.White)
            .border(
                width = if (isTrump) 2.dp else 0.5.dp,
                brush = if (isTrump) Brush.linearGradient(listOf(GoldLight, GoldPrimary, GoldDark)) else androidx.compose.ui.graphics.SolidColor(Color.LightGray.copy(alpha = 0.5f)),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isPlayable && onClick != null
            ) {
                onClick?.invoke()
            }
            .testTag("card_${card.id}")
            .padding(4.dp)
    ) {
        // Top-left rank & suit
        Column(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.rank.symbol,
                fontSize = (width.value * 0.28f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.28f).sp
            )
            Text(
                text = card.suit.symbol,
                fontSize = (width.value * 0.22f).sp,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.22f).sp
            )
        }

        // Center suit icon
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = card.suit.symbol,
                fontSize = (width.value * 0.55f).sp,
                color = card.suit.suitColor.copy(alpha = 0.95f),
                textAlign = TextAlign.Center
            )
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
                fontSize = (width.value * 0.28f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.28f).sp
            )
            Text(
                text = card.suit.symbol,
                fontSize = (width.value * 0.22f).sp,
                color = card.suit.suitColor,
                lineHeight = (width.value * 0.22f).sp
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
    val shape = RoundedCornerShape(8.dp)

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
