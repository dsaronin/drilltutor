package org.umoja4life.drilltutor

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

// --- THE LOADER (Matches JSON) ---
@Serializable
data class TopicData(
    @SerialName("fc_data")
    val fcData: List<FlashcardData> = emptyList(),

    // Metadata (Container Level)
    val level: String? = null,
    val recording: String? = null,
    val descriptions: List<String>? = null, // [Title, Desc, Intro]
    val topics: List<String>? = null,

    @SerialName("belongs_to")
    val belongsTo: List<String>? = null
)

// --- THE APP CURRENCY (Simple Tuple) ---
// ANNOTATION: Point to the custom serializer defined below
@Serializable(with = FlashcardDataSerializer::class)
data class FlashcardData(
    val front: String = "",
    val back: String = ""
)

// --- CUSTOM SERIALIZER ---
// Handles converting ["front", "back"] Array <-> FlashcardData Object
object FlashcardDataSerializer : KSerializer<FlashcardData> {

    // Boilerplate: Describes the structure to the framework
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FlashcardData")

    // CONVERT JSON -> KOTLIN
    override fun deserialize(decoder: Decoder): FlashcardData {
        // 1. Get the raw JSON element (The Array)
        val input = decoder as? JsonDecoder ?: throw IllegalStateException("Expected JsonDecoder")
        val element = input.decodeJsonElement()

        // 2. Cast to Array
        // Input is: ["az", "few"]
        val array = element.jsonArray

        // 3. Map by Index (0 = Front, 1 = Back)
        val front = array[0].jsonPrimitive.content
        val back = if (array.size > 1) array[1].jsonPrimitive.content else "" // Safety check

        return FlashcardData(front, back)
    }

    // CONVERT KOTLIN -> JSON (For saving later)
    override fun serialize(encoder: Encoder, value: FlashcardData) {
        val output = encoder as? JsonEncoder ?: throw IllegalStateException("Expected JsonEncoder")

        // Build a JSON Array: [ "front", "back" ]
        val jsonArray = buildJsonArray {
            add(value.front)
            add(value.back)
        }

        output.encodeJsonElement(jsonArray)
    }
}