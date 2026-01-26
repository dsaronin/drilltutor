package org.umoja4life.drilltutor

import kotlinx.serialization.Serializable

// Enum remains unchanged
enum class SourceType(val id: String) {
    VOCABULARY("Vocabulary"),
    SENTENCES("Sentences"),
    OPPOSITES("Opposites"),
    PHRASES("Phrases"),
    DIALOGS("Dialogs"),
    READINGS("Readings"),
    GLOSSARIES("Glossaries"),
    DICTIONARY("Dictionary"),
    UNKNOWN("Unknown");

    companion object {
        fun fromId(id: String): SourceType = entries.find { it.id.equals(id, ignoreCase = true) } ?: UNKNOWN
    }
}

// FIX: This class models the VALUE inside the map.
// matches: { "fc_data": [ ["front", "back"], ... ] }
@Serializable
data class TopicData(
    val fc_data: List<List<String>> = emptyList()
)

// Internal App Data Class (Unchanged)
data class FlashcardData(
    val id: String = "",
    val source: SourceType = SourceType.UNKNOWN,
    val topic: String = "",
    val front: String = "",
    val back: String = "",
    val notes: String = ""
)