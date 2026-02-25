package org.umoja4life.drilltutor

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class FileDataSource(
    private val context: Context,
    private val storageUriString: String
) : AbstractDataSource() {

    override val TAG = "FileDataSource"

    private val rootUri: Uri = Uri.parse(storageUriString)
    private val rootDocument: DocumentFile? = DocumentFile.fromTreeUri(context, rootUri)

    /**
     * Traverses the DocumentFile tree to locate the requested file and opens an InputStream.
     * @param path Example: "tr/vocabulary.json"
     */
    override suspend fun getInputStream(path: String): InputStream? {
        return withContext(Dispatchers.IO) {
            try {
                if (rootDocument == null || !rootDocument.canRead()) {
                    Environment.logError("$TAG: Cannot read root document for URI: $storageUriString")
                    return@withContext null
                }

                val parts = path.split("/")
                var currentDoc = rootDocument

                // Navigate down the directory tree
                for (part in parts) {
                    currentDoc = currentDoc?.findFile(part)
                    if (currentDoc == null) break
                }

                if (currentDoc != null && currentDoc.isFile && currentDoc.canRead()) {
                    return@withContext context.contentResolver.openInputStream(currentDoc.uri)
                } else {
                    null // File not found, AbstractDataSource handles the optional warning
                }
            } catch (e: Exception) {
                Environment.logError("$TAG: ERROR opening stream for $path: ${e.message}")
                null
            }
        }
    }

    /**
     * Scans the root DocumentFile for two-letter language directories.
     */
    override suspend fun getAvailableLanguages(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (rootDocument == null || !rootDocument.canRead()) {
                    Environment.logError("$TAG: Cannot read root document for languages. URI: $storageUriString")
                    return@withContext emptyList()
                }

                // List items, filter for directories with 2-character names
                val languages = rootDocument.listFiles()
                    .filter { it.isDirectory && it.name?.length == 2 }
                    .mapNotNull { it.name }
                    .sorted()

                Environment.logInfo("$TAG: Discovered languages: $languages")
                languages
            } catch (e: Exception) {
                Environment.logError("$TAG: ERROR listing languages: ${e.message}")
                emptyList()
            }
        }
    }
}