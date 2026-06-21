package com.interview.logviewer.presentation

import com.interview.logviewer.domain.model.AiFilter
import com.interview.logviewer.domain.model.GroupingMode
import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.domain.model.LogGroup
import com.interview.logviewer.domain.model.Severity

data class LogViewerUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val searchQuery: String = "",
    val groupingMode: GroupingMode = GroupingMode.BY_DATE,
    // For AI based search
    val aiFilter: AiFilter = AiFilter.ALL,
    val groups: List<LogGroup> = emptyList(),
    val totalCount: Int = 0,
    val filteredCount: Int = 0,
    val overallSeverityCounts: Map<Severity, Int> = emptyMap(),
    val filteredSeverityCounts: Map<Severity, Int> = emptyMap(),
    val selectedLog: LogEntry? = null
) {
    val isEmptyResult: Boolean get() = !isLoading && error == null && groups.isEmpty() && totalCount > 0
}
