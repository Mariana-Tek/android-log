package com.xplor.log

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import timber.log.Timber
import xplor.LOG
import xplor.LOGconfig

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LOGTest {

    val message = "this is a message"
    val error = "this is an error"
    val throwable = Throwable(error)

    lateinit var logs: MutableList<String>

    @Before
    fun setup() {
        logs = mutableListOf()
        Timber.plant(object : Timber.DebugTree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                logs.add("$priority $tag ${message.split('\n')[0]}")
            }
        })
        LOGconfig.initialize()
    }

    @After fun teardown() {
        Timber.uprootAll()
    }

    @Test
    fun testConfigEnabledAndExcludeNothing() {
        LOGconfig.isEnabled = true
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = ""
        LOG.initialize()

        LOG.w(throwable) { message }
        LOG.d(throwable) { message }
        LOG.e(throwable) { message }

        assertThat(logs).containsExactly(
            "5 XPLOR WARNING NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "3 XPLOR   DEBUG NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "6 XPLOR * ERROR NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message"
        )
    }

    @Test
    fun testConfigEnabledAndExcludeDebug() {
        LOGconfig.isEnabled = true
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "DEBUG"
        LOG.initialize()

        LOG.w(throwable) { message }
        LOG.d(throwable) { message }
        LOG.e(throwable) { message }

        assertThat(logs).containsExactly(
            "5 XPLOR WARNING NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "6 XPLOR * ERROR NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message"
        )
    }

    @Test
    fun testConfigEnabledAndExcludeEverything() {
        LOGconfig.isEnabled = true
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "INFO|DEBUG|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assert(logs.size == 0)
    }

    @Test
    fun testConfigEnabledAndIncludeEverything() {
        LOGconfig.isEnabled = true
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "~|INFO|DEBUG|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assertThat(logs).containsExactly(
            "3 XPLOR    INFO NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "3 XPLOR   DEBUG NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "3 XPLOR VERBOSE NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message"
        )
    }

    @Test
    fun testConfigDisabledAndExcludeNothing() {
        LOGconfig.isEnabled = false
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = ""
        LOG.initialize()

        LOG.w(throwable) { message }
        LOG.d(throwable) { message }
        LOG.e(throwable) { message }

        assert(logs.size == 0)
    }

    @Test
    fun testConfigDisabledAndExcludeDebug() {
        LOGconfig.isEnabled = false
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "DEBUG"
        LOG.initialize()

        LOG.w(throwable) { message }
        LOG.d(throwable) { message }
        LOG.e(throwable) { message }

        assert(logs.size == 0)
    }

    @Test
    fun testConfigDisabledAndExcludeEverything() {
        LOGconfig.isEnabled = false
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "INFO|DEBUG|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assert(logs.size == 0)
    }

    @Test
    fun testConfigDisabledAndIncludeEverything() {
        LOGconfig.isEnabled = false
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "~|INFO|DEBUG|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assert(logs.size == 0)
    }

    @Test
    fun testDynamicConfigurationChanges() {
        LOGconfig.isEnabled = false
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "~|INFO|DEBUG|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assert(logs.size == 0)

        LOGconfig.isEnabled = true
        LOG.initialize()
        logs = mutableListOf()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assertThat(logs).containsExactly(
            "3 XPLOR    INFO NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "3 XPLOR   DEBUG NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "3 XPLOR VERBOSE NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message"
        )

        LOGconfig.isEnabled = false
        LOG.initialize()
        logs = mutableListOf()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assert(logs.size == 0)

        LOGconfig.isEnabled = true
        LOG.initialize()
        logs = mutableListOf()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assertThat(logs).containsExactly(
            "3 XPLOR    INFO NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "3 XPLOR   DEBUG NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message",
            "3 XPLOR VERBOSE NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message"
        )
    }

    @Test
    fun testDynamicExclusions() {
        LOGconfig.isEnabled = true
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "INFO|DEBUG|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assert(logs.size == 0)

        LOGconfig.EXCLUDE_LOG_PATTERNS = "INFO|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        assertThat(logs).containsExactly(
            "3 XPLOR   DEBUG NativeMethodAccessorImpl.invoke0 - SDK 26 Main Thread - (null:-2) this is a message"
        )
    }
}
