package org.umoja4life.drilltutor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// FIX 1: Renamed from SourceType to FlashcardSource
enum class FlashcardSource(val id: String) {
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
        // Updated return type to FlashcardSource
        fun fromId(id: String): FlashcardSource = entries.find { it.id.equals(id, ignoreCase = true) } ?: UNKNOWN
    }
}

// FIX 3: Added @SerialName to handle the JSON key "fc_data" while keeping Kotlin variable clean.
@Serializable
data class TopicData(
    @SerialName("fc_data")
    val fcData: List<List<String>> = emptyList()
)

// Internal App Data Class
data class FlashcardData(
    val id: String = "",
    val source: FlashcardSource = FlashcardSource.UNKNOWN, // Now matches the Enum name above
    val topic: String = "",
    val front: String = "",
    val back: String = "",
    val notes: String = ""
)