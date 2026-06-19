package com.interview.logviewer.domain.usecase

import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.domain.repository.LogRepository
import javax.inject.Inject

class GetLogsUseCase @Inject constructor(
    private val repository: LogRepository
) {
    suspend operator fun invoke(): List<LogEntry> = repository.fetchLogs()
}
