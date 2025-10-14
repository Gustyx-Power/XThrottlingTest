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
fun StabilityIndicator(
    stability: Double,
    degradation: Double = 0.0,
    modifier: Modifier = Modifier
) {
    // Animated progress untuk smooth transition
    val animatedProgress by animateFloatAsState(
        targetValue = (stability / 100.0).toFloat().coerceIn(0f, 1f),
        label = "stability"
    )

    // Determine status berdasarkan stability
    val (statusText, statusColor, description) = when {
        stability >= 95.0 -> Triple("Excellent", Color(0xFF4CAF50), "Perfect - No throttling")
        stability >= 90.0 -> Triple("Very Good", Color(0xFF66BB6A), "Minimal throttling")
        stability >= 85.0 -> Triple("Good", Color(0xFF8BC34A), "Light throttling")
        stability >= 80.0 -> Triple("Fair", Color(0xFFCDDC39), "Moderate throttling")
        stability >= 75.0 -> Triple("Warning", Color(0xFFFFEB3B), "Notable throttling")
        stability >= 70.0 -> Triple("Throttling", Color(0xFFFF9800), "Significant throttling")
        stability >= 65.0 -> Triple("Heavy", Color(0xFFFF5722), "Heavy throttling")
        else -> Triple("Critical", Color(0xFFF44336), "Severe throttling")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = "Throttling Stability",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main stability percentage (BIG NUMBER)
            Text(
                text = String.format("%.1f%%", stability),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = statusColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status description
            Text(
                text = "$statusText - $description",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar (animated)
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Optional: Show degradation if significant
            if (degradation > 5.0) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Peak Degradation",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Worst drop from peak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    Text(
                        text = String.format("%.1f%%", degradation),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            degradation < 10.0 -> Color(0xFF4CAF50)
                            degradation < 20.0 -> Color(0xFFFF9800)
                            else -> Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}
