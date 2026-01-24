package org.umoja4life.drilltutor

import android.app.Application

class DrillTutorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Wake up the Environment
        Environment.init()
    }
}