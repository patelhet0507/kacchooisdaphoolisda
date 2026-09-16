package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiPiece(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val speedY: Float,
    val speedX: Float,
    val rotation: Float,
    val rotationSpeed: Float,
    val shapeType: Int // 0: rect, 1: circle, 2: strip
)

@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    particleCount: Int = 70
) {
    val colors = listOf(
        Color(0xFFFFD700), // Gold
        Color(0xFF00E676), // Emerald
        Color(0xFFFF4081), // Pink
        Color(0xFF00B0FF), // Cyan
        Color(0xFFE040FB), // Purple
        Color(0xFFFFAB40)  // Orange
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ConfettiTransition")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ConfettiTime"
    )

    val pieces = remember {
        List(particleCount) {
            ConfettiPiece(
                x = Random.nextFloat() * 1000f,
                y = Random.nextFloat() * -600f - 50f,
                size = Random.nextFloat() * 14f + 6f,
                color = colors.random(),
                speedY = Random.nextFloat() * 3.5f + 2f,
                speedX = (Random.nextFloat() - 0.5f) * 2.5f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 12f,
                shapeType = Random.nextInt(3)
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        pieces.forEach { piece ->
            val currentY = (piece.y + (time * piece.speedY * 2.2f)) % (height + 150f) - 50f
            val currentX = (piece.x + sin((time + piece.y) * 0.04f) * 40f + (time * piece.speedX * 0.6f)) % (width + 100f) - 50f
            val currentRotation = piece.rotation + (time * piece.rotationSpeed)

            rotate(currentRotation, pivot = Offset(currentX, currentY)) {
                when (piece.shapeType) {
                    0 -> {
                        drawRect(
                            color = piece.color,
                            topLeft = Offset(currentX - piece.size / 2, currentY - piece.size / 4),
                            size = androidx.compose.ui.geometry.Size(piece.size, piece.size / 2)
                        )
                    }
                    1 -> {
                        drawCircle(
                            color = piece.color,
                            radius = piece.size / 2,
                            center = Offset(currentX, currentY)
                        )
                    }
                    else -> {
                        drawRect(
                            color = piece.color,
                            topLeft = Offset(currentX - piece.size / 4, currentY - piece.size / 2),
                            size = androidx.compose.ui.geometry.Size(piece.size / 2, piece.size)
                        )
                    }
                }
            }
        }
    }
}
