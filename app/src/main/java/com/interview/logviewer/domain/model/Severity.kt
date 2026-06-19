package com.interview.logviewer.domain.model

enum class Severity(val label: String) {
    FATAL("FATAL"),
    ERROR("ERROR"),
    WARN("WARN"),
    INFO("INFO"),
    DEBUG("DEBUG"),
    UNKNOWN("UNKNOWN");

    companion object {
        val displayOrder = listOf(FATAL, ERROR, WARN, INFO, DEBUG)

        fun fromRaw(raw: String): Severity =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: UNKNOWN
    }
}
