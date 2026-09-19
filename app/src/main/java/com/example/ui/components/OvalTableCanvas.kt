package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.ui.theme.*

@Composable
fun OvalTableCanvas(
    modifier: Modifier = Modifier,
    is3DMode: Boolean = false
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // 1. Table Shadow
        drawOval(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(4f, 12f),
            size = Size(w, h)
        )

        // 2. Outer Wooden Rail (Rich Dark Wood)
        drawOval(
            brush = Brush.verticalGradient(listOf(WoodRail, WoodRailDark)),
            size = Size(w, h)
        )

        // 3. Gold Trim (Inside the rail)
        drawOval(
            brush = Brush.linearGradient(listOf(GoldDark, GoldPrimary, GoldLight, GoldPrimary, GoldDark)),
            topLeft = Offset(12f, 12f),
            size = Size(w - 24f, h - 24f),
            style = Stroke(width = 3f)
        )

        // 4. Main Emerald Felt
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(EmeraldSurface, EmeraldDeep),
                center = center,
                radius = w * 0.55f
            ),
            topLeft = Offset(18f, 18f),
            size = Size(w - 36f, h - 36f)
        )

        // 5. Decorative Inner Stitching
        drawOval(
            color = GoldPrimary.copy(alpha = 0.1f),
            topLeft = Offset(40f, 40f),
            size = Size(w - 80f, h - 80f),
            style = Stroke(
                width = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 15f), 0f)
            )
        )
    }
}
