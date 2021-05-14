package Xplor.LOG

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
import xplor.LOGConfig

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class LOGTest {

    val message = "this is a message"
    val error = "this is an error"
    val throwable = Throwable(error)

    @Before
    fun setup() {
        mockkStatic(Timber::class)
        Timber.plant(Timber.DebugTree())
    }

    @After fun teardown() {
        Timber.uprootAll()
    }

    @Test
    fun testConfigEnabledAndExcludeNothing() {
        LOGConfig.isEnabled = true
        LOGConfig.isDebug = true
        LOGConfig.EXCLUDE_LOG_PATTERNS = ""

        LOG.w(throwable) { message }
        LOG.d(throwable) { message }
        LOG.e(throwable) { message }

        verify(ordering = Ordering.ORDERED) {
            Timber.w(throwable, any<String>())
            Timber.d(throwable, any<String>())
            Timber.e(throwable, any<String>())
        }
    }

    @Test
    fun testConfigEnabledAndExcludeDebug() {
        LOGConfig.isEnabled = true
        LOGConfig.isDebug = true
        LOGConfig.EXCLUDE_LOG_PATTERNS = "DEBUG"

        LOG.w(throwable) { message }
        LOG.d(throwable) { message }
        LOG.e(throwable) { message }

        verify(exactly = 0) {
            Timber.d(throwable, any<String>())
        }

        verify(exactly = 1) {
            Timber.w(throwable, any<String>())
            Timber.e(throwable, any<String>())
        }
    }

    @Test
    fun testConfigEnabledAndExcludeEverything() {
        LOGConfig.isEnabled = true
        LOGConfig.isDebug = true
        LOGConfig.EXCLUDE_LOG_PATTERNS = "INFO|DEBUG|VERBOSE"

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        verify(exactly = 0) {
            Timber.i(any<Throwable>(), any<String>())
            Timber.d(any<Throwable>(), any<String>())
            Timber.v(any<Throwable>(), any<String>())
        }
    }

    @Test
    fun testConfigEnabledAndIncludeEverything() {
        LOGConfig.isEnabled = true
        LOGConfig.isDebug = true
        LOGConfig.EXCLUDE_LOG_PATTERNS = "~|INFO|DEBUG|VERBOSE"

        LOG.i { message }
        LOG.d { message }
        LOG.v { message }

        verify(exactly = 3) {
            Timber.d(any<Throwable>(), any<String>())
        }
    }
}
