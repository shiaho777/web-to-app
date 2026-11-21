package com.webtoapp.core.python

import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.util.UUID

internal object PythonDependencyCache {
    private const val MARKER = ".w2a_deps.sha256"
    private val include = Regex("""^\s*(?:--(?:requirement|constraint)(?:\s+|=)|-[rc]\s*)(\S.*?)\s*(?:\s+#.*)?$""")

    fun fingerprint(requirements: File, pythonVersion: String, abi: String): String? {
        val digest = MessageDigest.getInstance("SHA-256")
        val visited = mutableSetOf<File>()
        fun hash(file: File) {
            if (!visited.add(file.canonicalFile)) return
            val contents = file.readBytes()
            digest.update(contents.size.toString().toByteArray())
            digest.update(0)
            digest.update(contents)
            contents.toString(Charsets.UTF_8).lineSequence().forEach { line ->
                val path = include.matchEntire(line)?.groupValues?.get(1)?.trim()?.trim('"', '\'')
                if (path != null && !path.contains("://")) {
                    hash(File(path).let { if (it.isAbsolute) it else File(file.parentFile, path) })
                }
            }
        }
        return try {
            digest.update("2\n$pythonVersion\n$abi\n".toByteArray())
            hash(requirements)
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: IOException) {
            null
        }
    }

    fun hasPackages(directory: File): Boolean = directory.isDirectory && directory.walkTopDown()
        .any { it.isFile && it != File(directory, MARKER) }

    fun matches(directory: File, fingerprint: String?): Boolean = fingerprint != null &&
        runCatching { File(directory, MARKER).readText().trim() == fingerprint }.getOrDefault(false)

    fun mark(directory: File, fingerprint: String) {
        File(directory, MARKER).writeText(fingerprint)
    }

    fun replace(directory: File, fingerprint: String, stagingParent: File, install: (File) -> Boolean): Boolean {
        val work = File(stagingParent, "python-deps-${UUID.randomUUID()}")
        val staging = File(work, "packages")
        val previous = File(work, "previous")
        if (!staging.mkdirs()) return false
        try {
            if (!install(staging) || !hasPackages(staging)) return false
            mark(staging, fingerprint)
            if (directory.exists() && !directory.renameTo(previous)) return false
            if (staging.renameTo(directory)) return true
            if (previous.exists() && !previous.renameTo(directory)) {
                throw IOException("Could not restore Python dependencies from $previous")
            }
            return false
        } finally {
            if (!previous.exists() || directory.exists()) work.deleteRecursively()
        }
    }
}
