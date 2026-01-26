package org.umoja4life.drilltutor

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException

class AssetDataSource(private val context: Context) : FlashcardDataSource {

    private val TAG = "AssetDataSource"

    // Configure JSON parser
    private val jsonParser = Json {
        ignoreUnknownKeys = true // Don't crash on extra fields
        isLenient = true         // Allow slightly malformed JSON
    }

    override suspend fun loadFile(languageCode: String, sourceType: SourceType): Map<String, TopicData>? {
        return withContext(Dispatchers.IO) {
            // Map Enum to filename: VOCABULARY -> "vocabulary.json"
            val filename = "${sourceType.id.lowercase()}.json"
            val path = "$languageCode/$filename"

            Environment.logDebug("$TAG: Attempting to load: $path")

            try {
                // Open the file from assets
                context.assets.open(path).use { inputStream ->
                    val jsonString = inputStream.bufferedReader().use { it.readText() }

                    // Parse as a Map of "TopicString" -> TopicData
                    val data = jsonParser.decodeFromString<Map<String, TopicData>>(jsonString)

                    // LOGGING: Calculate total records for verification
                    val totalRecords = data.values.sumOf { it.fc_data.size }
                    Environment.logInfo("$TAG: SUCCESS: Loaded $path. Found ${data.size} topics, $totalRecords total cards.")

                    return@withContext data
                }
            } catch (e: IOException) {
                // Expected for optional files (e.g. if dictionary.json doesn't exist)
                Environment.logWarn("$TAG: File not found (Optional): $path")
                null
            } catch (e: Exception) {
                // This is an actual error (e.g. malformed JSON)
                Environment.logError("$TAG: ERROR parsing $path: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun getAvailableLanguages(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                // List all items in the root assets folder
                val languages = context.assets.list("")
                    ?.filter { it.length == 2 && !it.contains(".") }
                    ?.sorted()
                    ?: emptyList()

                Environment.logInfo("$TAG: Discovered languages: $languages")
                return@withContext languages
            } catch (e: IOException) {
                Environment.logError("$TAG: ERROR listing assets: ${e.message}")
                emptyList()
            }
        }
    }
}