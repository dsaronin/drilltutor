package org.umoja4life.drilltutor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.InputStream

abstract class AbstractDataSource : FlashcardDataSource {

    protected open val TAG = "AbstractDataSource"

    // Configure JSON parser
    protected val jsonParser = Json {
        ignoreUnknownKeys = true // Don't crash on extra fields
        isLenient = true         // Allow slightly malformed JSON
    }

    /**
     * Child classes implement this to provide an InputStream for the given relative path.
     * The base class handles reading, parsing, and closing the stream.
     * @param path Example: "tr/vocabulary.json"
     */
    protected abstract suspend fun getInputStream(path: String): InputStream?

    override suspend fun loadFile(languageCode: String, sourceType: FlashcardSource): Map<String, TopicData>? {
        return withContext(Dispatchers.IO) {
            val filename = "${sourceType.sourceName.lowercase()}.json"
            val path = "$languageCode/$filename"

            Environment.logDebug("$TAG: Attempting to load: $path")

            try {
                val inputStream = getInputStream(path) ?: run {
                    Environment.logWarn("$TAG: File not found (Optional): $path")
                    return@withContext null
                }

                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val data = jsonParser.decodeFromString<Map<String, TopicData>>(jsonString)

                val totalRecords = data.values.sumOf { it.fcData.size }
                Environment.logInfo("$TAG: SUCCESS: Loaded $path. Found ${data.size} topics, $totalRecords total cards.")

                return@withContext data
            } catch (e: Exception) {
                Environment.logError("$TAG: ERROR parsing $path: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    override suspend fun loadTextFile(languageCode: String, sourceType: FlashcardSource): String? {
        return withContext(Dispatchers.IO) {
            val filename = "${sourceType.sourceName.lowercase()}.txt"
            val path = "$languageCode/$filename"

            Environment.logDebug("$TAG: Attempting to load text file: $path")

            try {
                val inputStream = getInputStream(path) ?: run {
                    Environment.logWarn("$TAG: Text file not found (Optional): $path")
                    return@withContext null
                }

                val text = inputStream.bufferedReader().use { it.readText() }
                Environment.logInfo("$TAG: SUCCESS: Loaded text file $path.")
                return@withContext text
            } catch (e: Exception) {
                Environment.logError("$TAG: ERROR reading text file $path: ${e.message}")
                e.printStackTrace()
                null
            }
        }
    }

    /**
     * Scans the data source for available language directories.
     * Child classes should override this with their specific directory scanning logic.
     */
    override suspend fun getAvailableLanguages(): List<String> {
        Environment.logWarn("$TAG: getAvailableLanguages() not overridden. Returning default ['en'].")
        return listOf("en")
    }
}