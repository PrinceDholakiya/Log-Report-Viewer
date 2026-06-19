package com.interview.logviewer.domain.repository

import com.interview.logviewer.domain.model.LogEntry

interface LogRepository {
    suspend fun fetchLogs(): List<LogEntry>
}
