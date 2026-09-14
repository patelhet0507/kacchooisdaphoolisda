package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Emote
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldDeep
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun EmotePickerBar(
    onEmoteSelected: (Emote) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Toggle Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .background(if (isExpanded) GoldPrimary else DarkSurfaceElevated)
                .border(
                    width = 1.5.dp,
                    color = if (isExpanded) GoldLight else EmeraldBorder,
                    shape = CircleShape
                )
                .clickable { isExpanded = !isExpanded }
                .testTag("emote_toggle_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isExpanded) "✖" else "🎭",
                fontSize = if (isExpanded) 14.sp else 20.sp,
                color = if (isExpanded) EmeraldDeep else Color.Unspecified,
                fontWeight = FontWeight.Bold
            )
        }

        // Expanded Reactions Bar
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandHorizontally(spring(stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut() + shrinkHorizontally(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            Row(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurfaceElevated.copy(alpha = 0.95f))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Emote.values().forEach { emote ->
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable {
                                onEmoteSelected(emote)
                                isExpanded = false
                            }
                            .testTag("emote_${emote.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emote.emoji,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmoteBubbleView(
    emoteEmoji: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = emoteEmoji.isNotBlank(),
        enter = fadeIn() + scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = modifier
                .shadow(10.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceElevated.copy(alpha = 0.95f))
                .border(1.5.dp, GoldPrimary, RoundedCornerShape(16.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoteEmoji,
                fontSize = 24.sp
            )
        }
    }
}
