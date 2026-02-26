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

    // **********************************************************************
    // ******* CLASS-LEVEL FUNCTIONS  ***************************************
    // **********************************************************************
    companion object {
        /**
         * isValidSource
         * Validates if the provided SAF URI string represents a structurally sound DrillTutor directory.
         */
        fun isValidSource(context: Context, uriString: String): Boolean {
            return try {
                val uri = android.net.Uri.parse(uriString)
                val rootDoc = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, uri)

                if (rootDoc != null && rootDoc.isDirectory && rootDoc.canRead()) {
                    containsValidLanguageFolder(rootDoc)
                } else {
                    false
                }
            } catch (e: Exception) {
                Environment.logError("FileDataSource: Validation failed for URI: $uriString. ${e.message}")
                false
            }
        }

        /**
         * containsValidLanguageFolder
         * Iterates through subdirectories looking for at least one valid language folder.
         */
        private fun containsValidLanguageFolder(rootDoc: androidx.documentfile.provider.DocumentFile): Boolean {
            for (file in rootDoc.listFiles()) {
                val dirName = file.name
                if (file.isDirectory && dirName != null && dirName.matches(Environment.REGEX_LANG_DIR)) {
                    if (hasRequiredDataFiles(file)) {
                        return true // Short-circuit: we found at least one valid language
                    }
                }
            }
            return false
        }

        /**
         * hasRequiredDataFiles
         * Verifies the presence of minimum required JSON files within a language folder.
         */
        private fun hasRequiredDataFiles(langFolder: androidx.documentfile.provider.DocumentFile): Boolean {
            return langFolder.hasFile(Environment.FILE_VOCABULARY) &&
                    langFolder.hasFile(Environment.FILE_LESSONS)
        }

        private fun androidx.documentfile.provider.DocumentFile.hasFile(filename: String): Boolean {
            return this.findFile(filename)?.let { it.isFile && it.canRead() } ?: false
        }

    }  // end companion object

    // **********************************************************************

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