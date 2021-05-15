@file:Suppress("MemberVisibilityCanBePrivate")

package com.example.xplor.LOG.ui

import xplor.LOG
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.xplor.LOG.R

// an example of LOG in action..
// for the best experience, enter XPLOR: in your Android Studio logcat Regex
class DemoActivity : AppCompatActivity() {

    init {
        LOG.c()
    }

    private var clickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        LOG.m { "savedInstanceState=$savedInstanceState" }
        super.onCreate(savedInstanceState)
        LOG.s { "setContentView(R.layout.demo_activity)" }
        setContentView(R.layout.demo_activity)
    }

    // NOTE: the ExampleApp.kt uses LOGconfig.EXCLUDE_LOG_PATTERNS to exclude this 'onStart' method from logging.
    override fun onStart() {
        LOG.m() // so this message will not be logged
        super.onStart()
        LOG.s { "and this one is not logged either. No LOG messages inside 'DemoActivity.onStart' get logged now." }
    }

    override fun onResume() {
        LOG.m() // note that entry into onResume is logged normally here.
        super.onResume()
        LOG.w(Exception("this is a warning example.")) { "put additional warning info here." }
        LOG.e(Exception("this is an error example.")) { "put additional error info here." }
    }

    fun testButtonAction(view: View) {
        LOG.m { "CLICK!" }
        ++clickCount
        LOG.s { "clickCount=$clickCount"}
    }

}
