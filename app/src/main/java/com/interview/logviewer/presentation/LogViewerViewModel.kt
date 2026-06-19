package com.interview.logviewer.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interview.logviewer.presentation.LogViewerUiState
import com.interview.logviewer.di.DefaultDispatcher
import com.interview.logviewer.domain.model.AiFilter
import com.interview.logviewer.domain.model.GroupingMode
import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.domain.repository.LogRepository
import com.interview.logviewer.domain.usecase.FilterLogsUseCase
import com.interview.logviewer.domain.usecase.GetLogsUseCase
import com.interview.logviewer.domain.usecase.GroupLogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewerViewModel @Inject constructor(
    private val getLogsUseCase: GetLogsUseCase,
    private val filterLogsUseCase: FilterLogsUseCase,
    private val groupLogsUseCase: GroupLogsUseCase,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogViewerUiState())
    val uiState: StateFlow<LogViewerUiState> = _uiState.asStateFlow()

    /** Raw, un-debounced query — drives the text field so typing always feels instant. */
    private val searchQuery = MutableStateFlow("")

    /** Full unfiltered dataset, kept outside the StateFlow since the UI never renders it directly. */
    private var allLogs: List<LogEntry> = emptyList()

    init {
        loadLogs()
        observeSearchQuery()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onGroupingModeChange(mode: GroupingMode) {
        applyFilterAndGroup(searchQuery.value, mode)
    }

    fun onAiFilterChange(filter: AiFilter) {
        applyFilterAndGroup(searchQuery.value, _uiState.value.groupingMode, filter)
    }

    fun onLogSelected(log: LogEntry) {
        _uiState.update { it.copy(selectedLog = log) }
    }

    fun onDetailsDismissed() {
        _uiState.update { it.copy(selectedLog = null) }
    }

    fun retry() {
        loadLogs()
    }


    private fun loadLogs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getLogsUseCase() }
                .onSuccess { logs ->
                    allLogs = logs
                    val overallCounts = logs.groupingBy { it.severity }.eachCount()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            totalCount = logs.size,
                            overallSeverityCounts = overallCounts
                        )
                    }
                    applyFilterAndGroup(searchQuery.value, _uiState.value.groupingMode)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Something went wrong while loading logs."
                        )
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        searchQuery
            .debounce(200)
            .distinctUntilChanged()
            .onEach { query -> applyFilterAndGroup(query, _uiState.value.groupingMode) }
            .launchIn(viewModelScope)
    }

    private fun applyFilterAndGroup(
        query: String,
        mode: GroupingMode,
        aiFilter: AiFilter = _uiState.value.aiFilter   // defaults to whatever is already selected
    ) {
        viewModelScope.launch(defaultDispatcher) {
            val filtered = filterLogsUseCase(allLogs, query, aiFilter)
            val groups = groupLogsUseCase(filtered, mode)
            _uiState.update {
                it.copy(
                    groups = groups,
                    filteredCount = filtered.size,
                    groupingMode = mode,
                    aiFilter = aiFilter              // ← persist selection in state
                )
            }
        }
    }
}
