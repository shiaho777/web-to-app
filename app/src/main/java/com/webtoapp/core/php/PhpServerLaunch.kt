package com.webtoapp.core.php

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.io.File

object PhpServerLaunch {

    private const val TAG = "PhpServerLaunch"

    const val ROUTER_ASSET = "php_router_server.php"

    const val LISTEN_HOST = "127.0.0.1"

    const val ENTRY_FILE_ENV = "WEBTOAPP_PHP_ENTRY_FILE"

    const val WORKERS_ENV = "PHP_CLI_SERVER_WORKERS"

    const val DEFAULT_WORKERS = 6

    const val DEFAULT_ENTRY_FILE = "index.php"

    fun serverArgs(port: Int, documentRoot: String, routerScript: String): List<String> =
        listOf("-S", "$LISTEN_HOST:$port", "-t", documentRoot, routerScript)

    fun serverEnv(entryFile: String): Map<String, String> = mapOf(
        WORKERS_ENV to DEFAULT_WORKERS.toString(),
        ENTRY_FILE_ENV to entryFile.ifBlank { DEFAULT_ENTRY_FILE }
    )

    fun extractRouterScript(context: Context): String {
        val scriptFile = File(context.cacheDir, ROUTER_ASSET)
        val staging = File(context.cacheDir, "$ROUTER_ASSET.${System.nanoTime()}.tmp")
        try {
            context.assets.open(ROUTER_ASSET).use { input ->
                staging.outputStream().use { output -> input.copyTo(output) }
            }
            if (!staging.renameTo(scriptFile)) {
                staging.copyTo(scriptFile, overwrite = true)
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to extract $ROUTER_ASSET", e)
        } finally {
            staging.delete()
        }
        return scriptFile.absolutePath
    }
}
