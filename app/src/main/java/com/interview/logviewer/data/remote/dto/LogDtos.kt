package com.interview.logviewer.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LogResponseDto(
    @SerialName("total_count") val totalCount: Int,
    @SerialName("session_id") val sessionId: String,
    val data: List<LogEntryDto>
)

@Serializable
data class LogEntryDto(
    val id: String,
    val timestamp: String,
    val severity: String,
    val tag: String,
    val message: String,
    val metadata: LogMetadataDto
)

@Serializable
data class LogMetadataDto(
    @SerialName("latency_ms") val latencyMs: Int,
    @SerialName("is_ai_generated") val isAiGenerated: Boolean
)
