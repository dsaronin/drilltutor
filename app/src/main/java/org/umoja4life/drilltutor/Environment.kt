package org.umoja4life.drilltutor

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Environment
 * The Central Nervous System.
 * - Identity (Version, Name)
 * - Service Locator (Access to Settings)
 * - Logging Utility
 */
object Environment {

    // --- IDENTITY ---
    const val APP_NAME = "DrillTutor"
    val VERSION_NAME: String = BuildConfig.VERSION_NAME
    val VERSION_CODE: String = BuildConfig.DRILLTUTOR_VERSION_CODE
    val IS_DEBUG: Boolean = BuildConfig.DEBUG

    // --- LOGGING ---
    private const val LOG_TAG = "App" + APP_NAME

    // --- SUBSYSTEMS ---
    // The "lateinit" means we promise to initialize this in init() before using it.
    lateinit var settings: SettingsRepository
        private set
    lateinit var flashcards: FlashcardRepository
        private set

    // --- INITIALIZATION ---
    fun init(context: Context) {
        logInfo("Environment Initializing...")
        logInfo("Version: $VERSION_NAME (Build $VERSION_CODE)")

        // Instantiate Settings subsystem
        settings = SettingsRepository(context)

        // Instantiate Flashcard data subsystem
        flashcards = FlashcardRepository(AssetDataSource(context))

        // load master flashcard data for given language; assume "tr" for now
        flashcards.loadMasterData("tr")

    }

    // --- LOGGING UTILITIES ---
    fun logInfo(msg: String) = Log.i(LOG_TAG, msg)
    fun logDebug(msg: String) { if (IS_DEBUG) Log.d(LOG_TAG, "DEBUG: $msg") }
    fun logWarn(msg: String) = Log.w(LOG_TAG, "WARN: $msg")
    fun logError(msg: String) = Log.e(LOG_TAG, "ERROR: $msg")
}