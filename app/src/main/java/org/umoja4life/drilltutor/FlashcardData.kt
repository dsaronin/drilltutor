package org.umoja4life.drilltutor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- 1. THE LOADER (Matches JSON) ---
@Serializable
data class TopicData(
    @SerialName("fc_data")
    val fcData: List<List<String>> = emptyList(),

    // Metadata (Container Level)
    val level: String? = null,
    val recording: String? = null,
    val descriptions: List<String>? = null, // [Title, Desc, Intro]
    val topics: List<String>? = null,

    @SerialName("belongs_to")
    val belongsTo: List<String>? = null
)

// --- 2. THE APP CURRENCY (Simple Tuple) ---
// Just the raw content, exactly like the Ruby [front, back] array
data class FlashcardData(
    val front: String = "",
    val back: String = "",
)