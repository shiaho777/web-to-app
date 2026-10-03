package com.webtoapp.core.linux

import java.io.File
import kotlin.jvm.functions.Function1

/**
 * Launch fork+exec runtimes with the right channel for the current build:
 * plain ProcessBuilder when execve works (server-runtime exports pinned to
 * targetSdk 28), or the user-mode exec loader under host W^X (targetSdk>=29).
 *
 * The loader class stays host-only. This file is shell-synced, so the W^X
 * path looks it up by name and degrades when the loader is not in the APK.
 */
object HostProcessLauncher {

    data class Result(
        val process: Process?,
        val error: String?
    )

    fun start(
        context: android.content.Context,
        command: List<String>,
        env: Map<String, String>,
        cwd: File?,
        runtimeLabel: String = "PHP"
    ): Result {
        val wxRestricted = !RuntimeExecPolicy.canExecAppDataBinaries(context)
        if (!wxRestricted) {
            val pb = ProcessBuilder(command)
            if (cwd != null) pb.directory(cwd)
            val pbEnv = pb.environment()
            env.forEach { (k, v) -> pbEnv[k] = v }
            return Result(pb.start(), null)
        }
        if (!RuntimeExecPolicy.hasStaticExecBridge(context)) {
            return Result(null, RuntimeExecPolicy.hostPreviewBlockedMessage(runtimeLabel))
        }
        // Match ProcessBuilder.environment() semantics: the caller's vars are
        // an overlay on the parent environment, not a replacement for it.
        val fullEnv = System.getenv().toMutableMap()
        fullEnv.putAll(env)
        var spawnError: String? = null
        val proc = startStaticExec(command, fullEnv, cwd) { msg -> spawnError = msg }
        return Result(proc, spawnError)
    }

    /**
     * Host preview only. [com.webtoapp.core.linux.StaticExecProcess] is excluded
     * from the shell sync, and generated APKs do not carry libstatic_exec.so.
     */
    private fun startStaticExec(
        command: List<String>,
        env: Map<String, String>,
        cwd: File?,
        onError: (String) -> Unit
    ): Process? {
        return try {
            val clazz = Class.forName("com.webtoapp.core.linux.StaticExecProcess")
            val method = clazz.getDeclaredMethod(
                "start",
                List::class.java,
                Map::class.java,
                File::class.java,
                Function1::class.java
            )
            method.invoke(null, command, env, cwd, onError) as? Process
        } catch (t: Throwable) {
            onError("libstatic_exec.so 不可用: ${t.message}")
            null
        }
    }
}
