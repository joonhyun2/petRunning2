package com.example.petrunning2.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.petrunning2.ui.theme.ColorPrimaryActive

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val color = ColorPrimaryActive

        // 줄기
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.95f),
            end = Offset(w * 0.5f, h * 0.35f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 오른쪽 잎
        val rightLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.55f)
            cubicTo(w * 0.62f, h * 0.38f, w * 0.92f, h * 0.18f, w * 0.82f, h * 0.04f)
            cubicTo(w * 0.60f, h * 0.14f, w * 0.44f, h * 0.38f, w * 0.5f, h * 0.55f)
        }
        drawPath(rightLeaf, color = color)

        // 왼쪽 잎
        val leftLeaf = Path().apply {
            moveTo(w * 0.5f, h * 0.45f)
            cubicTo(w * 0.38f, h * 0.28f, w * 0.08f, h * 0.22f, w * 0.18f, h * 0.08f)
            cubicTo(w * 0.36f, h * 0.18f, w * 0.50f, h * 0.32f, w * 0.5f, h * 0.45f)
        }
        drawPath(leftLeaf, color = color)
    }
}
