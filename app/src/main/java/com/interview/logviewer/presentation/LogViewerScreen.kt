package com.interview.logviewer.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.interview.logviewer.domain.model.GroupingMode
import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.presentation.components.GroupHeader
import com.interview.logviewer.presentation.components.LogDetailsSheet
import com.interview.logviewer.presentation.components.LogListItem
import com.interview.logviewer.presentation.components.LogSearchBar
import com.interview.logviewer.presentation.components.SeverityRingIndicator
import com.interview.logviewer.domain.model.AiFilter
import com.interview.logviewer.presentation.components.ShimmerLogItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogViewerScreen(viewModel: LogViewerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("AI Log Viewer") })
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            LogSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            )
            OverviewRow(uiState = uiState, onGroupingModeChange = viewModel::onGroupingModeChange,onAiFilterChange = viewModel::onAiFilterChange)

            HorizontalDivider()
            when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> ErrorState(message = uiState.error!!, onRetry = viewModel::retry)
                uiState.isEmptyResult -> EmptyResultState()
                else -> LogList(uiState = uiState, onLogClick = viewModel::onLogSelected)
            }
        }
    }

    val selectedLog = uiState.selectedLog
    if (selectedLog != null) {
        LogDetailsSheet(
            log = selectedLog,
            sheetState = sheetState,
            onDismiss = viewModel::onDetailsDismissed
        )
    }
}

@Composable
private fun OverviewRow(
    uiState: LogViewerUiState,
    onGroupingModeChange: (GroupingMode) -> Unit,
    onAiFilterChange: (AiFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SeverityRingIndicator(severityCounts = uiState.filteredSeverityCounts, diameter = 72.dp, strokeWidth = 10.dp)

        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
            Text(
                text = "${uiState.filteredCount} of ${uiState.totalCount} logs",
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = "Filter By :",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GroupingMode.entries.forEach { mode ->
                    FilterChip(
                        selected = uiState.groupingMode == mode,
                        onClick = { onGroupingModeChange(mode) },
                        label = { Text(mode.label) }
                    )
                }
                AiFilterDropdown(
                    selected = uiState.aiFilter,
                    onSelect = onAiFilterChange
                )
            }
        }
    }
}

// For AI Search Filter
@Composable
private fun AiFilterDropdown(
    selected: AiFilter,
    onSelect: (AiFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            selected = selected != AiFilter.ALL,
            onClick = { expanded = true },
            label = { Text(selected.label) },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded)
                        Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AiFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.label) },
                    onClick = {
                        onSelect(filter)
                        expanded = false
                    },
                    leadingIcon = if (filter == selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LogList(
    uiState: LogViewerUiState,
    onLogClick: (LogEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        uiState.groups.forEach { group ->
            stickyHeader(key = "header_${group.key}") {
                GroupHeader(group = group)
            }
            items(items = group.entries, key = { it.id }) { log ->
                LogListItem(log = log, onClick = onLogClick)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun LoadingState() {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Show 12 skeleton rows — enough to fill any screen
        items(12) {
            ShimmerLogItem()
            HorizontalDivider()
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    val userFriendlyMessage = when {
        message.contains("ssl", true) ->
            "Secure connection failed. Please check your internet connection and try again."

        message.contains("timeout", true) ->
            "The request took too long. Please try again."

        message.contains("host", true) ->
            "Unable to reach the server. Please check your internet connection."

        message.contains("network", true) ->
            "Network connection unavailable."

        else ->
            "Something went wrong while loading logs."
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {

            Text(
                text = "Unable to Load Logs",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = userFriendlyMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRetry
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun EmptyResultState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "No logs match your search",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
