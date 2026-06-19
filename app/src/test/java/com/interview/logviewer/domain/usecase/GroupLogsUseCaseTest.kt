package com.interview.logviewer.domain.usecase

import com.interview.logviewer.domain.model.GroupingMode
import com.interview.logviewer.domain.model.Severity
import com.interview.logviewer.fake.FakeLogRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.TimeZone

class GroupLogsUseCaseTest {

    private val groupLogs by lazy { GroupLogsUseCase() }
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun pinTimeZoneToUtc() {
        // GroupLogsUseCase buckets by the JVM's default zone; pinning it to UTC
        // makes the day-boundary assertions below deterministic regardless of
        // the timezone the test happens to run in.
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun restoreTimeZone() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `groups entries by calendar day`() {
        val logs = listOf(
            FakeLogRepository.sampleLog(id = "1", timestamp = Instant.parse("2026-01-22T10:00:00Z")),
            FakeLogRepository.sampleLog(id = "2", timestamp = Instant.parse("2026-01-22T20:00:00Z")),
            FakeLogRepository.sampleLog(id = "3", timestamp = Instant.parse("2026-01-23T08:00:00Z"))
        )

        val groups = groupLogs(logs, GroupingMode.BY_DATE)

        assertEquals(2, groups.size)
        assertEquals(3, groups.sumOf { it.entries.size })
    }

    @Test
    fun `groups entries by session id`() {
        val logs = listOf(
            FakeLogRepository.sampleLog(id = "1", sessionId = "session-a"),
            FakeLogRepository.sampleLog(id = "2", sessionId = "session-a"),
            FakeLogRepository.sampleLog(id = "3", sessionId = "session-b")
        )

        val groups = groupLogs(logs, GroupingMode.BY_SESSION)

        assertEquals(2, groups.size)
        val sessionA = groups.first { it.key == "session-a" }
        assertEquals(2, sessionA.entries.size)
    }

    @Test
    fun `groups are sorted with the most recent entries first`() {
        val logs = listOf(
            FakeLogRepository.sampleLog(id = "old", timestamp = Instant.parse("2026-01-20T10:00:00Z")),
            FakeLogRepository.sampleLog(id = "new", timestamp = Instant.parse("2026-01-25T10:00:00Z"))
        )

        val groups = groupLogs(logs, GroupingMode.BY_DATE)

        assertEquals("new", groups.first().entries.first().id)
    }

    @Test
    fun `severity counts are computed per group`() {
        val logs = listOf(
            FakeLogRepository.sampleLog(id = "1", severity = Severity.ERROR, timestamp = Instant.parse("2026-01-22T10:00:00Z")),
            FakeLogRepository.sampleLog(id = "2", severity = Severity.ERROR, timestamp = Instant.parse("2026-01-22T11:00:00Z")),
            FakeLogRepository.sampleLog(id = "3", severity = Severity.DEBUG, timestamp = Instant.parse("2026-01-22T12:00:00Z"))
        )

        val group = groupLogs(logs, GroupingMode.BY_DATE).first()

        assertEquals(2, group.severityCounts[Severity.ERROR])
        assertEquals(1, group.severityCounts[Severity.DEBUG])
    }

    @Test
    fun `empty input produces no groups`() {
        val groups = groupLogs(emptyList(), GroupingMode.BY_DATE)
        assertTrue(groups.isEmpty())
    }
}
