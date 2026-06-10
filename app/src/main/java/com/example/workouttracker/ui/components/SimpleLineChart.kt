package com.example.workouttracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.workouttracker.ui.theme.Accent
import com.example.workouttracker.ui.theme.Divider
import kotlin.math.roundToInt

@Composable
fun SimpleLineChart(
    data: List<Pair<Long, Double>>,
    label: String,
    modifier: Modifier = Modifier
) {
    val lineColor = Accent
    val gridColor = Divider
    val surfaceColor = MaterialTheme.colorScheme.surface

    if (data.isEmpty()) return

    val points = remember(data) { data.map { it.second } }
    val min = remember(points) { points.min() }
    val max = remember(points) { points.max() }
    val range = remember(points) { if (max == min) 1.0 else max - min }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        modifier = modifier
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val padding = 40f
            val chartWidth = canvasWidth - padding * 2
            val chartHeight = canvasHeight - padding * 2

            if (data.size < 2) return@Canvas

            // Grid lines
            for (i in 0..3) {
                val y = padding + chartHeight * i / 3
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(padding, y),
                    end = Offset(canvasWidth - padding, y),
                    strokeWidth = 1f
                )
            }

            // Data line
            val path = Path()
            val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)

            data.forEachIndexed { index, pair ->
                val x = padding + index * stepX
                val y = padding + chartHeight * (1f - ((pair.second - min) / range).toFloat())
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Data points
            data.forEachIndexed { index, pair ->
                val x = padding + index * stepX
                val y = padding + chartHeight * (1f - ((pair.second - min) / range).toFloat())
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = surfaceColor,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}
