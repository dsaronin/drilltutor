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
    private const val LOG_TAG = APP_NAME

    // --- SUBSYSTEMS ---
    // The "lateinit" means we promise to initialize this in init() before using it.
    lateinit var settings: SettingsRepository
        private set

    // --- INITIALIZATION ---
    fun init(context: Context) {
        logInfo("Environment Initializing...")

        // Instantiate the subsystems
        settings = SettingsRepository(context)

        logInfo("Version: $VERSION_NAME (Build $VERSION_CODE)")

        // --- START TEST HARNESS ---
        CoroutineScope(Dispatchers.IO).launch {
            logInfo("--- STARTING SMOKE TEST ---")
            val testSource = AssetDataSource(context)

            // Test 1: List Languages
            testSource.getAvailableLanguages()

            // Test 2: Load Vocabulary (Expect Success if tr/vocabulary.json exists)
            testSource.loadFile("tr", FlashcardSource.VOCABULARY)

            // Test 3: Load Unknown (Expect Failure/Null)
            testSource.loadFile("tr", FlashcardSource.DICTIONARY)

            logInfo("--- SMOKE TEST COMPLETE ---")
        }
        // --- END TEST HARNESS ---
    }

    // --- LOGGING UTILITIES ---
    fun logInfo(msg: String) = Log.i(LOG_TAG, msg)
    fun logDebug(msg: String) { if (IS_DEBUG) Log.d(LOG_TAG, "DEBUG: $msg") }
    fun logWarn(msg: String) = Log.w(LOG_TAG, "WARN: $msg")
    fun logError(msg: String) = Log.e(LOG_TAG, "ERROR: $msg")
}