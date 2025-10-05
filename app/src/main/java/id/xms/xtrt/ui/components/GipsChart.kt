package id.xms.xtrt.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GipsChart(
    data: List<Pair<Long, Double>>,
    stability: Double, // ✅ NEW: Receive from TestState
    modifier: Modifier = Modifier
) {
    val maxGips = data.maxOfOrNull { it.second } ?: 0.0
    val minGips = if (data.isNotEmpty()) data.minOfOrNull { it.second } ?: 0.0 else 0.0
    val avgGips = if (data.isNotEmpty()) data.map { it.second }.average() else 0.0

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performance Graph",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${data.size} samples",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✅ Status based on PASSED stability (from TestState)
            val (statusText, statusColor) = when {
                data.isEmpty() -> "Waiting" to Color(0xFF2196F3)
                stability >= 95.0 -> "Excellent" to Color(0xFF4CAF50)
                stability >= 90.0 -> "Very Good" to Color(0xFF66BB6A)
                stability >= 85.0 -> "Good" to Color(0xFF8BC34A)
                stability >= 80.0 -> "Fair" to Color(0xFFCDDC39)
                stability >= 75.0 -> "Warning" to Color(0xFFFFEB3B)
                stability >= 70.0 -> "Throttling" to Color(0xFFFF9800)
                stability >= 65.0 -> "Heavy Throttle" to Color(0xFFFF5722)
                else -> "Critical" to Color(0xFFF44336)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, shape = RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniStat("Max", maxGips, Color(0xFF4CAF50))
                    MiniStat("Min", minGips, Color(0xFFF44336))
                    MiniStat("Avg", avgGips, Color(0xFF2196F3))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MAIN CHART
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F5F5))
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawRect(color = Color.White, size = size)

                    if (data.isNotEmpty()) {
                        drawChartContent(data, maxGips, minGips)
                    } else {
                        drawEmptyState()
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TinyLegend("Excellent", Color(0xFF4CAF50))
                    TinyLegend("Throttling", Color(0xFFFF9800))
                    TinyLegend("Critical", Color(0xFFF44336))
                }

                val duration = data.lastOrNull()?.first ?: 0L
                Text(
                    text = formatTime(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TinyLegend(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun DrawScope.drawChartContent(
    data: List<Pair<Long, Double>>,
    maxGips: Double,
    minGips: Double
) {
    val width = size.width
    val height = size.height

    if (width <= 0 || height <= 0) return

    val leftPadding = 70f
    val rightPadding = 25f
    val topPadding = 25f
    val bottomPadding = 25f

    val chartWidth = width - leftPadding - rightPadding
    val chartHeight = height - topPadding - bottomPadding

    if (chartWidth <= 0 || chartHeight <= 0) return

    // Smart range
    val range = maxGips - minGips
    val rangePercent = if (maxGips > 0) (range / maxGips) * 100 else 0.0

    val (displayMin, displayMax) = if (rangePercent < 5.0 && maxGips > 0) {
        val avg = (maxGips + minGips) / 2
        val expanded = avg * 0.25
        Pair((avg - expanded / 2).coerceAtLeast(0.0), avg + expanded / 2)
    } else {
        Pair((minGips - range * 0.15).coerceAtLeast(0.0), maxGips + range * 0.15)
    }

    val dataRange = displayMax - displayMin
    if (dataRange <= 0) return

    // Grid
    val gridColor = Color.Gray.copy(alpha = 0.3f)
    val strongGridColor = Color.Gray.copy(alpha = 0.5f)

    for (i in 0..10) {
        val y = topPadding + (chartHeight / 10f) * i
        val isStrong = i % 2 == 0

        drawLine(
            color = if (isStrong) strongGridColor else gridColor,
            start = Offset(leftPadding, y),
            end = Offset(width - rightPadding, y),
            strokeWidth = if (isStrong) 2f else 1f
        )

        val value = displayMax - ((displayMax - displayMin) / 10.0) * i
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 28f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }

        drawContext.canvas.nativeCanvas.drawText(
            String.format("%.2f", value),
            leftPadding - 10f,
            y + 10f,
            paint
        )
    }

    // Vertical grid
    if (data.size > 1) {
        val vLines = minOf(10, data.size - 1)
        for (i in 0..vLines) {
            val idx = ((i.toFloat() / vLines) * (data.size - 1)).toInt()
            val x = leftPadding + (chartWidth / (data.size - 1)) * idx

            drawLine(
                color = gridColor,
                start = Offset(x, topPadding),
                end = Offset(x, height - bottomPadding),
                strokeWidth = 1f
            )
        }
    }

    // Axes
    drawLine(
        color = Color.DarkGray,
        start = Offset(leftPadding, topPadding),
        end = Offset(leftPadding, height - bottomPadding),
        strokeWidth = 3f
    )

    drawLine(
        color = Color.DarkGray,
        start = Offset(leftPadding, height - bottomPadding),
        end = Offset(width - rightPadding, height - bottomPadding),
        strokeWidth = 3f
    )

    // Single point
    if (data.size == 1) {
        val (_, gips) = data.first()
        val x = leftPadding + chartWidth / 2
        val normY = ((gips - displayMin) / dataRange).toFloat().coerceIn(0f, 1f)
        val y = topPadding + chartHeight - (normY * chartHeight)

        val color = getColorForPerformance(gips, maxGips)

        for (r in listOf(35f, 25f, 15f)) {
            drawCircle(
                color = color.copy(alpha = 0.3f / (r / 12)),
                radius = r,
                center = Offset(x, y)
            )
        }
        drawCircle(color = color, radius = 10f, center = Offset(x, y))
        return
    }

    // Calculate all points
    val points = data.mapIndexed { i, (_, gips) ->
        val x = leftPadding + (chartWidth / (data.size - 1)) * i
        val normY = ((gips - displayMin) / dataRange).toFloat().coerceIn(0f, 1f)
        val y = topPadding + chartHeight - (normY * chartHeight)
        Offset(x, y)
    }

    // Draw multi-color fill
    drawMultiColorFill(points, data, maxGips, height, bottomPadding, topPadding)

    // Draw lines
    for (i in 0 until points.size - 1) {
        val start = points[i]
        val end = points[i + 1]

        val currentPerformance = data[i + 1].second
        val segmentColor = getColorForPerformance(currentPerformance, maxGips)

        drawLine(
            color = Color.Black.copy(alpha = 0.1f),
            start = Offset(start.x + 1, start.y + 1),
            end = Offset(end.x + 1, end.y + 1),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        drawLine(
            color = segmentColor,
            start = start,
            end = end,
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    // Smart point rendering
    val shouldDrawPoints = data.size <= 50
    val pointInterval = when {
        data.size <= 50 -> 1
        data.size <= 200 -> 5
        data.size <= 500 -> 10
        else -> 20
    }

    if (shouldDrawPoints) {
        points.forEachIndexed { i, pt ->
            val color = getColorForPerformance(data[i].second, maxGips)

            drawCircle(color = color.copy(alpha = 0.2f), radius = 8f, center = pt)
            drawCircle(color = Color.White, radius = 5f, center = pt)
            drawCircle(color = color, radius = 3.5f, center = pt)
        }
    } else {
        points.forEachIndexed { i, pt ->
            if (i % pointInterval == 0 || i == points.size - 1) {
                val color = getColorForPerformance(data[i].second, maxGips)

                drawCircle(color = color.copy(alpha = 0.15f), radius = 6f, center = pt)
                drawCircle(color = Color.White, radius = 4f, center = pt)
                drawCircle(color = color, radius = 2.5f, center = pt)
            }
        }
    }
}

private fun getColorForPerformance(currentGips: Double, absoluteMaxGips: Double): Color {
    val performanceRatio = if (absoluteMaxGips > 0) currentGips / absoluteMaxGips else 1.0

    return when {
        performanceRatio >= 0.95 -> Color(0xFF4CAF50)
        performanceRatio >= 0.90 -> Color(0xFF66BB6A)
        performanceRatio >= 0.85 -> Color(0xFF8BC34A)
        performanceRatio >= 0.80 -> Color(0xFFCDDC39)
        performanceRatio >= 0.75 -> Color(0xFFFFEB3B)
        performanceRatio >= 0.70 -> Color(0xFFFFC107)
        performanceRatio >= 0.65 -> Color(0xFFFF9800)
        performanceRatio >= 0.60 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
}

private fun DrawScope.drawMultiColorFill(
    points: List<Offset>,
    data: List<Pair<Long, Double>>,
    absoluteMaxGips: Double,
    height: Float,
    bottomPadding: Float,
    topPadding: Float
) {
    for (i in 0 until points.size - 1) {
        val start = points[i]
        val end = points[i + 1]

        val currentPerformance = data[i + 1].second
        val fillColor = getColorForPerformance(currentPerformance, absoluteMaxGips)

        val segmentPath = Path().apply {
            moveTo(start.x, height - bottomPadding)
            lineTo(start.x, start.y)
            lineTo(end.x, end.y)
            lineTo(end.x, height - bottomPadding)
            close()
        }

        drawPath(
            path = segmentPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    fillColor.copy(alpha = 0.3f),
                    fillColor.copy(alpha = 0.05f)
                ),
                startY = topPadding,
                endY = height - bottomPadding
            )
        )
    }
}

private fun DrawScope.drawEmptyState() {
    val w = size.width
    val h = size.height
    val lp = 70f
    val rp = 25f
    val tp = 25f
    val bp = 25f
    val cw = w - lp - rp
    val ch = h - tp - bp

    val grid = Color.Gray.copy(alpha = 0.2f)

    for (i in 0..10) {
        val y = tp + (ch / 10f) * i
        drawLine(grid, Offset(lp, y), Offset(w - rp, y), 1f)
    }

    for (i in 0..10) {
        val x = lp + (cw / 10f) * i
        drawLine(grid, Offset(x, tp), Offset(x, h - bp), 1f)
    }

    drawLine(Color.Gray.copy(0.5f), Offset(lp, tp), Offset(lp, h - bp), 3f)
    drawLine(Color.Gray.copy(0.5f), Offset(lp, h - bp), Offset(w - rp, h - bp), 3f)

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 32f
        textAlign = android.graphics.Paint.Align.CENTER
        alpha = 128
        isAntiAlias = true
    }

    drawContext.canvas.nativeCanvas.drawText("Waiting for benchmark data...", w / 2, h / 2, paint)
}

private fun formatTime(secs: Long): String {
    val h = secs / 3600
    val m = (secs % 3600) / 60
    val s = secs % 60
    return if (h > 0) String.format("%02d:%02d:%02d", h, m, s)
    else String.format("%02d:%02d", m, s)
}
