package id.xms.xtrt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    onShareImage: () -> Unit,
    onSharePdf: () -> Unit,
    onShareJson: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Share Test Results", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            ShareOption(
                icon = Icons.Default.Image,
                title = "Share as Image",
                description = "Quick share to social media (PNG)",
                badge = "Popular",
                onClick = {
                    onShareImage()
                    onDismiss()
                }
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ShareOption(
                icon = Icons.Default.PictureAsPdf,
                title = "Export as PDF Report",
                description = "Professional documentation",
                onClick = {
                    onSharePdf()
                    onDismiss()
                }
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            ShareOption(
                icon = Icons.Default.DataObject,
                title = "Export Raw Data",
                description = "JSON format for technical analysis",
                onClick = {
                    onShareJson()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = badge,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
        )
    }
}
