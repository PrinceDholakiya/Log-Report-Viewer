package com.interview.logviewer.domain.usecase

import com.interview.logviewer.domain.model.AiFilter
import com.interview.logviewer.domain.model.LogEntry
import javax.inject.Inject

class FilterLogsUseCase @Inject constructor() {

    operator fun invoke(
        logs: List<LogEntry>,
        query: String,
        aiFilter: AiFilter = AiFilter.ALL
    ): List<LogEntry> {
        val needle = query.trim().lowercase()

        // To filter logs based on AI generated or not.
        return logs
            .filter { entry ->
                when (aiFilter) {
                    AiFilter.ALL -> true
                    AiFilter.AI_GENERATED -> entry.isAiGenerated
                    AiFilter.NOT_AI_GENERATED -> !entry.isAiGenerated
                }
            }

            // To filter logs based on search query.
            .filter { entry ->
                // Check is there anything typed in the search box if no then keep all the entry untouched otherwise check for the needle in the searchable text.
                needle.isEmpty() || entry.searchableText.contains(needle)
            }
    }

}