package com.interview.logviewer.presentation.logviewer

import com.interview.logviewer.domain.model.GroupingMode
import com.interview.logviewer.domain.model.Severity
import com.interview.logviewer.domain.usecase.FilterLogsUseCase
import com.interview.logviewer.domain.usecase.GetLogsUseCase
import com.interview.logviewer.domain.usecase.GroupLogsUseCase
import com.interview.logviewer.fake.FakeLogRepository
import com.interview.logviewer.presentation.LogViewerViewModel
import com.interview.logviewer.util.MainDispatcherRule
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class LogViewerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun buildViewModel(repository: FakeLogRepository): LogViewerViewModel =
        LogViewerViewModel(
            getLogsUseCase = GetLogsUseCase(repository),
            filterLogsUseCase = FilterLogsUseCase(),
            groupLogsUseCase = GroupLogsUseCase(),
            // Reuse the same virtual-time scheduler as the Main dispatcher so
            // advanceUntilIdle()/advanceTimeBy() deterministically flush the
            // background filtering/grouping work too.
            defaultDispatcher = mainDispatcherRule.testDispatcher
        )

    private val sampleLogs = listOf(
        FakeLogRepository.sampleLog(id = "1", message = "Connection timed out", tag = "network", severity = Severity.ERROR),
        FakeLogRepository.sampleLog(id = "2", message = "Cache miss", tag = "cache", severity = Severity.FATAL),
        FakeLogRepository.sampleLog(id = "3", message = "User logged in", tag = "auth", severity = Severity.DEBUG, timestamp = Instant.parse("2026-01-23T08:00:00Z"))
    )

    @Test
    fun `initial state is loading`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = buildViewModel(FakeLogRepository(logs = sampleLogs))
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `successful load populates totals, severity counts and groups`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = buildViewModel(FakeLogRepository(logs = sampleLogs))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals(3, state.totalCount)
        assertEquals(3, state.filteredCount)
        assertEquals(1, state.overallSeverityCounts[Severity.ERROR])
        assertEquals(1, state.overallSeverityCounts[Severity.FATAL])
        assertTrue(state.groups.isNotEmpty())
    }

    @Test
    fun `repository failure surfaces an error state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = buildViewModel(FakeLogRepository(shouldFail = true))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.groups.isEmpty())
    }

    @Test
    fun `retry recovers after a prior failure`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeLogRepository(shouldFail = true)
        val viewModel = buildViewModel(repository)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.error)

        repository.setShouldFail(false)
        repository.setLogs(sampleLogs)
        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.error)
        assertEquals(3, state.totalCount)
    }

    @Test
    fun `typing updates the text field immediately but filtering waits for debounce`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = buildViewModel(FakeLogRepository(logs = sampleLogs))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("cache")

        // The raw query reflects every keystroke right away.
        assertEquals("cache", viewModel.uiState.value.searchQuery)
        // But the filtered result set hasn't been recomputed yet (debounce window not elapsed).
        assertEquals(3, viewModel.uiState.value.filteredCount)

        advanceTimeBy(250)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.filteredCount)
        assertEquals(listOf("2"), viewModel.uiState.value.groups.flatMap { it.entries }.map { it.id })
    }

    @Test
    fun `search with no matches yields an empty result state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = buildViewModel(FakeLogRepository(logs = sampleLogs))
        advanceUntilIdle()

        viewModel.onSearchQueryChange("nonexistent term")
        advanceTimeBy(250)
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.filteredCount)
        assertTrue(viewModel.uiState.value.isEmptyResult)
    }

    @Test
    fun `changing grouping mode regroups without waiting for the search debounce`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = buildViewModel(FakeLogRepository(logs = sampleLogs))
        advanceUntilIdle()

        viewModel.onGroupingModeChange(GroupingMode.BY_SESSION)
        advanceUntilIdle()

        assertEquals(GroupingMode.BY_SESSION, viewModel.uiState.value.groupingMode)
        assertTrue(viewModel.uiState.value.groups.all { it.key == "session-666" })
    }

    @Test
    fun `selecting and dismissing a log toggles the details sheet state`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = buildViewModel(FakeLogRepository(logs = sampleLogs))
        advanceUntilIdle()

        val logToSelect = sampleLogs.first()
        viewModel.onLogSelected(logToSelect)
        assertEquals(logToSelect, viewModel.uiState.value.selectedLog)

        viewModel.onDetailsDismissed()
        assertNull(viewModel.uiState.value.selectedLog)
    }
}
