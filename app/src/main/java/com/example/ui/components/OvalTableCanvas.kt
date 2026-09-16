package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import com.example.ui.theme.*

@Composable
fun OvalTableCanvas(
    modifier: Modifier = Modifier,
    is3DMode: Boolean = false
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        // Aspect ratio 1.6:1 for 3/4 perspective
        val tableWidth = width * 0.95f
        val tableHeight = tableWidth / 1.6f
        
        val left = (width - tableWidth) / 2
        val top = (height - tableHeight) / 2
        val tableSize = Size(tableWidth, tableHeight)
        
        val tablePath = Path().apply {
            addOval(androidx.compose.ui.geometry.Rect(Offset(left, top), tableSize))
        }

        // Layer 1: Drop shadow
        val shadowSize = Size(tableWidth, tableHeight)
        drawOval(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset(left, top + 10f),
            size = shadowSize
        )

        // Layer 2: Wood rail/bumper
        val railWidth = 10f
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(WoodRail, WoodRailLight, WoodRail),
                start = Offset(left, top),
                end = Offset(left + tableWidth, top + tableHeight)
            ),
            topLeft = Offset(left - railWidth/2, top - railWidth/2),
            size = Size(tableWidth + railWidth, tableHeight + railWidth),
            style = Stroke(width = railWidth)
        )
        
        // Inner highlight on rail
        drawOval(
            color = Color.White.copy(alpha = 0.1f),
            topLeft = Offset(left - railWidth/4, top - railWidth/4),
            size = Size(tableWidth + railWidth/2, tableHeight + railWidth/2),
            style = Stroke(width = 1f)
        )

        // Layer 3: Inner felt
        clipPath(tablePath) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(FeltCenter, FeltMid, FeltEdge, FeltDeep),
                    center = Offset(width / 2, height / 2),
                    radius = tableWidth / 2
                ),
                size = size
            )
            
            // Layer 6: Subtle felt texture
            val random = java.util.Random(42)
            repeat(200) {
                val rx = random.nextFloat() * width
                val ry = random.nextFloat() * height
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = 1f + random.nextFloat() * 2f,
                    center = Offset(rx, ry)
                )
            }
        }

        // Layer 4: Stitching line
        val stitchOffset = 6f
        drawOval(
            color = GoldPrimary.copy(alpha = 0.15f),
            topLeft = Offset(left + stitchOffset, top + stitchOffset),
            size = Size(tableWidth - stitchOffset * 2, tableHeight - stitchOffset * 2),
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
            )
        )

        // Layer 5: Center emblem (very low alpha)
        val emblemSize = tableHeight * 0.4f
        val emblemLeft = width / 2 - emblemSize / 2
        val emblemTop = height / 2 - emblemSize / 2
        
        // Draw low alpha suit symbols in a cross pattern
        val suitAlpha = 0.05f
        val suitSize = emblemSize * 0.4f
        
        // Simplified emblem: "K-P" text or symbols
        // We'll draw four small circles/suits
        val center = Offset(width / 2, height / 2)
        val dist = emblemSize * 0.3f
        
        drawCircle(Color.Black.copy(alpha = suitAlpha), radius = 4f, center = center.copy(x = center.x - dist)) // Spade
        drawCircle(Color.Red.copy(alpha = suitAlpha), radius = 4f, center = center.copy(y = center.y - dist)) // Heart
        drawCircle(Color.Black.copy(alpha = suitAlpha), radius = 4f, center = center.copy(x = center.x + dist)) // Club
        drawCircle(Color.Red.copy(alpha = suitAlpha), radius = 4f, center = center.copy(y = center.y + dist)) // Diamond
    }
}
