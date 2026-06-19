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

    val criticalCount = (severityCounts[Severity.FATAL] ?: 0) + (severityCounts[Severity.ERROR] ?: 0)

    val errorDensityPercent =
        if (total == 0) 0 else ((criticalCount * 100f) / total).toInt()

    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(diameter)
        ) {
            val stroke = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Butt
            )

            val arcSize = Size(
                width = size.width - stroke.width,
                height = size.height - stroke.width
            )

            val topLeft = Offset(
                x = stroke.width / 2f,
                y = stroke.width / 2f
            )

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
                    val sweepAngle =
                        (count.toFloat() / total.toFloat()) * 360f

                    drawArc(
                        color = severity.toColor(),
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

        // To mention total % of errors and fatal combined.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$errorDensityPercent%",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "errors",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
