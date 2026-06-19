package com.interview.logviewer.data.repository

import com.interview.logviewer.data.mapper.toDomain
import com.interview.logviewer.data.remote.LogApiService
import com.interview.logviewer.domain.model.LogEntry
import com.interview.logviewer.domain.repository.LogRepository
import javax.inject.Inject

class LogRepositoryImpl @Inject constructor(
    private val api: LogApiService
) : LogRepository {

    // Survives for the lifetime of the singleton repository (i.e. the app process)
    private var cache: List<LogEntry>? = null

    override suspend fun fetchLogs(): List<LogEntry> {
        // Return cached data immediately if available
        cache?.let { return it }

        // Otherwise hit the network, cache, and return
        return api.getLogs()
            .toDomain()
            .also { cache = it }
    }
}
