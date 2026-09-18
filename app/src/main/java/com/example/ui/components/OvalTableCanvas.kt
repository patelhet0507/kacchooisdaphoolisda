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

        // Layer 2: Rail/bumper (Darker for poker style)
        val railWidth = 14f
        drawOval(
            brush = Brush.linearGradient(
                colors = listOf(WoodRailDark, WoodRail, WoodRailDark),
                start = Offset(left, top),
                end = Offset(left + tableWidth, top + tableHeight)
            ),
            topLeft = Offset(left - railWidth/2, top - railWidth/2),
            size = Size(tableWidth + railWidth, tableHeight + railWidth),
            style = Stroke(width = railWidth)
        )
        
        // Inner highlight on rail
        drawOval(
            color = Color.White.copy(alpha = 0.08f),
            topLeft = Offset(left - railWidth/3, top - railWidth/3),
            size = Size(tableWidth + railWidth/1.5f, tableHeight + railWidth/1.5f),
            style = Stroke(width = 1.5f)
        )

        // Layer 3: Inner felt
        clipPath(tablePath) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(FeltCenter, FeltMid, FeltEdge, FeltDeep),
                    center = Offset(width / 2, height / 2),
                    radius = tableWidth * 0.7f
                ),
                size = size
            )
            
            // Subtle felt texture pattern
            val random = java.util.Random(42)
            repeat(300) {
                val rx = random.nextFloat() * width
                val ry = random.nextFloat() * height
                drawCircle(
                    color = Color.White.copy(alpha = 0.02f),
                    radius = 0.8f + random.nextFloat() * 1.5f,
                    center = Offset(rx, ry)
                )
            }

            // Layer 4: Primary Stitching line (Outer)
            val stitchOffset = 12f
            drawOval(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(left + stitchOffset, top + stitchOffset),
                size = Size(tableWidth - stitchOffset * 2, tableHeight - stitchOffset * 2),
                style = Stroke(
                    width = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )

            // Layer 5: Inner Play Area Rectangle (from image)
            val rectWidth = tableWidth * 0.55f
            val rectHeight = tableHeight * 0.45f
            val rectLeft = width / 2 - rectWidth / 2
            val rectTop = height / 2 - rectHeight / 2
            
            drawRoundRect(
                color = Color.White.copy(alpha = 0.15f),
                topLeft = Offset(rectLeft, rectTop),
                size = Size(rectWidth, rectHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f),
                style = Stroke(
                    width = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                )
            )
        }

        // Layer 6: Dealer Position (Top Center)
        val dealerWidth = 80f
        val dealerHeight = 30f
        val dealerRect = androidx.compose.ui.geometry.Rect(
            Offset(width / 2 - dealerWidth / 2, top - 10f),
            Size(dealerWidth, dealerHeight)
        )
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.5f),
            topLeft = dealerRect.topLeft,
            size = dealerRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f)
        )
        drawRoundRect(
            color = GoldPrimary.copy(alpha = 0.4f),
            topLeft = dealerRect.topLeft,
            size = dealerRect.size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f),
            style = Stroke(width = 1.5f)
        )
        
        // Add "DEALER" text or just a visual hint?
        // Since I can't draw text easily in DrawScope without native canvas, I'll use a small white dash pattern inside
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = 3f,
            center = Offset(width / 2, top + 5f)
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
