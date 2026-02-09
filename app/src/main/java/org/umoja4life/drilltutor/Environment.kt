package org.umoja4life.drilltutor

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    // --- CONCURRENCY ---
    // A global scope for the app environment.
    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // --- SUBSYSTEMS ---
    // The "lateinit" means we promise to initialize this in init() before using it.
    lateinit var settings: SettingsRepository
        private set
    lateinit var flashcards: FlashcardRepository
        private set
    lateinit var playerState: PlayerStateRepository

    // --- INITIALIZATION ---
    fun init(context: Context) {
        // PREVENT MEMORY LEAK:
        // We ensure we only hold the Application Context, not an Activity Context.
        val appContext = context.applicationContext

        logInfo("Environment Initializing...")
        logInfo("Version: $VERSION_NAME (Build $VERSION_CODE)")

        // Instantiate Settings subsystem
        settings = SettingsRepository(appContext)

        // Instantiate Flashcard data subsystem
        flashcards = FlashcardRepository(AssetDataSource(appContext))

        // Instantiate PlayerState subsystem
        playerState = PlayerStateRepository(appContext)

        scope.launch {
            getSettingsAndLoadData()  // get the settings, load the data
        }

        logInfo("Environment Initialized.")
    }

    /**
     * getSettingsAndLoadData
     * Sequential startup logic:
     * 1. Waists for Settings to be read from disk.
     * 2. Loads the correct Flashcard data based on the saved language.
     */
    private suspend fun getSettingsAndLoadData() {
        logInfo("Environment: Waiting for saved settings...")

        // Suspend until DataStore is ready (approx 20-50ms)
        val savedLanguage = settings.awaitLanguageLoad()

        logInfo("Environment: Settings loaded ($savedLanguage). Triggering data load...")
        flashcards.loadFlashcardData(savedLanguage)
    }


    // --- LOGGING UTILITIES ---
    fun logInfo(msg: String) = Log.i(LOG_TAG, msg)
    fun logDebug(msg: String) { if (IS_DEBUG) Log.d(LOG_TAG, "DEBUG: $msg") }
    fun logWarn(msg: String) = Log.w(LOG_TAG, "WARN: $msg")
    fun logError(msg: String) = Log.e(LOG_TAG, "ERROR: $msg")
}