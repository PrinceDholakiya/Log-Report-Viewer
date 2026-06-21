package com.interview.logviewer.data.mapper

import com.interview.logviewer.data.remote.dto.LogEntryDto
import com.interview.logviewer.data.remote.dto.LogResponseDto
import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.domain.model.Severity
import java.time.Instant

fun LogResponseDto.toDomain(): List<LogEntry> =
    data.map { it.toDomain(sessionId = sessionId) }

fun LogEntryDto.toDomain(sessionId: String): LogEntry {
    val parsedTimestamp = runCatching { Instant.parse(timestamp) }.getOrDefault(Instant.EPOCH)
    // Severity restore from raw string to enum for easy filtering rather than facing crash due to case sensitivity.
    val severity = Severity.fromRaw(severity)

    // Making new searchable field to later use for searching. (msg, tag, severity, id)
    val searchableText = buildString {
        append(message.lowercase())
        append(' ')
        append(tag.lowercase())
        append(' ')
        append(severity.label.lowercase())
        append(' ')
        append(id.lowercase())
    }

    return LogEntry(
        id = id,
        timestamp = parsedTimestamp,
        severity = severity,
        tag = tag,
        message = message,
        sessionId = sessionId,
        latencyMs = metadata.latencyMs,
        isAiGenerated = metadata.isAiGenerated,
        searchableText = searchableText // <- New field for searching
    )
}
