package com.interview.logviewer.fake

import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.domain.model.Severity
import com.interview.logviewer.domain.repository.LogRepository
import java.time.Instant

class FakeLogRepository(
    private var logs: List<LogEntry> = emptyList(),
    private var shouldFail: Boolean = false
) : LogRepository {

    override suspend fun fetchLogs(): List<LogEntry> {
        if (shouldFail) throw RuntimeException("network unavailable")
        return logs
    }

    fun setLogs(newLogs: List<LogEntry>) {
        logs = newLogs
    }

    fun setShouldFail(value: Boolean) {
        shouldFail = value
    }

    companion object {
        fun sampleLog(
            id: String = "id-1",
            timestamp: Instant = Instant.parse("2026-01-22T15:48:17.892472Z"),
            severity: Severity = Severity.ERROR,
            tag: String = "network",
            message: String = "Connection timed out",
            sessionId: String = "session-666",
            latencyMs: Int = 2040,
            isAiGenerated: Boolean = true
        ): LogEntry = LogEntry(
            id = id,
            timestamp = timestamp,
            severity = severity,
            tag = tag,
            message = message,
            sessionId = sessionId,
            latencyMs = latencyMs,
            isAiGenerated = isAiGenerated,
            searchableText = "${message.lowercase()} ${tag.lowercase()} ${severity.label.lowercase()} ${id.lowercase()}"
        )
    }
}
