package com.interview.logviewer.domain.model

import java.time.Instant

data class LogEntry(
    val id: String,
    val timestamp: Instant,
    val severity: Severity,
    val tag: String,
    val message: String,
    val sessionId: String,
    val latencyMs: Int,
    val isAiGenerated: Boolean,
    val searchableText: String
)
