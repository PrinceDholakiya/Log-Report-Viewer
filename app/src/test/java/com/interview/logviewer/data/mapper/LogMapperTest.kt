package com.interview.logviewer.data.mapper

import com.interview.logviewer.data.remote.dto.LogEntryDto
import com.interview.logviewer.data.remote.dto.LogMetadataDto
import com.interview.logviewer.data.remote.dto.LogResponseDto
import com.interview.logviewer.domain.model.Severity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class LogMapperTest {
    private fun dto(
        id: String = "33dbf6fc-d031-4dea-a7ce-1a8e96b19f14",
        timestamp: String = "2026-01-22T15:48:17.892472Z",
        severity: String = "ERROR",
        tag: String = "network",
        message: String = "Connection timed out",
        latencyMs: Int = 2040,
        isAiGenerated: Boolean = true
    ) = LogEntryDto(
        id = id,
        timestamp = timestamp,
        severity = severity,
        tag = tag,
        message = message,
        metadata = LogMetadataDto(latencyMs = latencyMs, isAiGenerated = isAiGenerated)
    )

    @Test
    fun `maps response fields into domain LogEntry`() {
        val response = LogResponseDto(
            totalCount = 1,
            sessionId = "session-666",
            data = listOf(dto())
        )

        val result = response.toDomain()

        assertEquals(1, result.size)
        val entry = result.first()
        assertEquals("33dbf6fc-d031-4dea-a7ce-1a8e96b19f14", entry.id)
        assertEquals(Severity.ERROR, entry.severity)
        assertEquals("network", entry.tag)
        assertEquals("Connection timed out", entry.message)
        assertEquals("session-666", entry.sessionId)
        assertEquals(2040, entry.latencyMs)
        assertTrue(entry.isAiGenerated)
        assertEquals(Instant.parse("2026-01-22T15:48:17.892472Z"), entry.timestamp)
    }

    @Test
    fun `unknown severity string falls back to UNKNOWN`() {
        val response = LogResponseDto(
            totalCount = 1,
            sessionId = "session-666",
            data = listOf(dto(severity = "TRACE"))
        )

        val entry = response.toDomain().first()

        assertEquals(Severity.UNKNOWN, entry.severity)
    }

    @Test
    fun `severity comparison is case insensitive`() {
        val response = LogResponseDto(
            totalCount = 1,
            sessionId = "session-666",
            data = listOf(dto(severity = "error"))
        )

        val entry = response.toDomain().first()

        assertEquals(Severity.ERROR, entry.severity)
    }

    @Test
    fun `searchableText includes message, tag, severity and id in lowercase`() {
        val response = LogResponseDto(
            totalCount = 1,
            sessionId = "session-666",
            data = listOf(dto())
        )

        val entry = response.toDomain().first()

        assertTrue(entry.searchableText.contains("connection timed out"))
        assertTrue(entry.searchableText.contains("network"))
        assertTrue(entry.searchableText.contains("error"))
        assertTrue(entry.searchableText.contains(entry.id.lowercase()))
    }

    @Test
    fun `malformed timestamp falls back to epoch instead of crashing`() {
        val response = LogResponseDto(
            totalCount = 1,
            sessionId = "session-666",
            data = listOf(dto(timestamp = "not-a-real-timestamp"))
        )

        val entry = response.toDomain().first()

        assertEquals(Instant.EPOCH, entry.timestamp)
    }
}
