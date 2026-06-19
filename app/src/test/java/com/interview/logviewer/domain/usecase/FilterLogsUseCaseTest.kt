package com.interview.logviewer.domain.usecase

import com.interview.logviewer.domain.model.Severity
import com.interview.logviewer.fake.FakeLogRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterLogsUseCaseTest {

    private val filterLogs = FilterLogsUseCase()

    private val logs = listOf(
        FakeLogRepository.sampleLog(id = "1", message = "Connection timed out", tag = "network", severity = Severity.ERROR),
        FakeLogRepository.sampleLog(id = "2", message = "Cache miss", tag = "cache", severity = Severity.FATAL),
        FakeLogRepository.sampleLog(id = "3", message = "User logged in", tag = "auth", severity = Severity.DEBUG)
    )

    @Test
    fun `blank query returns the full list unfiltered`() {
        val result = filterLogs(logs, "")
        assertEquals(3, result.size)
    }

    @Test
    fun `query matches on message text`() {
        val result = filterLogs(logs, "cache miss")
        assertEquals(listOf("2"), result.map { it.id })
    }

    @Test
    fun `query matches on tag`() {
        val result = filterLogs(logs, "auth")
        assertEquals(listOf("3"), result.map { it.id })
    }

    @Test
    fun `query matches on severity label`() {
        val result = filterLogs(logs, "fatal")
        assertEquals(listOf("2"), result.map { it.id })
    }

    @Test
    fun `query is case insensitive`() {
        val result = filterLogs(logs, "CONNECTION")
        assertEquals(listOf("1"), result.map { it.id })
    }

    @Test
    fun `no matches returns an empty list`() {
        val result = filterLogs(logs, "this text matches nothing")
        assertTrue(result.isEmpty())
    }

    @Test
    fun `surrounding whitespace in the query is ignored`() {
        val result = filterLogs(logs, "   auth   ")
        assertEquals(listOf("3"), result.map { it.id })
    }
}
