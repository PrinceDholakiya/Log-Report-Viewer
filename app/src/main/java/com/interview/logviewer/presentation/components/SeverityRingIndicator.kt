package com.interview.logviewer.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.interview.logviewer.domain.model.Severity
import com.interview.logviewer.ui.theme.toColor

@Composable
fun SeverityRingIndicator(
    severityCounts: Map<Severity, Int>,
    modifier: Modifier = Modifier,
    diameter: Dp = 96.dp,
    strokeWidth: Dp = 12.dp
) {
    val total = severityCounts.values.sum()

    // Find the severity with the most logs right with current search/filter — updates live
    val dominantEntry = severityCounts
        .filter { it.value > 0 }
        .maxByOrNull { it.value }   // highest count

    val dominantSeverity = dominantEntry?.key
    val dominantPercent = if (total == 0 || dominantEntry == null) 0
    else ((dominantEntry.value * 100f) / total).toInt()

    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Butt)
            val arcSize = Size(size.width - stroke.width, size.height - stroke.width)
            val topLeft = Offset(stroke.width / 2f, stroke.width / 2f)

            if (total == 0) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.25f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = stroke
                )
                return@Canvas
            }

            var startAngle = -90f

            Severity.displayOrder.forEach { severity ->
                val count = severityCounts[severity] ?: 0
                if (count > 0) {
                    val sweepAngle = (count.toFloat() / total.toFloat()) * 360f

                    drawArc(
                        color = if (severity == dominantSeverity)
                            severity.toColor()           // full opacity — stands out
                        else
                            severity.toColor().copy(alpha = 0.4f),  // dimmed — recedes
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                    startAngle += sweepAngle
                }
            }
        }

        // Center label — always shows the dominant severity
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$dominantPercent%",
                style = MaterialTheme.typography.titleLarge,
                // Color matches the dominant segment — instant visual link
                color = dominantSeverity?.toColor()
                    ?: MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = dominantSeverity?.label ?: "-",
                style = MaterialTheme.typography.labelSmall,
                color = dominantSeverity?.toColor()?.copy(alpha = 0.8f)
                    ?: MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}