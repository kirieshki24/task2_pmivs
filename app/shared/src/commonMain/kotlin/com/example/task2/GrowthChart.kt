package com.example.task2

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp

@Composable
fun GrowthChart(
    values: List<Double>,
    yearLabel: String,
    amountLabel: String,
    currency: String,
    modifier: Modifier = Modifier,
    showGradient: Boolean = false,
    lineWidthDp: Float = 3f,
    interactive: Boolean = false
) {
    if (values.size < 2) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface

    var animTarget by remember(values) { mutableStateOf(0f) }
    LaunchedEffect(values) { animTarget = 1f }
    val animProgress by animateFloatAsState(
        targetValue = animTarget,
        animationSpec = tween(durationMillis = 1200),
        label = "chart_anim"
    )

    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        val paddingLeft = 80f
        val paddingBottom = 50f
        val paddingTop = 20f
        val paddingRight = 20f

        val chartW = size.width - paddingLeft - paddingRight
        val chartH = size.height - paddingTop - paddingBottom

        if (chartW <= 0 || chartH <= 0) return@Canvas

        val minVal = values.first()
        val maxVal = values.last().coerceAtLeast(minVal + 1.0)
        val range = maxVal - minVal

        fun xOf(index: Int) = paddingLeft + (index.toFloat() / (values.size - 1)) * chartW
        fun yOf(value: Double) = paddingTop + chartH - ((value - minVal) / range * chartH).toFloat()

        // Grid lines
        val gridLines = 4
        repeat(gridLines + 1) { i ->
            val y = paddingTop + (i.toFloat() / gridLines) * chartH
            drawLine(
                color = outlineColor.copy(alpha = 0.2f),
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + chartW, y),
                strokeWidth = 1f
            )
            val gridValue = maxVal - (i.toFloat() / gridLines) * range
            val label = "${currency}${gridValue.formatMoney()}"
            val measured = textMeasurer.measure(label, style = TextStyle(fontSize = 9.sp, color = onSurface.copy(alpha = 0.7f)))
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(paddingLeft - measured.size.width - 4f, y - measured.size.height / 2f),
                style = TextStyle(fontSize = 9.sp, color = onSurface.copy(alpha = 0.7f))
            )
        }

        // X-axis labels (years)
        val maxLabels = minOf(values.size, 6)
        val step = ((values.size - 1).toFloat() / (maxLabels - 1)).coerceAtLeast(1f)
        for (i in 0 until maxLabels) {
            val index = (i * step).toInt().coerceIn(0, values.size - 1)
            val x = xOf(index)
            val label = "$index"
            val measured = textMeasurer.measure(label, style = TextStyle(fontSize = 9.sp))
            drawText(
                textMeasurer = textMeasurer,
                text = label,
                topLeft = Offset(x - measured.size.width / 2f, paddingTop + chartH + 6f),
                style = TextStyle(fontSize = 9.sp, color = onSurface.copy(alpha = 0.7f))
            )
        }

        // Axes
        drawLine(color = outlineColor, start = Offset(paddingLeft, paddingTop), end = Offset(paddingLeft, paddingTop + chartH), strokeWidth = 1.5f)
        drawLine(color = outlineColor, start = Offset(paddingLeft, paddingTop + chartH), end = Offset(paddingLeft + chartW, paddingTop + chartH), strokeWidth = 1.5f)

        // Animated line path
        val animatedCount = (values.size * animProgress).toInt().coerceAtLeast(2).coerceAtMost(values.size)
        val linePath = Path()
        values.take(animatedCount).forEachIndexed { index, value ->
            val x = xOf(index)
            val y = yOf(value)
            if (index == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }

        if (showGradient && animatedCount >= 2) {
            val fillPath = Path().also { it.addPath(linePath) }
            val lastX = xOf(animatedCount - 1)
            fillPath.lineTo(lastX, paddingTop + chartH)
            fillPath.lineTo(paddingLeft, paddingTop + chartH)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent),
                    startY = paddingTop,
                    endY = paddingTop + chartH
                )
            )
        }

        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(
                width = lineWidthDp * density,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Hover dot
        val hIdx = hoveredIndex
        if (interactive && hIdx != null && hIdx < values.size) {
            drawCircle(
                color = primaryColor,
                radius = 6f * density,
                center = Offset(xOf(hIdx), yOf(values[hIdx]))
            )
            drawCircle(
                color = Color.White,
                radius = 3f * density,
                center = Offset(xOf(hIdx), yOf(values[hIdx]))
            )
        }

        // Start dot
        drawCircle(
            color = primaryColor,
            radius = 4f * density,
            center = Offset(xOf(0), yOf(values[0]))
        )
        // End dot (when animation complete)
        if (animProgress > 0.99f) {
            drawCircle(
                color = primaryColor,
                radius = 4f * density,
                center = Offset(xOf(values.size - 1), yOf(values.last()))
            )
        }
    }
}
