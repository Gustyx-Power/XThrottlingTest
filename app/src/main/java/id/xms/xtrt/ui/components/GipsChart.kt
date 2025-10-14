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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun GipsChart(
    data: List<Pair<Long, Double>>,
    stability: Double,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Enhanced Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Performance Graph",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${data.size} samples • ${formatTime(data.lastOrNull()?.first ?: 0L)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badge with enhanced styling
            val (statusText, statusColor) = when {
                data.isEmpty() -> "Waiting" to Color(0xFF2196F3)
                stability >= 95.0 -> "Excellent" to Color(0xFF4CAF50)
                stability >= 90.0 -> "Very Good" to Color(0xFF66BB6A)
                stability >= 85.0 -> "Good" to Color(0xFF8BC34A)
                stability >= 80.0 -> "Fair" to Color(0xFFCDDC39)
                stability >= 75.0 -> "Warning" to Color(0xFFFFEB3B)
                stability >= 70.0 -> "Throttling" to Color(0xFFFF9800)
                stability >= 65.0 -> "Heavy" to Color(0xFFFF5722)
                else -> "Critical" to Color(0xFFF44336)
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(statusColor, shape = RoundedCornerShape(5.dp))
                    )
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Enhanced Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedStat("Maximum", maxGips, Color(0xFF4CAF50))
                EnhancedStat("Minimum", minGips, Color(0xFFF44336))
                EnhancedStat("Average", avgGips, Color(0xFF2196F3))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ENHANCED CHART with shadow and better styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFAFAFA),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (data.isNotEmpty()) {
                        drawEnhancedChart(data, maxGips, minGips)
                    } else {
                        drawEmptyState()
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enhanced Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedLegend("Excellent", Color(0xFF4CAF50))
                EnhancedLegend("Moderate", Color(0xFFFF9800))
                EnhancedLegend("Critical", Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun EnhancedStat(label: String, value: Double, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = String.format("%.2f", value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = "GIPS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun EnhancedLegend(text: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = RoundedCornerShape(3.dp))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun DrawScope.drawEnhancedChart(
    data: List<Pair<Long, Double>>,
    maxGips: Double,
    minGips: Double
) {
    val width = size.width
    val height = size.height

    val leftPadding = 80f
    val rightPadding = 30f
    val topPadding = 30f
    val bottomPadding = 40f

    val chartWidth = width - leftPadding - rightPadding
    val chartHeight = height - topPadding - bottomPadding

    if (chartWidth <= 0 || chartHeight <= 0) return

    // Enhanced range calculation
    val range = maxGips - minGips
    val rangePercent = if (maxGips > 0) (range / maxGips) * 100 else 0.0

    val (displayMin, displayMax) = if (rangePercent < 5.0 && maxGips > 0) {
        val avg = (maxGips + minGips) / 2
        val expanded = avg * 0.25
        Pair((avg - expanded / 2).coerceAtLeast(0.0), avg + expanded / 2)
    } else {
        Pair((minGips - range * 0.1).coerceAtLeast(0.0), maxGips + range * 0.1)
    }

    val dataRange = displayMax - displayMin
    if (dataRange <= 0) return

    // Enhanced Grid with alternating background
    val gridColor = Color.Gray.copy(alpha = 0.15f)
    val strongGridColor = Color.Gray.copy(alpha = 0.3f)

    for (i in 0..10) {
        val y = topPadding + (chartHeight / 10f) * i
        val isStrong = i % 2 == 0

        // Alternating background
        if (i < 10) {
            val nextY = topPadding + (chartHeight / 10f) * (i + 1)
            if (i % 2 == 0) {
                drawRect(
                    color = Color(0xFFF9F9F9),
                    topLeft = Offset(leftPadding, y),
                    size = androidx.compose.ui.geometry.Size(chartWidth, nextY - y)
                )
            }
        }

        // Grid lines
        drawLine(
            color = if (isStrong) strongGridColor else gridColor,
            start = Offset(leftPadding, y),
            end = Offset(width - rightPadding, y),
            strokeWidth = if (isStrong) 2f else 1f
        )

        // Y-axis labels with better formatting
        val value = displayMax - ((displayMax - displayMin) / 10.0) * i
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#424242")
            textSize = 30f
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        drawContext.canvas.nativeCanvas.drawText(
            String.format("%.1f", value),
            leftPadding - 15f,
            y + 12f,
            paint
        )
    }

    // Vertical time grid
    if (data.size > 1) {
        val vLines = minOf(8, data.size - 1)
        for (i in 0..vLines) {
            val idx = ((i.toFloat() / vLines) * (data.size - 1)).toInt()
            val x = leftPadding + (chartWidth / (data.size - 1)) * idx

            drawLine(
                color = gridColor,
                start = Offset(x, topPadding),
                end = Offset(x, height - bottomPadding),
                strokeWidth = 1f
            )

            // Time labels
            val timeSeconds = data[idx].first
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#757575")
                textSize = 26f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${timeSeconds}s",
                x,
                height - bottomPadding + 30f,
                paint
            )
        }
    }

    // Enhanced Axes
    drawLine(
        color = Color.DarkGray,
        start = Offset(leftPadding, topPadding),
        end = Offset(leftPadding, height - bottomPadding),
        strokeWidth = 4f
    )

    drawLine(
        color = Color.DarkGray,
        start = Offset(leftPadding, height - bottomPadding),
        end = Offset(width - rightPadding, height - bottomPadding),
        strokeWidth = 4f
    )

    // Calculate points
    val points = data.mapIndexed { i, (_, gips) ->
        val x = leftPadding + (chartWidth / (data.size - 1)) * i
        val normY = ((gips - displayMin) / dataRange).toFloat().coerceIn(0f, 1f)
        val y = topPadding + chartHeight - (normY * chartHeight)
        Offset(x, y)
    }

    // Enhanced gradient fill
    drawEnhancedFill(points, data, maxGips, height, bottomPadding, topPadding)

    // Draw smooth line with enhanced styling
    val path = Path()
    points.forEachIndexed { index, point ->
        if (index == 0) path.moveTo(point.x, point.y)
        else path.lineTo(point.x, point.y)
    }

    // Outer glow
    drawPath(
        path = path,
        color = Color.Black.copy(alpha = 0.1f),
        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Main colored line
    for (i in 0 until points.size - 1) {
        val start = points[i]
        val end = points[i + 1]
        val segmentColor = getColorForPerformance(data[i + 1].second, maxGips)

        drawLine(
            color = segmentColor,
            start = start,
            end = end,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }

    // Enhanced point markers
    val shouldDrawPoints = data.size <= 60
    if (shouldDrawPoints) {
        points.forEachIndexed { i, pt ->
            val color = getColorForPerformance(data[i].second, maxGips)

            // Outer ring
            drawCircle(
                color = color.copy(alpha = 0.3f),
                radius = 10f,
                center = pt
            )
            // White center
            drawCircle(color = Color.White, radius = 6f, center = pt)
            // Colored dot
            drawCircle(color = color, radius = 4f, center = pt)
        }
    }
}

private fun DrawScope.drawEnhancedFill(
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
                    fillColor.copy(alpha = 0.4f),
                    fillColor.copy(alpha = 0.08f)
                ),
                startY = topPadding,
                endY = height - bottomPadding
            )
        )
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

private fun DrawScope.drawEmptyState() {
    val w = size.width
    val h = size.height
    val lp = 80f
    val rp = 30f
    val tp = 30f
    val bp = 40f
    val cw = w - lp - rp
    val ch = h - tp - bp

    val grid = Color.Gray.copy(alpha = 0.15f)

    for (i in 0..10) {
        val y = tp + (ch / 10f) * i
        drawLine(grid, Offset(lp, y), Offset(w - rp, y), 1.5f)
    }

    for (i in 0..10) {
        val x = lp + (cw / 10f) * i
        drawLine(grid, Offset(x, tp), Offset(x, h - bp), 1.5f)
    }

    drawLine(Color.Gray.copy(0.5f), Offset(lp, tp), Offset(lp, h - bp), 4f)
    drawLine(Color.Gray.copy(0.5f), Offset(lp, h - bp), Offset(w - rp, h - bp), 4f)

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.GRAY
        textSize = 36f
        textAlign = android.graphics.Paint.Align.CENTER
        alpha = 100
        isAntiAlias = true
    }

    drawContext.canvas.nativeCanvas.drawText("Waiting for benchmark data...", w / 2, h / 2, paint)
}

private fun formatTime(secs: Long): String {
    val h = secs / 3600
    val m = (secs % 3600) / 60
    val s = secs % 60
    return if (h > 0) String.format("%dh %02dm", h, m)
    else if (m > 0) String.format("%dm %02ds", m, s)
    else String.format("%ds", s)
}
