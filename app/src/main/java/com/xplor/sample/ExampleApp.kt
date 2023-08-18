package com.xplor.sample

import xplor.LOG
import xplor.LOGconfig
import android.app.Application
import timber.log.Timber

open class ExampleApp: Application() {

    override fun onCreate() {
        super.onCreate()

        // ensure release builds always report WARNING, ERROR and WTF
        LOGconfig.isEnabled = true
        Timber.plant(Timber.DebugTree())

        // debug builds enable advanced logging capability
        if (BuildConfig.DEBUG) {
            LOGconfig.isDebug = true
            LOGconfig.EXCLUDE_LOG_PATTERNS = "DemoActivity.onStart" // example of excluding LOG messages

            LOG.d { "-".repeat(80) }
            LOG.d { "DEBUG ENABLED for " + BuildConfig.VERSION_NAME }
        }
    }
}
