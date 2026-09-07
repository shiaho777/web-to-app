package com.webtoapp.core.shell

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Manifest component parity gate (incident: BridgeAlarmReceiver).
 *
 * Generated APKs run the shell template's manifest. A component class that is part
 * of the shell-synced runtime source set but missing from
 * `shell/src/main/AndroidManifest.xml` is dead at runtime — e.g. an explicit
 * PendingIntent broadcast to an undeclared receiver is silently dropped by the
 * system, which is how scheduled Web notifications broke in every exported app
 * while the host preview kept working (the host manifest declared it).
 *
 * Three legs:
 * 1. Every component class (Activity/Service/BroadcastReceiver subclass) found in the
 *    synced sources + shell java-overrides must be declared in the shell manifest.
 * 2. Every `com.webtoapp` component declared in the shell manifest must exist in the
 *    synced/override sources (no dead declarations).
 * 3. Every host-manifest component whose class is part of the shell source set must
 *    also be declared in the shell manifest (catches indirect supertypes leg 1's
 *    framework-base list may not know).
 *
 * If a synced component is intentionally NOT declared in the shell manifest (e.g. a
 * receiver that is only ever registered dynamically at runtime), add it to
 * [SHELL_DECLARATION_ALLOW] with a reason.
 */
class ShellManifestComponentParityTest {

    private companion object {

        // Framework/library component base classes recognized directly. Supertypes
        // that are themselves synced classes are resolved transitively via superMap.
        val COMPONENT_BASES = setOf(
            "BroadcastReceiver",
            "Service",
            "IntentService",
            "LifecycleService",
            "FirebaseMessagingService",
            "Activity",
            "AppCompatActivity",
            "ComponentActivity",
            "FragmentActivity"
        )

        /** Fully qualified component name -> reason it needs no shell manifest entry. */
        val SHELL_DECLARATION_ALLOW = mapOf<String, String>()

        val COMPONENT_TAGS = listOf("activity", "service", "receiver")

        val PACKAGE_PREFIX = "com.webtoapp"
    }

    @Test
    fun `synced component classes are declared in the shell manifest`() {
        val sources = collectShellSources()
        assertWithMessage(
            "Harness sanity: synced source set must not be empty — the include/exclude " +
                "pattern parsing of shell/build.gradle.kts probably broke"
        ).that(sources.keys.size).isGreaterThan(50)
        assertWithMessage(
            "Harness sanity: ShellModeManager.kt must be part of the synced set"
        ).that(sources.keys.any { it.endsWith("core/shell/ShellModeManager.kt") }).isTrue()

        val superMap = buildSuperMap(sources)
        val declared = parseManifestComponents(shellManifestFile()).map { it.fqcn }.toSet()

        val missing = mutableListOf<String>()
        for ((relativePath, source) in sources) {
            for (name in componentClassesIn(source, superMap)) {
                val fqcn = fqcnFromPath(relativePath, name)
                if (fqcn !in declared && fqcn !in SHELL_DECLARATION_ALLOW) {
                    missing += "$fqcn ($relativePath)"
                }
            }
        }

        val staleAllow = SHELL_DECLARATION_ALLOW.keys - declared.let { allComponentFqcns(sources) }
        assertWithMessage(
            "Stale SHELL_DECLARATION_ALLOW entries (class gone or now declared): $staleAllow"
        ).that(staleAllow).isEmpty()

        assertWithMessage(
            buildString {
                appendLine("Shell-synced component classes missing from shell/src/main/AndroidManifest.xml:")
                appendLine("Generated APKs run the shell manifest — an undeclared component is dead at")
                appendLine("runtime (explicit broadcasts to undeclared receivers are silently dropped;")
                appendLine("this broke scheduled Web notifications via BridgeAlarmReceiver).")
                appendLine("Declare the component in the shell manifest (mirroring the host manifest),")
                appendLine("or add a SHELL_DECLARATION_ALLOW entry with a reason if it is genuinely")
                appendLine("registered dynamically only.")
                appendLine()
                missing.forEach { appendLine("  $it") }
            }
        ).that(missing).isEmpty()
    }

    @Test
    fun `shell manifest components exist in synced sources`() {
        val sources = collectShellSources()
        val declaredClasses = classNamesIn(sources)

        val dead = parseManifestComponents(shellManifestFile())
            .filter { it.fqcn.startsWith(PACKAGE_PREFIX) }
            .map { it.fqcn.substringAfterLast('.') }
            .distinct()
            .filter { it !in declaredClasses }

        assertWithMessage(
            "Shell manifest declares components whose classes are not part of the " +
                "shell-synced sources or java-overrides (dead declarations, or a class " +
                "was dropped from syncShellRuntimeSources): $dead"
        ).that(dead).isEmpty()
    }

    @Test
    fun `host manifest components from synced packages are declared in shell`() {
        val sources = collectShellSources()
        val shellClassFqcns = allClassFqcns(sources)
        val shellDeclared = parseManifestComponents(shellManifestFile()).map { it.fqcn }.toSet()

        val missing = parseManifestComponents(hostManifestFile())
            .map { it.fqcn }
            .filter { it.startsWith(PACKAGE_PREFIX) }
            // Only classes that ship inside the shell template are the shell's problem;
            // host-only components (editor activities, apkbuilder services…) are skipped.
            .filter { it in shellClassFqcns }
            .filter { it !in shellDeclared && it !in SHELL_DECLARATION_ALLOW }
            .distinct()

        assertWithMessage(
            "Host manifest declares components whose classes are shell-synced but which " +
                "the shell manifest does not declare (host preview works, exported APK " +
                "silently drops the component): $missing"
        ).that(missing).isEmpty()
    }

    // ------------------------------------------------------------------
    // Source-set assembly
    // ------------------------------------------------------------------

    private data class ManifestComponent(val tag: String, val fqcn: String)

    /** Relative path -> sanitized source text (app synced files + shell java-overrides). */
    private fun collectShellSources(): Map<String, String> {
        val appJava = resolveExistingDir("app/src/main/java", "src/main/java")
        val shellMain = resolveExistingDir("shell/src/main", "../shell/src/main")
        val shellBuild = resolveExistingFile("shell/build.gradle.kts", "../shell/build.gradle.kts")

        val (includes, excludes) = parseSyncPatterns(shellBuild)
        assertWithMessage("Parsed include patterns from shell/build.gradle.kts")
            .that(includes).isNotEmpty()

        val includeRegexes = includes.map(::globToRegex)
        val excludeRegexes = excludes.map(::globToRegex)

        val result = mutableMapOf<String, String>()
        appJava.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { file ->
                val relative = file.relativeTo(appJava).path.replace(File.separatorChar, '/')
                if (includeRegexes.any { it.matches(relative) } &&
                    excludeRegexes.none { it.matches(relative) }
                ) {
                    result["app/$relative"] = stripCodeNoise(file.readText())
                }
            }

        val overrides = File(shellMain, "java-overrides")
        if (overrides.isDirectory) {
            overrides.walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .forEach { file ->
                    val relative = file.relativeTo(shellMain).path.replace(File.separatorChar, '/')
                    result["overrides/$relative"] = stripCodeNoise(file.readText())
                }
        }
        return result
    }

    private fun parseSyncPatterns(shellBuild: File): Pair<List<String>, List<String>> {
        val text = shellBuild.readText()
        val taskStart = text.indexOf("val syncShellRuntimeSources")
        assertWithMessage("syncShellRuntimeSources task not found in shell/build.gradle.kts")
            .that(taskStart >= 0).isTrue()
        val taskEnd = text.indexOf("into(layout.buildDirectory", taskStart)
        val body = text.substring(taskStart, if (taskEnd > taskStart) taskEnd else text.length)
        return extractQuotedPatterns(body, "include") to extractQuotedPatterns(body, "exclude")
    }

    private fun extractQuotedPatterns(body: String, fn: String): List<String> {
        val block = Regex("""\b$fn\s*\(([\s\S]*?)\n\s*\)""").find(body)?.groupValues?.get(1)
            ?: return emptyList()
        return Regex("\"([^\"]+)\"").findAll(block).map { it.groupValues[1] }.toList()
    }

    private fun globToRegex(glob: String): Regex {
        val sb = StringBuilder()
        var i = 0
        while (i < glob.length) {
            when {
                glob.startsWith("**/", i) -> {
                    sb.append("(?:.*/)?")
                    i += 3
                }
                glob.startsWith("**", i) -> {
                    sb.append(".*")
                    i += 2
                }
                glob[i] == '*' -> {
                    sb.append("[^/]*")
                    i++
                }
                else -> {
                    sb.append(Regex.escape(glob[i].toString()))
                    i++
                }
            }
        }
        return Regex("^$sb$")
    }

    // ------------------------------------------------------------------
    // Manifest parsing
    // ------------------------------------------------------------------

    private fun hostManifestFile() =
        resolveExistingFile("app/src/main/AndroidManifest.xml", "src/main/AndroidManifest.xml")

    private fun shellManifestFile() =
        resolveExistingFile("shell/src/main/AndroidManifest.xml", "../shell/src/main/AndroidManifest.xml")

    private fun parseManifestComponents(manifest: File): List<ManifestComponent> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(manifest)
        return COMPONENT_TAGS.flatMap { tag ->
            val nodes = document.getElementsByTagName(tag)
            (0 until nodes.length).mapNotNull { index ->
                val name = nodes.item(index).attributes?.getNamedItem("android:name")?.nodeValue
                name?.let { ManifestComponent(tag, resolveFqcn(it)) }
            }
        }
    }

    private fun resolveFqcn(manifestName: String): String = when {
        manifestName.startsWith(".") -> PACKAGE_PREFIX + manifestName
        '.' in manifestName -> manifestName
        else -> "$PACKAGE_PREFIX.$manifestName"
    }

    // ------------------------------------------------------------------
    // Kotlin class scanning
    // ------------------------------------------------------------------

    private val CLASS_DECL = Regex("""\bclass\s+(\w+)""")

    private fun classNamesIn(sources: Map<String, String>): Set<String> =
        sources.values.flatMap { source ->
            CLASS_DECL.findAll(source).map { it.groupValues[1] }
        }.toSet()

    /** FQCNs of every class declared in the shell source set, derived from file paths. */
    private fun allClassFqcns(sources: Map<String, String>): Set<String> {
        val result = mutableSetOf<String>()
        for ((key, source) in sources) {
            CLASS_DECL.findAll(source).forEach { match ->
                result += fqcnFromPath(key, match.groupValues[1])
            }
        }
        return result
    }

    private fun allComponentFqcns(sources: Map<String, String>): Set<String> {
        val superMap = buildSuperMap(sources)
        val result = mutableSetOf<String>()
        for ((key, source) in sources) {
            componentClassesIn(source, superMap).forEach { name ->
                result += fqcnFromPath(key, name)
            }
        }
        return result
    }

    /**
     * Package prefix derived from the file path: `app/com/webtoapp/core/notification/X.kt`
     * declaring `class Y` -> `com.webtoapp.core.notification.Y`. Kotlin allows package/path
     * mismatch but this repo never uses it.
     */
    private fun fqcnFromPath(key: String, className: String): String {
        val relative = when {
            key.startsWith("app/") -> key.removePrefix("app/")
            key.startsWith("overrides/java-overrides/") -> key.removePrefix("overrides/java-overrides/")
            else -> key.substringAfter("java-overrides/")
        }
        val packagePath = relative.substringBeforeLast('/')
        return packagePath.replace('/', '.') + "." + className
    }

    /** className -> direct supertype simple names, across the whole shell source set. */
    private fun buildSuperMap(sources: Map<String, String>): Map<String, Set<String>> {
        val map = mutableMapOf<String, MutableSet<String>>()
        for (source in sources.values) {
            scanClassDeclarations(source) { name, _, supers ->
                map.getOrPut(name) { mutableSetOf() } += supers
            }
        }
        return map
    }

    /** Component classes declared in one sanitized source, resolved transitively. */
    private fun componentClassesIn(
        source: String,
        superMap: Map<String, Set<String>>
    ): Set<String> {
        val result = mutableSetOf<String>()
        scanClassDeclarations(source) { name, isAbstract, _ ->
            if (!isAbstract && reachesComponentBase(name, superMap, mutableSetOf())) {
                result += name
            }
        }
        return result
    }

    private fun reachesComponentBase(
        name: String,
        superMap: Map<String, Set<String>>,
        seen: MutableSet<String>
    ): Boolean {
        if (name in COMPONENT_BASES) return true
        if (!seen.add(name)) return false
        return (superMap[name] ?: emptySet()).any { reachesComponentBase(it, superMap, seen) }
    }

    /**
     * Invokes [onClass] for every `class X : Supers` declaration. Supertype parsing skips
     * the (possibly parenthesized) primary constructor so constructor parameter types like
     * `class ShellPermissionDelegate(private val activity: AppCompatActivity)` are never
     * mistaken for a supertype.
     */
    private fun scanClassDeclarations(source: String, onClass: (name: String, isAbstract: Boolean, supers: Set<String>) -> Unit) {
        for (match in CLASS_DECL.findAll(source)) {
            val name = match.groupValues[1]
            val prefix = source.substring(maxOf(0, match.range.first - 40), match.range.first)
            val isAbstract = Regex("""\babstract\s+$""").containsMatchIn(prefix)

            var rest = source.substring(match.range.last + 1).trimStart()
            if (rest.startsWith("(")) {
                val afterCtor = skipBalancedParens(rest) ?: continue
                rest = afterCtor.trimStart()
            }
            if (!rest.startsWith(":")) {
                onClass(name, isAbstract, emptySet())
                continue
            }
            val braceIdx = rest.indexOf('{')
            val segment = if (braceIdx > 0) rest.substring(1, braceIdx) else rest.substring(1).take(400)
            val supers = segment.split(',').mapNotNull { part ->
                val identifier = part.trim()
                    .substringBefore('(')
                    .substringBefore('<')
                    .substringBefore(" by ")
                    .trim()
                    .substringAfterLast('.')
                identifier.takeIf { Regex("^[A-Za-z_][A-Za-z0-9_]*$").matches(it) }
            }.toSet()
            onClass(name, isAbstract, supers)
        }
    }

    private fun skipBalancedParens(text: String): String? {
        var depth = 0
        for ((index, ch) in text.withIndex()) {
            if (ch == '(') depth++
            else if (ch == ')') {
                depth--
                if (depth == 0) return text.substring(index + 1)
            }
        }
        return null
    }

    /**
     * Linear char-scan sanitizer: replaces string/char literals and comments with
     * short placeholders so declaration scanning never sees code-like text inside
     * literals. Deliberately regex-free — java.util.regex recurses per repetition
     * on lazy/DOT_MATCHES_ALL quantifiers, and the synced set contains multi-MB
     * sources (core/i18n/Strings.kt), which blew the CI test worker stack
     * (StackOverflowError in StringUTF16 via Pattern) with the regex version.
     */
    private fun stripCodeNoise(source: String): String {
        val out = StringBuilder(source.length)
        val n = source.length
        var i = 0
        while (i < n) {
            val c = source[i]
            when {
                c == '"' && source.startsWith("\"\"\"", i) -> {
                    val end = source.indexOf("\"\"\"", i + 3)
                    i = if (end < 0) n else end + 3
                    out.append("\"\"")
                }
                c == '"' -> {
                    var j = i + 1
                    while (j < n) {
                        when (source[j]) {
                            '\\' -> j += 2
                            '"', '\n' -> { j++; break }
                            else -> j++
                        }
                    }
                    i = j.coerceAtMost(n)
                    out.append("\"\"")
                }
                c == '\'' -> {
                    var j = i + 1
                    while (j < n) {
                        when (source[j]) {
                            '\\' -> j += 2
                            '\'', '\n' -> { j++; break }
                            else -> j++
                        }
                    }
                    i = j.coerceAtMost(n)
                    out.append("''")
                }
                c == '/' && i + 1 < n && source[i + 1] == '*' -> {
                    val end = source.indexOf("*/", i + 2)
                    i = if (end < 0) n else end + 2
                    out.append(' ')
                }
                c == '/' && i + 1 < n && source[i + 1] == '/' -> {
                    val end = source.indexOf('\n', i)
                    i = if (end < 0) n else end
                }
                else -> {
                    out.append(c)
                    i++
                }
            }
        }
        return out.toString()
    }

    private fun resolveExistingDir(vararg candidates: String): File {
        return candidates.asSequence().map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate directory from: ${candidates.joinToString()}")
    }

    private fun resolveExistingFile(vararg candidates: String): File {
        return candidates.asSequence().map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate file from: ${candidates.joinToString()}")
    }
}
