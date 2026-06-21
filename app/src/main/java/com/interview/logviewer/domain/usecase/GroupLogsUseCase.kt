package com.interview.logviewer.domain.usecase

import com.interview.logviewer.domain.model.GroupingMode
import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.domain.model.LogGroup
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


class GroupLogsUseCase @Inject constructor() {

    private val dateKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dateLabelFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
    private val zone = ZoneId.systemDefault()

    operator fun invoke(logs: List<LogEntry>, mode: GroupingMode): List<LogGroup> {
        // Bucket logs by session id or by calendar day
        val buckets: Map<String, List<LogEntry>> = when (mode) {
            GroupingMode.BY_SESSION -> logs.groupBy { it.sessionId }
            GroupingMode.BY_DATE -> logs.groupBy { dateKeyFormatter.format(it.timestamp.atZone(zone)) }
        }

        // Build a LogGroup for each bucket
        return buckets.map { (key, entries) ->
            val sortedEntries = entries.sortedByDescending { it.timestamp }
            LogGroup(
                key = key,
                label = labelFor(key, mode, sortedEntries.firstOrNull()),
                entries = sortedEntries,
                // Precomputed once per group so the severity ring just reads
                severityCounts = entries.groupingBy { it.severity }.eachCount()
            )
        }.sortedByDescending { group -> group.entries.maxOf { it.timestamp } }
    }

    // Builds the header text shown for each group
    private fun labelFor(key: String, mode: GroupingMode, newest: LogEntry?): String = when (mode) {
        GroupingMode.BY_SESSION -> "Session: $key"
        GroupingMode.BY_DATE -> newest?.let { dateLabelFormatter.format(it.timestamp.atZone(zone)) } ?: key
    }
}
