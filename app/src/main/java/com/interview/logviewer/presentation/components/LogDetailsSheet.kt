package com.interview.logviewer.presentation.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.ui.theme.toColor
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
private val detailTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' HH:mm:ss.SSS")

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDetailsSheet(
    log: LogEntry,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, end = 8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(log.severity.toColor())
                )
                Text(
                    text = log.severity.label,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = log.severity.toColor()
                )
            }

            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            HorizontalDivider()

            DetailRow(label = "Timestamp", value = detailTimeFormatter.format(log.timestamp.atZone(ZoneId.systemDefault())))
            DetailRow(label = "Tag", value = log.tag)
            DetailRow(label = "Session", value = log.sessionId)
            DetailRow(label = "Latency", value = "${log.latencyMs} ms")
            DetailRow(label = "AI generated", value = if (log.isAiGenerated) "Yes" else "No")
            DetailRow(label = "Log ID", value = log.id)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
