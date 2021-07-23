![LOG](logo.png)

This is an extensible Kotlin Android logger which provides utility on top of Jake Warton's [Timber](https://github.com/JakeWharton/timber) logger library.

LOG aims to make logs easily readable, filterable and navigable. We achieve readability by enforcing a line format. Each LOG line uses a fixed size
for the date, time and package name. This is followed by the LOG level, the class and method, the active thread, a file name:line number, and any extra information.
LOG automatically creates most of these elements for you. The file name portion is clickable and will navigate to the file at a line number.
A lambda can be associated with each LOG line to generate and include extra information. LOG lines are automatically truncated at 3900 characters to avoid
issues with Timber or the underlying android.util.Log class. LOG is easily integrated with [Sentry](https://sentry.io).

LOG levels are extensible. Currently 10 predefined LOG levels exist. These are:

 *  LOG.c   - **CREATE** (object creation from an init {} block)
 *  LOG.m   - **METHOD** (entry into a method)
 *  LOG.s   - **STATE** (a state change)
 *  LOG.p   - **PROVIDE** (Dagger2 @PROVIDES execution)
 *  LOG.v   - **VERBOSE** (a Verbose message)
 *  LOG.d   - **DEBUG** (a Debug message)
 *  LOG.i   - **INFO** (an Info message)
 *  LOG.w   - **WARNING** (a Warning message)
 *  LOG.e   - **ERROR** (an Error)
 *  LOG.wtf - **WTF** (What a Terrible Failure)

LOG lines are filterable. The object **LOGconfig** exists to configure LOG behavior.
LOGconfig filters by removing LOG lines that match a set of patterns, or by excluding lines that don't match a set of patterns.
Note that WARNING, ERROR and WTF messages can not be filtered.

Usage
-----

It is simple to use LOG in your application. We recommend adding **LOG.m()** as the first line to each method.
We also recommend adding an **init {}** block to each object. Inside your init block, first call **LOG.c()**. That will
LOG every time you CREATE that object. If you are using Dagger2 (or another dependency injection framework) then
use **LOG.p()** to log when the object is PROVIDED.

Each LOG statement can have an associated lambda that generates additional information for the message. For example, when
entering a method, **LOG.m()** will just log entry into the method.  But if you want to also include the method's arguments, then
use this syntax: **LOG.m { "argument1=$argument1, argument2=$argument2, ..." }**. Note you don't need parenthesis when supplying a lambda.

Each LOG statement can have an optional *Throwable* parameter that will also log the Exception. Here are some examples showing the various syntax forms.
These examples are applicable to all LOG levels (not just DEBUG):

        LOG.d() // log a simple DEBUG message
        LOG.d { "additional $information" } // the 'information' object is included
        LOG.d(Exception("this is an excepton!")) // log an exeption
        // log an Exception and also include the 'information' object
        LOG.d(Exception("this is an excepton!")) { "additional $information" }

LOG requires the *Timber* library. Your build.gradle must include Timber:

```groovy
implementation 'com.jakewharton.timber:timber:4.7.1+'
```

Additionally you need to specify a **LOGconfig** configuration to activate logging (and optionally filtering).
The **Configuration** and **Filtering** sections below explain how to do this setup using detailed examples.

Configuration
-------------

LOGconfig lets you configure LOG behavior.

 * The flag **LOGConfig.isEnabled** lets you enable or disable LOG easily
 * The flag **LOGConfig.isDebug** lets you enable advanced debugging
 * The String **LOGConfig.EXCLUDE_LOG_PATTERNS** defines patterns for filtering with advanced debugging

Note that when enabling LOG, you also need a **Timber Tree** planted before output will appear.
You should create an *Application* class that initializes the logger.  The example below shows how to setup your Application for both Release and Debug logging.
If enabled, *Release* logging defaults to only showing ERROR, WARNING and WTF messages. *Debug* logging is powerful with custom levels and filtering capability.

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

Filtering
---------

LOG messages are filtered based on rules specified in the **LOGconfig.EXCLUDE_LOG_PATTERNS** object.
Modify the LOGconfig.EXCLUDE_LOG_PATTERNS to have the patterns that you want *excluded* from logging,
or alternatively configure it with patterns to *include only* when logging. **Separate each item with a vertical bar.**

For example, to exclude all "onStart" and "onResume" methods from being logged everywhere:

        LOGconfig.EXCLUDE_LOG_PATTERNS = "onStart|onResume"

To exclude a file from logging, add the *file name* to the EXCLUDE_LOG_PATTERNS.  To exclude a method add the *method name*.
For example "DemoActivity.onStart" will exclude the onStart method only in DemoActivity. Whereas just "DemoActivity" will exclude
all logging from the DemoActivity.

        LOGconfig.EXCLUDE_LOG_PATTERNS = "DemoActivity.onStart"

If an exclude item is any of:  [ "PROVIDE", "CREATE", "METHOD", "STATE", "VERBOSE", "DEBUG", "INFO" ]
then all logging for the indicated log-level state(s) get blocked.  Note that WARNING, ERROR and WTF can not be filtered like this

This example below discards all messages with log levels: PROVIDE, CREATE, METHOD, STATE, and VERBOSE

        LOGconfig.EXCLUDE_LOG_PATTERNS = "PROVIDE|CREATE|METHOD|STATE|VERBOSE"

You can exclude based on the Thread name.  This example discards all LOG messages originating on the main Thread:

        LOGconfig.EXCLUDE_LOG_PATTERNS = "- main -"

The meaning for EXCLUDE_LOG_PATTERNS is inverted if the first item is a tilde ('\~') character.
It is possible to exclude everything *except* some pattern (i.e. make LOG behave to INCLUDE ONLY)
This example uses '\~' to only show LOG messages from the main Thread and discard everything else:

        LOGconfig.EXCLUDE_LOG_PATTERNS = "~|- main -"

This last example only logs the DemoActivity and everything else is discarded:

        LOGconfig.EXCLUDE_LOG_PATTERNS = "~|DemoActivity"

Dynamic Reconfiguration
-----------------------

LOG behavior is dynamically reconfigurable.  For example, you may only want to enable logging in specific sections of your app;
and those sections may use different LOG configuration values.  To do this, when your app first starts, disable LOG in the Application:

        LOGconfig.isEnabled = false
        LOG.initialize()

Then when the code enters a critical section, initialize LOG as desired. For example:

        LOGconfig.isEnabled = true
        LOGconfig.isDebug = true
        LOGconfig.EXCLUDE_LOG_PATTERNS = "~|INFO|DEBUG|VERBOSE"
        LOG.initialize()

When you leave a critical section, disable LOG again, like this:

        LOGconfig.isEnabled = false
        LOG.initialize()

Sentry Integration
------------------

This logger is easily integrated with [Sentry](https://sentry.io).  For Sentry integration, set the LOG variable 'theSentryCallback'
to point to your Sentry implementation.  The example below shows a possible implementation.  You may also want to add a default exception handler.
Note that only ERROR and WTF messages are currently sent to Sentry.

    class SentryHandler() {

        companion object {

            fun initializeSentry(clientName: String, environment: String, dsn: String) {
                LOG.m()
                LOG.theSentryCallback = Companion::reportToSentry
                val options = SentryOptions()
                options.dsn = dsn
                options.isAttachStacktrace = true
                options.enableUncaughtExceptionHandler = true
                options.setDebug(BuildConfig.DEBUG)
                options.environment = environment
                options.sentryClientName = clientName
                options.serverName = clientName
                Sentry.init(options)
            }

            fun reportToSentry(t: Throwable?, tag: String, message: String) {
                LOG.m()
                if (Sentry.isEnabled()) {
                    Sentry.setTag("info", tag)
                    Sentry.setTag("message", message)
                    val sentryMessage = Message()
                    sentryMessage.message = message
                    val event = SentryEvent()
                    if (t != null) {
                        val sentryException = SentryException()
                        sentryException.threadId = Thread.currentThread().id
                        sentryException.value = t.message
                        //sentryException.stacktrace = getSentryStackTrace()
                        event.exceptions = listOf(sentryException)
                    }
                    event.level = when (tag) {
                        LOG.WTF -> SentryLevel.FATAL
                        LOG.ERROR -> SentryLevel.ERROR
                        LOG.WARN -> SentryLevel.WARNING
                        LOG.DEBUG -> SentryLevel.DEBUG
                        else -> SentryLevel.INFO
                    }
                    event.message = sentryMessage
                    addBreadcrumbs(event)
                    Sentry.captureEvent(event)
                }
            }

            // Capture the last 50 lines of logcat to use as Breadcrumbs
            private fun addBreadcrumbs(event: SentryEvent) {
                LOG.m()
                try {
                    val process = Runtime.getRuntime()
                        .exec("logcat -t 50 --pid ${android.os.Process.myPid()} *:D")
                    val lines = process.inputStream.bufferedReader().use { it.readText() }.split("\n")
                    if (lines.size > 0) {
                        for (line in lines) {
                            event.addBreadcrumb(line)
                        }
                    }
                }
                catch(e: Exception) {
                    // unable to run logcat process.  Testing?
                }
            }
        }
    }

Example
-------

Below is Android Studio logcat output produced from the included LOG-sample project:

        2021-05-28 08:54:30.326 15974-15974/com.example.xplor.LOG D/XPLOR:   DEBUG ExampleApp.onCreate - main - (ExampleApp.kt:24) --------------------------------------------------------------------------------
        2021-05-28 08:54:30.326 15974-15974/com.example.xplor.LOG D/XPLOR:   DEBUG ExampleApp.onCreate - main - (ExampleApp.kt:25) DEBUG ENABLED for 1.0.0
        2021-05-28 08:54:30.339 15974-15974/com.example.xplor.LOG D/XPLOR:  CREATE DemoActivity.<init> - main - (DemoActivity.kt:16)  
        2021-05-28 08:54:30.361 15974-15974/com.example.xplor.LOG D/XPLOR:  METHOD DemoActivity.onCreate - main - (DemoActivity.kt:22) savedInstanceState=null
        2021-05-28 08:54:30.375 15974-15974/com.example.xplor.LOG D/XPLOR:   STATE DemoActivity.onCreate - main - (DemoActivity.kt:24) setContentView(R.layout.demo_activity)
        2021-05-28 08:54:30.447 15974-15974/com.example.xplor.LOG D/XPLOR:  METHOD DemoActivity.onResume - main - (DemoActivity.kt:36)  
        2021-05-28 08:54:30.449 15974-15974/com.example.xplor.LOG W/XPLOR: WARNING DemoActivity.onResume - main - (DemoActivity.kt:38) put additional warning info here.
            java.lang.Exception: this is a warning example.
                at com.example.xplor.LOG.ui.DemoActivity.onResume(DemoActivity.kt:38)
                at android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1354)
                at android.app.Activity.performResume(Activity.java:7079)
                at android.app.ActivityThread.performResumeActivity(ActivityThread.java:3620)
                at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3685)
                at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2898)
                at android.app.ActivityThread.-wrap11(Unknown Source:0)
                at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1593)
                at android.os.Handler.dispatchMessage(Handler.java:105)
                at android.os.Looper.loop(Looper.java:164)
                at android.app.ActivityThread.main(ActivityThread.java:6541)
                at java.lang.reflect.Method.invoke(Native Method)
                at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:767)
        2021-05-28 08:54:30.450 15974-15974/com.example.xplor.LOG E/XPLOR: * ERROR DemoActivity.onResume - main - (DemoActivity.kt:39) put additional error info here.
            java.lang.Exception: this is an error example.
                at com.example.xplor.LOG.ui.DemoActivity.onResume(DemoActivity.kt:39)
                at android.app.Instrumentation.callActivityOnResume(Instrumentation.java:1354)
                at android.app.Activity.performResume(Activity.java:7079)
                at android.app.ActivityThread.performResumeActivity(ActivityThread.java:3620)
                at android.app.ActivityThread.handleResumeActivity(ActivityThread.java:3685)
                at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2898)
                at android.app.ActivityThread.-wrap11(Unknown Source:0)
                at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1593)
                at android.os.Handler.dispatchMessage(Handler.java:105)
                at android.os.Looper.loop(Looper.java:164)
                at android.app.ActivityThread.main(ActivityThread.java:6541)
                at java.lang.reflect.Method.invoke(Native Method)
                at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:240)
                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:767)
        2021-05-28 09:01:58.286 15974-15974/com.example.xplor.LOG D/XPLOR:  METHOD DemoActivity.testButtonAction - main - (DemoActivity.kt:43) CLICK!
        2021-05-28 09:01:58.286 15974-15974/com.example.xplor.LOG D/XPLOR:   STATE DemoActivity.testButtonAction - main - (DemoActivity.kt:45) clickCount=1
        2021-05-28 09:01:59.555 15974-15974/com.example.xplor.LOG D/XPLOR:  METHOD DemoActivity.testButtonAction - main - (DemoActivity.kt:43) CLICK!
        2021-05-28 09:01:59.556 15974-15974/com.example.xplor.LOG D/XPLOR:   STATE DemoActivity.testButtonAction - main - (DemoActivity.kt:45) clickCount=2

If you want to build the LOG-sample project for Release testing, you need to generate a keystore and modify the LOG-sample/build.gradle to include that.
Use this command to generate a Release keystore for testing:

        keytool -genkey -v -keystore test.keystore -alias alias_name -keyalg RSA -sigalg SHA1withRSA -keysize 2048 -validity 10000

Known Issues
------------

The use of *inline* and *reified* produce invalid line numbers in LOG.
The issue is documented here: https://youtrack.jetbrains.com/issue/KT-12896
and here: https://youtrack.jetbrains.com/issue/KT-28542

License
-------

    MIT License
    
    Copyright (c) 2021 Mariana-Tek at Xplor
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.

