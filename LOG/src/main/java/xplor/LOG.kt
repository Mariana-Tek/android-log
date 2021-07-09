@file:Suppress("MemberVisibilityCanBePrivate")

package xplor

import android.annotation.SuppressLint
import timber.log.Timber

//
// this code was adapted from TimberKT by AJ Alt
// see https://github.com/ajalt/timberkt/blob/master/timberkt/src/main/kotlin/com/github/ajalt/timberkt/TimberKt.kt
//

/**
 * A robust [android.util.Log] replacement allowing for build-time filtering of a suite of custom tags.
 * From your app's Application, set `LOGconfig` for run-time filtering of your logcat logs.
 *
 * @see [LOG.c]
 * @see [LOG.m]
 * @see [LOG.s]
 * @see [LOG.p]
 * @see [LOG.v]
 * @see [LOG.s]
 * @see [LOG.i]
 * @see [LOG.w]
 * @see [LOG.e]
 * @see [LOG.wtf]
 */
@Suppress("NOTHING_TO_INLINE")
object LOG {

    // region TAGS!
    const val CREATE  = " CREATE"
    const val METHOD  = " METHOD"
    const val STATE   = "  STATE"
    const val PROVIDE = "PROVIDE"
    const val VERBOSE = "VERBOSE"
    const val DEBUG   = "  DEBUG"
    const val INFO    = "   INFO"
    const val WARNING = "WARNING"
    const val ERROR   = "* ERROR"
    const val WTF     = "*!WTF!*"
    // endregion

    // region Helper vals
    private const val TAG = "XPLOR"
    private const val MAX_LOG_LINE_SIZE = 3900
    private const val NEGATION_FLAG = "~"
    private const val PIPE = '|'
    private const val DOT = '.'
    private const val SPACE = " "
    private val NO_MESSAGE: () -> String = { SPACE }
    private var EXCLUDE_LOG_PATTERNS: MutableList<String> = mutableListOf()
    private var isExcluding: Boolean = false
    /**
     * true if [EXCLUDE_LOG_PATTERNS] should be inverted to actually include patterns instead.
     * Signified by the presence of [NEGATION_FLAG] in EXCLUDE_LOG_PATTERNS
     */
    private var shouldInvertExclude: Boolean = false
    // endregion

    // region Initialization
    init {
        initialize()
    }

    fun initialize() {
        EXCLUDE_LOG_PATTERNS = mutableListOf()
        var negatingFlagExists = false
        isExcluding = LOGconfig.EXCLUDE_LOG_PATTERNS.isNotEmpty()
        // NOTE: only enable EXCLUDE_LOG_PATTERNS for Debug builds
        if (LOGconfig.isDebug && LOGconfig.isEnabled && isExcluding) {
            val excludeList = LOGconfig.EXCLUDE_LOG_PATTERNS.split(PIPE)
            if (excludeList[0] == NEGATION_FLAG) {
                negatingFlagExists = true
            }
            for (excludeItem in excludeList) {
                if (excludeItem.isNotEmpty() && excludeItem != NEGATION_FLAG) {
                    EXCLUDE_LOG_PATTERNS.add(excludeItem)
                }
            }
        }
        shouldInvertExclude = negatingFlagExists
    }
    // endregion

    // region Public API, expected LOG.* methods
    /**
     *  Log PROVIDE the construction of an object. No-ops on Production
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun p(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled && LOGconfig.isDebug && included(t, PROVIDE, message())) {
            log(level = PROVIDE) { makeLogItem(t, message()) }
        }
    }

    /**
     *  Log CREATE the construction of an object. No-ops on Production
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun c(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled && LOGconfig.isDebug && included(t, CREATE, message())) {
            log(level = CREATE) { makeLogItem(t, message()) }
        }
    }

    /**
     *  Log METHOD the entry point of an object method. No-ops on Production
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun m(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled && LOGconfig.isDebug && included(t, METHOD, message())) {
            log(level = METHOD) { makeLogItem(t, message()) }
        }
    }

    /**
     *  Log STATE the state being processed. No-ops on Production
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun s(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled && LOGconfig.isDebug && included(t, STATE, message())) {
            log(level = STATE) { makeLogItem(t, message()) }
        }
    }

    /**
     *  Log a VERBOSE message. No-ops on Production
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun v(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled && LOGconfig.isDebug && included(t, VERBOSE, message())) {
            log(level = VERBOSE) { makeLogItem(t, message()) }
        }
    }

    /**
     *  Log a DEBUG message. No-ops on Production
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */

    fun d(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled && LOGconfig.isDebug && included(t, DEBUG, message())) {
            log(level = DEBUG) { makeLogItem(t, message()) }
        }
    }

    /**
     *  Log an INFO message. No-ops on Production
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun i(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled && LOGconfig.isDebug && included(t, INFO, message())) {
            log(level = INFO) { makeLogItem(t, message()) }
        }
    }

    /**
     *  Log a WARNING message. Production always logs this
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun w(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled) {
            val message = message()
            log(level = WARNING) { makeLogItem(t, message) }
            sentryCallback(t, WARNING, message)
        }
    }

    /**
     *  Log an ERROR message. Production always logs this
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun e(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled) {
            val message = message()
            log(level = ERROR) { makeLogItem(t, message) }
            sentryCallback(t, ERROR, message)
        }
    }

    /**
     *  Log a WTF message. Production always logs this
     *  @param t An optional [Throwable] to log
     *  @param message a message that will be evaluated lazily when the message is printed
     */
    fun wtf(t: Throwable? = null, message: () -> String = NO_MESSAGE) {
        if (LOGconfig.isEnabled) {
            val message = message()
            log(level = WTF) { makeLogItem(t, message) }
            sentryCallback(t, WTF, message)
        }
    }
    // endregion

    // region Internal
    /**
     * log this message <-- everything funnels through here
     *  @param level The SentryLevel log level
     *  @param block The lambda that evaluates to a Pair of (Throwable to message)
     */
    @SuppressLint("TimberExceptionLogging")
    private fun log(level: String = "", block: () -> Pair<Throwable?, String>) {
        if (Timber.treeCount() > 0) {
            val (t, message) = block()
            val tag = getInfo(message != SPACE && t != null, level)
            val logMessage = (tag + message).take(MAX_LOG_LINE_SIZE)
            Timber.tag(TAG)
            when (level) {
                WTF     -> Timber.wtf(t, logMessage)
                ERROR   -> Timber.e(t, logMessage)
                WARNING -> Timber.w(t, logMessage)
                else    -> Timber.d(t, logMessage)
            }
        }
    }

    /**
     * Check if this message is to be logged
     *  @param t The Throwable (if any)
     *  @param level The log level
     *  @param message The extra message to log (if any)
     *
     * NOTE: as the EXCLUDE_LOG_PATTERNS list becomes large, this method can slow execution - EXCLUDE_LOG_PATTERNS is only intended for Debug builds
     * check if the EXCLUDE_LOG_PATTERNS list contains a match for some part of this message
     */
    private fun included(t: Throwable?, level: String, message: String): Boolean {
        val infoList = getInfo(message != SPACE && t != null, level, withDelim = true).split(PIPE)
        for (infoExcludeItem in infoList) {
            for (excludeLogItem in EXCLUDE_LOG_PATTERNS) {
                if (infoExcludeItem.contains(excludeLogItem)) {
                    return shouldInvertExclude
                }
            }
        }
        if (message.isNotEmpty()) {
            for (excludeItem in EXCLUDE_LOG_PATTERNS) {
                if (message.contains(excludeItem)) {
                    return shouldInvertExclude
                }
            }
        }
        return !shouldInvertExclude
    }

    /**
     * Obtain information from the stack about this log message
     *  @param offsetFrame Flag to indicate stack offset -1
     *  @param level The log level
     *  @param withDelim Flag to indicate if call from 'included'
     *
     * Ignoring redundant "${string}" because it makes text light up better.
     */
    @Suppress("RemoveCurlyBracesFromTemplate")
    private fun getInfo(offsetFrame: Boolean, level: String, withDelim: Boolean = false): String {
        val threadName = Thread.currentThread().name
        if (withDelim) {
            val index = if (offsetFrame) 5 else 6
            Thread.currentThread().stackTrace[index].let { caller ->
                return "${level}|${caller.simpleClassName}.${caller.methodName}|- $threadName -|${caller.fileName}"
            }
        }
        else {
            val index = if (offsetFrame) 6 else 7
            Thread.currentThread().stackTrace[index].let { caller ->
                return "${level} ${caller.simpleClassName}.${caller.methodName} - $threadName - (${caller.fileName}:${caller.lineNumber}) "
            }
        }
    }

    private val StackTraceElement.simpleClassName get() = this.className.substringAfterLast(DOT)

    private fun makeLogItem(t: Throwable?, message: String): Pair<Throwable?, String> {
        return t to message
    }
    // endregion

    // region callback for Sentry
    // to integrate with Sentry, change this variable to point to your Sentry logger
    var sentryCallback = SentryCallbackStub.Companion::reportToSentryStub
    // endregion
}

// region Sentry callback Stub
class SentryCallbackStub {
    companion object {
        fun reportToSentryStub(t: Throwable?, tag: String, message: String) {}
    }
}
// endregion

// in your Android Studio, enter XPLOR: into the Regex box for Logcat.  This will filter to show only LOG messages.
object LOGconfig {
    // region configuration

    fun initialize() {
        LOGconfig.isEnabled = true
        LOGconfig.isDebug = false
        LOGconfig.EXCLUDE_LOG_PATTERNS = ""
    }

    var isEnabled = true    // is LOG enabled?
    var isDebug = false     // is BuildConfig.DEBUG set in your app build?

    // modify the LOGconfig.EXCLUDE_LOG_PATTERNS string with log patterns that you want excluded from logging.
    // be sure to separate each item with a vertical bar.

    // EXAMPLES:
    //
    // to exclude all "onStart" and "onResume" methods from being logged
    // LOGconfig.EXCLUDE_LOG_PATTERNS = "onStart|onResume"
    //
    // To exclude a file from logging, add the file name to the EXCLUDE_LOG_PATTERNS
    // To exclude a method add the method name, for example "DemoActivity.onStart" will exclude the onStart in DemoActivity
    // Here is an example to exclude just onStart in DemoActivity:
    // LOGconfig.EXCLUDE_LOG_PATTERNS = "DemoActivity.onStart"
    //
    // if an exclude item is any of:  [ "PROVIDE", "CREATE", "METHOD", "STATE", "VERBOSE", "DEBUG", "INFO" ]
    // then all logging for the indicated log-level state(s) get blocked
    // Note that WARNING, ERROR and WTF can not be filtered like this
    //
    // Here is an example that discards all messages with log levels: PROVIDE, CREATE, METHOD, STATE, and VERBOSE
    // LOGconfig.EXCLUDE_LOG_PATTERNS = "PROVIDE|CREATE|METHOD|STATE|VERBOSE"
    //
    // You can exclude based on the Thread name.
    // Here is an example of discarding all LOG messages on the main Thread:
    // LOGconfig.EXCLUDE_LOG_PATTERNS = "- main -"

    // SPECIAL CASE:
    // it is possible to exclude everything *EXCEPT* some pattern (i.e. make LOG behave to INCLUDE ONLY)
    // This example uses '~' to only show LOG messages from the main Thread and discard everything else:
    // LOGconfig.EXCLUDE_LOG_PATTERNS = "~|- main -"
    //
    // This is how the '~' character works when present in LOGconfig.EXCLUDE_LOG_PATTERNS:
    // If the first exclude item is '~' (tilde) then that inverts the meaning of everything..
    // so all LOG messages EXCEPT what's in the EXCLUDE_LOG_PATTERNS get discarded.
    //
    // in this example we only log the DemoActivity and everything else is discarded
    // LOGconfig.EXCLUDE_LOG_PATTERNS = "~|DemoActivity"

    var EXCLUDE_LOG_PATTERNS = "" // nothing is excluded
    // endregion
}
