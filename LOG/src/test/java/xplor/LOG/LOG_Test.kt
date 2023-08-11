package xplor.LOG

import io.mockk.Ordering
import io.mockk.mockkStatic
import io.mockk.verify
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
    val debugTree = Timber.DebugTree()

    @Before
    fun setup() {
        mockkStatic(Timber::class)
        Timber.plant(debugTree)
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

        verify(ordering = Ordering.ORDERED) {
            Timber.plant(debugTree)
            Timber.treeCount()
            Timber.tag("XPLOR")
            Timber.w(throwable, any<String>())
            Timber.treeCount()
            Timber.tag("XPLOR")
            Timber.d(throwable, any<String>())
            Timber.treeCount()
            Timber.tag("XPLOR")
            Timber.e(throwable, any<String>())
        }
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

        verify(exactly = 0) {
            Timber.d(throwable, any<String>())
        }

        verify(exactly = 1) {
            Timber.plant(debugTree)
            Timber.w(throwable, any<String>())
            Timber.e(throwable, any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 0) {
            Timber.i(any<Throwable>(), any<String>())
            Timber.d(any<Throwable>(), any<String>())
            Timber.v(any<Throwable>(), any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 3) {
            Timber.d(any<Throwable>(), any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 0) {
            Timber.w(throwable, any<String>())
            Timber.d(throwable, any<String>())
            Timber.e(throwable, any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 0) {
            Timber.d(throwable, any<String>())
            Timber.w(throwable, any<String>())
            Timber.e(throwable, any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 0) {
            Timber.i(any<Throwable>(), any<String>())
            Timber.d(any<Throwable>(), any<String>())
            Timber.v(any<Throwable>(), any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 0) {
            Timber.i(any<Throwable>(), any<String>())
            Timber.d(any<Throwable>(), any<String>())
            Timber.v(any<Throwable>(), any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 0) {
            Timber.i(any<Throwable>(), any<String>())
            Timber.d(any<Throwable>(), any<String>())
            Timber.v(any<Throwable>(), any<String>())
        }

        LOGconfig.isEnabled = true
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        verify(exactly = 3) {
            Timber.d(any<Throwable>(), any<String>())
        }

        LOGconfig.isEnabled = false
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        verify(exactly = 3) {
            Timber.d(any<Throwable>(), any<String>())
        }

        LOGconfig.isEnabled = true
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        verify(exactly = 6) {
            Timber.d(any<Throwable>(), any<String>())
        }
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

        verify(exactly = 1) {
            Timber.plant(debugTree)
        }

        verify(exactly = 0) {
            Timber.i(any<Throwable>(), any<String>())
            Timber.d(any<Throwable>(), any<String>())
            Timber.v(any<Throwable>(), any<String>())
        }

        LOGconfig.EXCLUDE_LOG_PATTERNS = "INFO|VERBOSE"
        LOG.initialize()

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        verify(ordering = Ordering.ORDERED) {
            Timber.plant(debugTree)
            Timber.treeCount()
            Timber.tag("XPLOR")
            Timber.d(any<Throwable>(), any<String>())
        }
    }
}
