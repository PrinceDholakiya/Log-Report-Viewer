package com.interview.logviewer.domain.model

data class LogGroup(
    val key: String,
    val label: String,
    val entries: List<LogEntry>,
    val severityCounts: Map<Severity, Int>
) {
    val errorDensity: Float
        get() {
            if (entries.isEmpty()) return 0f
            val criticalCount = (severityCounts[Severity.FATAL] ?: 0) + (severityCounts[Severity.ERROR] ?: 0)
            return criticalCount.toFloat() / entries.size
        }
}

enum class GroupingMode(val label: String) {
    BY_DATE("Date"),
    BY_SESSION("Session")
}
