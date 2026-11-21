package com.webtoapp.core.nodejs

import com.google.gson.JsonObject
import com.webtoapp.util.GsonProvider
import java.io.File

object NodeBootstrap {

    const val BOOTSTRAP_FILE_NAME = ".webtoapp_bootstrap.cjs"

    fun isEsmEntry(entryPath: File): Boolean {
        return when (entryPath.extension.lowercase()) {
            "mjs" -> true
            "cjs" -> false
            else -> nearestPackageTypeIsModule(entryPath.parentFile)
        }
    }

    private fun nearestPackageTypeIsModule(startDir: File?): Boolean {
        var dir = startDir
        while (dir != null) {
            val pkg = File(dir, "package.json")
            if (pkg.isFile) {
                return try {
                    val json = GsonProvider.gson.fromJson(pkg.readText(), JsonObject::class.java)
                    json?.get("type")?.asString == "module"
                } catch (_: Exception) {
                    false
                }
            }
            dir = dir.parentFile
        }
        return false
    }

    fun jsStringLiteral(value: String): String {
        val sb = StringBuilder("\"")
        for (ch in value) {
            when {
                ch == '\\' -> sb.append("\\\\")
                ch == '"' -> sb.append("\\\"")
                ch == '\n' -> sb.append("\\n")
                ch == '\r' -> sb.append("\\r")
                ch == '\t' -> sb.append("\\t")
                ch.code < 0x20 -> sb.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                else -> sb.append(ch)
            }
        }
        return sb.append('"').toString()
    }

    fun generate(projectDir: String, entryFilePath: String, esm: Boolean): String {
        val dir = jsStringLiteral(projectDir)
        val entry = jsStringLiteral(entryFilePath)
        val load = if (esm) {
            """
            import(require('url').pathToFileURL($entry).href).catch(function (e) {
                console.error('[wta-bootstrap] entry failed:', (e && (e.stack || e.message)) || e);
            });
            """.trimIndent()
        } else {
            """
            try {
                require($entry);
            } catch (e) {
                console.error('[wta-bootstrap] entry failed:', (e && (e.stack || e.message)) || e);
            }
            """.trimIndent()
        }
        return """
            'use strict';
            try {
                process.chdir($dir);
            } catch (e) {
                console.error('[wta-bootstrap] chdir failed:', (e && e.message) || e);
            }
            process.on('uncaughtException', function (err) {
                console.error('[wta-bootstrap] uncaughtException:', (err && (err.stack || err.message)) || err);
            });
            process.on('unhandledRejection', function (reason) {
                console.error('[wta-bootstrap] unhandledRejection:', (reason && (reason.stack || reason.message)) || reason);
            });
            process.exit = function (code) {
                console.error('[wta-bootstrap] process.exit(' + code + ') intercepted; keeping :nodejs process alive.');
            };
            globalThis.__wtaKeepAlive = setInterval(function () {}, 60000);
            $load
        """.trimIndent()
    }
}