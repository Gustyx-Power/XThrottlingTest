package id.xms.xtrt.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StabilityIndicator(stability: Double) {
    val animatedProgress by animateFloatAsState(
        targetValue = (stability / 100.0).toFloat(),
        label = "stability"
    )

    val stabilityColor = when {
        stability >= 95 -> Color(0xFF4CAF50) // Green
        stability >= 85 -> Color(0xFFFFC107) // Yellow
        stability >= 70 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Throttling Stability",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%.1f%%", stability),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = stabilityColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = stabilityColor,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when {
                    stability >= 95 -> "Excellent - No throttling detected"
                    stability >= 85 -> "Good - Minor throttling"
                    stability >= 70 -> "Fair - Moderate throttling"
                    else -> "Poor - Significant throttling"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
