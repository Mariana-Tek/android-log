@file:Suppress("unused")

package com.example.xplor.LOG

import xplor.LOG
import xplor.LOGConfig
import android.app.Application
import timber.log.Timber

open class ExampleApp: Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            LOGConfig.isEnabled = true
            LOGConfig.isDebug = true
            LOGConfig.EXCLUDE_LOG_PATTERNS = "DemoActivity.onStart" // example of excluding LOG messages

            Timber.plant(Timber.DebugTree())
            LOG.d { "-".repeat(80) }
            LOG.d { "DEBUG ENABLED for " + BuildConfig.VERSION_NAME }
        }
    }
}
