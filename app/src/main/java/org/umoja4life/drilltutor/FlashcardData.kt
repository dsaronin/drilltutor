package org.umoja4life.drilltutor

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- DATA MODELS ---

@Serializable
data class TopicData(
    @SerialName("fc_data")
    val fcData: List<List<String>> = emptyList()
)

data class FlashcardData(
    val id: String = "",
    val source: FlashcardSource = FlashcardSource.UNKNOWN,
    val topic: String = "",
    val front: String = "",
    val back: String = "",
    val notes: String = ""
)