package com.webtoapp.core.python

/**
 * Interprets abnormal pip / runtime process exit codes.
 *
 * A shell wrapper reports a signal death as 128+signal; the issue log's
 * `exitCode=159` is 128+31 = SIGSYS — the kernel's seccomp filter killed the
 * bundled musl Python because it issued a system call outside this Android
 * version's allowlist (Android 8.0 introduced seccomp with an old-kernel
 * policy). That is a deterministic environment incompatibility: retrying an
 * identical command only repeats the failure.
 *
 * Kept free of Android dependencies so it is unit-testable on the plain JVM.
 */
object PythonProcessDiagnostics {

    const val SIGHUP = 1
    const val SIGINT = 2
    const val SIGQUIT = 3
    const val SIGILL = 4
    const val SIGTRAP = 5
    const val SIGABRT = 6
    const val SIGBUS = 7
    const val SIGFPE = 8
    const val SIGKILL = 9
    const val SIGSEGV = 11
    const val SIGPIPE = 13
    const val SIGTERM = 15
    const val SIGSYS = 31

    /** Signal number behind a wrapper-reported exit code (128+signal), or null. */
    fun signalForExitCode(exitCode: Int): Int? =
        if (exitCode >= 128) exitCode - 128 else null

    fun signalName(signal: Int): String = when (signal) {
        SIGHUP -> "SIGHUP"
        SIGINT -> "SIGINT"
        SIGQUIT -> "SIGQUIT"
        SIGILL -> "SIGILL"
        SIGTRAP -> "SIGTRAP"
        SIGABRT -> "SIGABRT"
        SIGBUS -> "SIGBUS"
        SIGFPE -> "SIGFPE"
        SIGKILL -> "SIGKILL"
        SIGSEGV -> "SIGSEGV"
        SIGPIPE -> "SIGPIPE"
        SIGTERM -> "SIGTERM"
        SIGSYS -> "SIGSYS"
        else -> "SIGNAL_$signal"
    }

    /**
     * True when the process died from a signal that indicates a deterministic
     * environment problem (not a transient network hiccup), so an identical
     * retry cannot succeed. SIGSYS is the seccomp kill described above.
     */
    fun isDeterministicEnvironmentFailure(exitCode: Int): Boolean =
        signalForExitCode(exitCode) == SIGSYS
}
