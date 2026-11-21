package com.webtoapp.core.python

import java.io.File

object PythonLaunchTarget {

    data class AsgiTarget(val module: String, val objectName: String, val factory: Boolean) {
        val spec: String get() = "$module:$objectName"
        val configValue: String get() = if (factory) "$spec()" else spec
    }

    private val ASSIGNMENT = Regex(
        """^([A-Za-z_]\w*)\s*(?::[^=]+)?=\s*(?:[A-Za-z_]\w*\.)*(?:FastAPI|Starlette|Quart|Litestar)\s*\("""
    )

    private val FACTORY = Regex(
        """^def\s+([A-Za-z_]\w*)\s*\([^)]*\)\s*(?:->\s*[^:]+)?:"""
    )

    private val CONFIGURED_TARGET = Regex("""([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*):([A-Za-z_]\w*(?:\.[A-Za-z_]\w*)*)(\(\))?""")
    private val ASGI_IMPORT = Regex("""^\s*(?:from|import)\s+(?:fastapi|starlette|quart|litestar)\b""", RegexOption.MULTILINE)
    private val ASGI_DEPENDENCY = Regex("""(?<![\w-])(?:fastapi|starlette|quart|litestar)(?![\w-])""")

    private val FACTORY_CALL_NAMES = listOf("create_app", "make_app", "app_factory", "create_application")

    fun hasAsgiImport(source: String): Boolean = ASGI_IMPORT.containsMatchIn(source)

    fun hasAsgiDependency(source: String): Boolean = ASGI_DEPENDENCY.containsMatchIn(source)

    fun moduleNameFor(entryFile: String): String {
        return entryFile
            .removeSuffix(".py")
            .replace('\\', '/')
            .trim('/')
            .split('/')
            .filter { it.isNotBlank() && it != "." }
            .joinToString(".")
    }

    fun detectAsgiTarget(projectDir: File, entryFile: String, configuredModule: String = ""): AsgiTarget {
        val module = moduleNameFor(entryFile)
        val configured = CONFIGURED_TARGET.matchEntire(configuredModule.trim())?.let {
            AsgiTarget(it.groupValues[1], it.groupValues[2], it.groupValues[3].isNotEmpty())
        }
        if (configured != null && configured != AsgiTarget(module, "app", false)) return configured

        val source = runCatching { File(projectDir, entryFile).readText() }.getOrNull()
            ?: return AsgiTarget(module, "app", factory = false)

        val lines = source.lines()
        for (line in lines) {
            val match = ASSIGNMENT.find(line) ?: continue
            return AsgiTarget(module, match.groupValues[1], factory = false)
        }

        for (name in FACTORY_CALL_NAMES) {
            val defined = lines.any { line ->
                val m = FACTORY.find(line) ?: return@any false
                m.groupValues[1] == name
            }
            if (defined) return AsgiTarget(module, name, factory = true)
        }

        return AsgiTarget(module, "app", factory = false)
    }

    fun djangoSettingsFromManagePy(projectDir: File): String? {
        val source = runCatching { File(projectDir, "manage.py").readText() }.getOrNull() ?: return null
        val pattern = Regex("""['"]DJANGO_SETTINGS_MODULE['"]\s*(?:\]\s*=|,)\s*['"]([^'"]+)['"]""")
        return pattern.find(source)?.groupValues?.get(1)
    }

    fun detectDjangoSettings(projectDir: File): String? {
        djangoSettingsFromManagePy(projectDir)?.let { return it }
        return projectDir.listFiles()?.filter { it.isDirectory && File(it, "settings.py").isFile }
            ?.singleOrNull()?.let { "${it.name}.settings" }
    }
}
