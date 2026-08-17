package com.webtoapp.core.linux

import java.io.File

/**
 * Launch fork+exec runtimes with the right channel for the current build:
 * plain ProcessBuilder when execve works (generated APKs, targetSdk 28), or
 * the user-mode exec loader under host W^X (targetSdk>=29).
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
        val proc = StaticExecProcess.start(command, fullEnv, cwd) { msg -> spawnError = msg }
        return Result(proc, spawnError)
    }
}
