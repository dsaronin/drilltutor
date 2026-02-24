package org.umoja4life.drilltutor

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

class AssetDataSource(private val context: Context) : AbstractDataSource() {

    override val TAG = "AssetDataSource"

    /**
     * Provides the InputStream from the Android Assets folder.
     */
    override suspend fun getInputStream(path: String): InputStream? {
        return try {
            context.assets.open(path)
        } catch (e: IOException) {
            // Return null if the file doesn't exist, letting the parent handle the warning
            null
        }
    }

    /**
     * Scans the root assets folder for two-letter language directories.
     */
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