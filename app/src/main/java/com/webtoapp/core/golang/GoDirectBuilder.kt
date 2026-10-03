package com.webtoapp.core.golang

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.io.File
import java.security.MessageDigest

/**
 * On-device Go build driver for host builds that cannot fork+exec the Go
 * toolchain (targetSdk>=29 SELinux W^X).
 *
 * Instead of running the multi-process `go build` (go -> compile/link/asm via
 * execve, all denied), it replays the exact tool invocations `go build`
 * would perform — captured once via `go build -n` and verified end-to-end:
 *
 *   1. `go list -e -deps -json ./...` (single-shot, pure metadata) for the
 *      package DAG, file lists (already GOOS/GOARCH-filtered), vendoring maps
 *      and DefaultGODEBUG.
 *   2. Per package in topological order: `compile` (+ `asm` symabis/objects
 *      for packages with .s files, folded into the archive with [GoArchive]),
 *      each launched via [com.webtoapp.core.linux.HostProcessLauncher] which
 *      uses the user-mode static exec loader on W^X hosts.
 *   3. `link -buildmode=pie` for the main package with a synthesized
 *      importcfg + modinfo.
 *
 * Every launch is a single process that never spawns children, so nothing
 * ever touches the blocked execve path. Package archives are cached
 * persistently (keyed by content fingerprints, transitively), so the stdlib
 * compiles once and incremental rebuilds only touch what changed.
 */
object GoDirectBuilder {

    private const val TAG = "GoDirectBuilder"

    /**
     * Mirrors cmd/go/internal/work/gc.go: these std packages carry forward
     * declarations supplied behind-the-scenes by package runtime, so the
     * compiler must not assume completeness. (Any package with .s files is
     * additionally never complete.)
     */
    internal val NO_COMPLETE_STD = setOf(
        "bytes", "internal/poll", "net", "os",
        "runtime/metrics", "runtime/pprof", "runtime/trace",
        "sync", "syscall", "time"
    )

    data class GoListPackage(
        val importPath: String,
        val name: String,
        val dir: String,
        val goFiles: List<String>,
        val sFiles: List<String>,
        val imports: List<String>,
        val importMap: Map<String, String>,
        val deps: List<String>,
        val standard: Boolean,
        val depOnly: Boolean,
        val cgoFiles: List<String> = emptyList(),
        val cFiles: List<String> = emptyList(),
        val cxxFiles: List<String> = emptyList(),
        val mFiles: List<String> = emptyList(),
        val fFiles: List<String> = emptyList(),
        val swigFiles: List<String> = emptyList(),
        val sysoFiles: List<String> = emptyList(),
        val embedFiles: List<String> = emptyList(),
        val modulePath: String? = null,
        val moduleVersion: String? = null,
        val defaultGoDebug: String? = null,
    )

    data class PackageGraph(
        val packages: List<GoListPackage>,
        val main: GoListPackage,
        val goDebug: String,
    )

    suspend fun build(
        context: Context,
        projectDir: File,
        binaryName: String,
        env: Map<String, String> = emptyMap(),
        onOutput: (String) -> Unit = {},
    ): File? {
        if (!GoToolchainManager.isGoReady(context)) {
            onOutput("[go] toolchain not ready")
            return null
        }
        val goRoot = GoToolchainManager.getGoRoot(context)
        val toolDir = findToolDir(goRoot)
            ?: run {
                onOutput("[go] toolchain tool dir missing under ${goRoot.absolutePath}")
                return null
            }
        val goVersion = queryGoVersion(context, projectDir, env)
            ?: run {
                onOutput("[go] cannot determine toolchain version")
                return null
            }
        val lang = "go" + goVersion.removePrefix("go").split(".").take(2).joinToString(".")
        val goos = env["GOOS"] ?: "android"
        val goarch = resolveGoArch()
        onOutput("[go] direct driver: $goVersion $goos/$goarch (${toolDir.name})")

        val graph = queryGraph(context, projectDir, env, onOutput) ?: return null
        val refusal = refuseUnsupported(graph.packages)
        if (refusal != null) {
            onOutput("[go] $refusal")
            AppLogger.e(TAG, "direct build refused: $refusal")
            return null
        }

        val workRoot = File(projectDir, ".wta-gobuild").also { it.mkdirs() }
        val cacheRoot = File(
            GoToolchainManager.getToolchainRoot(context),
            "direct-cache/v1/${sanitize("${goVersion}_${goos}_${goarch}")}"
        ).also { it.mkdirs() }
        pruneCache(cacheRoot, onOutput)

        val order = topoSort(graph.packages)
        onOutput("[go] ${order.size} packages, compiling")
        val archiveFor = mutableMapOf<String, File>()
        val fingerprintFor = mutableMapOf<String, String>()
        var done = 0
        for (pkg in order) {
            if (pkg.importPath == "unsafe") continue // compiler-intrinsic, never archived
            val fp = fingerprint(pkg, goVersion, lang, goos, goarch, fingerprintFor)
            fingerprintFor[pkg.importPath] = fp
            val pkgWork = File(workRoot, sanitize(pkg.importPath)).also { it.mkdirs() }
            val cached = File(cacheRoot, "${sanitize(pkg.importPath)}/_pkg_.a")
            val cachedFp = File(cacheRoot, "${sanitize(pkg.importPath)}/fingerprint.txt")
            val outArchive = File(pkgWork, "_pkg_.a")
            if (cached.exists() && cachedFp.exists() && cachedFp.readText() == fp) {
                cached.copyTo(outArchive, overwrite = true)
            } else {
                if (!buildPackage(
                        context, pkg, pkgWork, outArchive, archiveFor,
                        toolDir, goRoot, goVersion, lang, goos, goarch, env, onOutput
                    )
                ) {
                    return null
                }
                val destDir = File(cacheRoot, sanitize(pkg.importPath)).also { it.mkdirs() }
                outArchive.copyTo(File(destDir, "_pkg_.a"), overwrite = true)
                File(destDir, "fingerprint.txt").writeText(fp)
            }
            archiveFor[pkg.importPath] = outArchive
            done++
            if (done % 25 == 0 || pkg == order.last()) {
                onOutput("[go] $done/${order.size} packages")
            }
        }

        val main = graph.main
        val mainArchive = archiveFor[main.importPath]
            ?: run {
                onOutput("[go] main archive missing: ${main.importPath}")
                return null
            }
        val binDir = File(projectDir, "bin").also { it.mkdirs() }
        val output = File(binDir, binaryName.ifBlank { projectDir.name })
        if (!linkMain(
                context, main, mainArchive, archiveFor, output,
                toolDir, goRoot, goos, goarch, graph.goDebug, env, onOutput
            )
        ) {
            return null
        }
        output.setExecutable(true, false)
        AppLogger.i(TAG, "Go direct build OK: ${output.absolutePath} (${output.length() / 1024} KB)")
        return output
    }

    // ---------- package graph ----------

    /**
     * Package graph via `go list -e -deps -f <template>`: the default JSON
     * output path computes staleness (which fork+execs the compile tool for
     * its build ID and dies with "permission denied" on W^X hosts), while a
     * custom -f template only resolves metadata and runs fine single-shot.
     * Fields are \u001F-separated (a real control char, never in paths).
     */
    internal suspend fun queryGraph(
        context: Context,
        projectDir: File,
        env: Map<String, String>,
        onOutput: (String) -> Unit,
    ): PackageGraph? {
        val template = listOf(
            "{{.ImportPath}}", "{{.Name}}", "{{.Dir}}",
            "{{join .GoFiles \",\"}}", "{{join .SFiles \",\"}}",
            "{{join .Imports \",\"}}",
            "{{range \$k,\$v := .ImportMap}}{{\$k}}={{\$v}};{{end}}",
            "{{join .Deps \",\"}}", "{{.Standard}}", "{{.DepOnly}}",
            "{{join .CgoFiles \",\"}}", "{{join .CFiles \",\"}}",
            "{{join .CXXFiles \",\"}}", "{{join .MFiles \",\"}}",
            "{{join .FFiles \",\"}}", "{{join .SwigFiles \",\"}}",
            "{{join .SysoFiles \",\"}}", "{{join .EmbedFiles \",\"}}",
            "{{with .Module}}{{.Path}}|{{.Version}}{{end}}",
            "{{.DefaultGODEBUG}}"
        ).joinToString("\u001F")
        val res = GoBuildEnvironment.executeGo(
            context = context,
            arguments = listOf("list", "-e", "-deps", "-f", template, "./..."),
            workingDir = projectDir,
            env = env,
            onOutput = {},
        )
        if (res.exitCode != 0 || res.stdout.isBlank()) {
            val tail = (res.stderr.lineSequence().filter { it.isNotBlank() }.lastOrNull()
                ?: res.stdout.lineSequence().filter { it.isNotBlank() }.lastOrNull()
                ?: "go list failed (exit=${res.exitCode})")
            onOutput("[go] package graph failed: $tail")
            AppLogger.e(TAG, "go list failed: exit=${res.exitCode}\n${res.stderr}")
            return null
        }
        val packages = res.stdout.lineSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { parseFlatLine(it) }
            .toList()
        if (packages.isEmpty()) {
            onOutput("[go] package graph empty")
            return null
        }
        val mains = packages.filter { !it.depOnly && it.name == "main" }
        if (mains.isEmpty()) {
            onOutput("[go] no main package (package main with func main) found")
            return null
        }
        if (mains.size > 1) {
            onOutput("[go] ${mains.size} main packages; building ${mains.first().importPath}")
        }
        val goDebug = packages.firstNotNullOfOrNull { it.defaultGoDebug }.orEmpty()
        return PackageGraph(packages, mains.first(), goDebug)
    }

    internal fun parseFlatLine(line: String): GoListPackage? {
        return try {
            val f = line.split('\u001F')
            fun get(i: Int): String = if (i < f.size) f[i] else ""
            fun csv(i: Int): List<String> {
                val s = get(i)
                return if (s.isBlank()) emptyList() else s.split(",")
            }
            val importMap = mutableMapOf<String, String>()
            if (get(6).isNotBlank()) {
                for (entry in get(6).split(";")) {
                    if (entry.isBlank()) continue
                    val eq = entry.indexOf('=')
                    if (eq > 0) importMap[entry.substring(0, eq)] = entry.substring(eq + 1)
                }
            }
            val mod = get(18).split("|", limit = 2)
            GoListPackage(
                importPath = get(0),
                name = get(1),
                dir = get(2),
                goFiles = csv(3),
                sFiles = csv(4),
                imports = csv(5),
                importMap = importMap,
                deps = csv(7),
                standard = get(8) == "true",
                depOnly = get(9) == "true",
                cgoFiles = csv(10),
                cFiles = csv(11),
                cxxFiles = csv(12),
                mFiles = csv(13),
                fFiles = csv(14),
                swigFiles = csv(15),
                sysoFiles = csv(16),
                embedFiles = csv(17),
                modulePath = mod.getOrNull(0)?.ifBlank { null },
                moduleVersion = mod.getOrNull(1)?.ifBlank { null },
                defaultGoDebug = get(19).ifBlank { null },
            ).takeIf { it.importPath.isNotBlank() }
        } catch (e: Exception) {
            AppLogger.w(TAG, "skip unparsable go list line: ${e.message}")
            null
        }
    }

    internal fun refuseUnsupported(packages: List<GoListPackage>): String? {
        for (p in packages) {
            if (p.importPath == "unsafe") continue
            if (p.cgoFiles.isNotEmpty() || p.cFiles.isNotEmpty() ||
                p.cxxFiles.isNotEmpty() || p.mFiles.isNotEmpty() ||
                p.fFiles.isNotEmpty() || p.swigFiles.isNotEmpty()
            ) {
                return "package ${p.importPath} needs cgo/C sources (CGO is not supported on-device)"
            }
            if (p.sysoFiles.isNotEmpty()) {
                return "package ${p.importPath} needs precompiled .syso objects (not supported on-device)"
            }
            if (p.embedFiles.isNotEmpty()) {
                return "package ${p.importPath} uses go:embed (not supported by the on-device driver yet)"
            }
            if (p.goFiles.isEmpty() && p.sFiles.isEmpty()) {
                return "package ${p.importPath} has no buildable Go/asm files"
            }
        }
        return null
    }

    internal fun topoSort(packages: List<GoListPackage>): List<GoListPackage> {
        val byPath = packages.associateBy { it.importPath }
        val order = mutableListOf<GoListPackage>()
        val seen = mutableSetOf<String>()
        val temp = mutableSetOf<String>()
        fun visit(ip: String) {
            if (ip in seen || ip == "unsafe") return
            if (ip in temp) return // cycle guard: emit once, let the compiler complain
            temp.add(ip)
            val p = byPath[ip]
            if (p != null) {
                for (dep in p.imports) visit(p.importMap[dep] ?: dep)
            }
            temp.remove(ip)
            seen.add(ip)
            if (p != null) order.add(p)
        }
        for (p in packages) visit(p.importPath)
        return order
    }

    // ---------- per-package build ----------

    internal fun renderImportCfg(
        pkg: GoListPackage,
        archiveFor: Map<String, File>,
    ): String {
        val sb = StringBuilder("# import config\n")
        for ((src, dst) in pkg.importMap) {
            sb.append("importmap ").append(src).append('=').append(dst).append('\n')
        }
        for (imp in pkg.imports) {
            val resolved = pkg.importMap[imp] ?: imp
            if (resolved == "unsafe") continue
            val archive = archiveFor[resolved] ?: continue
            sb.append("packagefile ").append(resolved).append('=')
                .append(archive.absolutePath).append('\n')
        }
        return sb.toString()
    }

    internal fun compileArgs(
        pkg: GoListPackage,
        outArchive: File,
        importCfg: File,
        workDir: File,
        goVersion: String,
        lang: String,
        pflag: String,
        complete: Boolean,
        symabis: File?,
        asmHdr: File?,
    ): List<String> {
        return buildList {
            add("-o"); add(outArchive.absolutePath)
            add("-trimpath"); add(workDir.absolutePath + "=>")
            add("-p"); add(pflag)
            add("-lang=$lang")
            if (pkg.standard) add("-std")
            if (complete) add("-complete")
            add("-goversion"); add(goVersion)
            add("-c=" + Runtime.getRuntime().availableProcessors().coerceAtLeast(1))
            add("-shared")
            add("-nolocalimports")
            add("-importcfg"); add(importCfg.absolutePath)
            add("-pack")
            if (symabis != null) {
                add("-symabis"); add(symabis.absolutePath)
            }
            if (asmHdr != null) {
                add("-asmhdr"); add(asmHdr.absolutePath)
            }
            pkg.goFiles.forEach { add(workDirOf(pkg, workDir) + it) }
        }
    }

    private fun workDirOf(pkg: GoListPackage, fallback: File): String {
        return if (pkg.dir.isNotBlank()) pkg.dir.trimEnd('/') + "/" else fallback.absolutePath + "/"
    }

    private suspend fun buildPackage(
        context: Context,
        pkg: GoListPackage,
        workDir: File,
        outArchive: File,
        archiveFor: Map<String, File>,
        toolDir: File,
        goRoot: File,
        goVersion: String,
        lang: String,
        goos: String,
        goarch: String,
        env: Map<String, String>,
        onOutput: (String) -> Unit,
    ): Boolean {
        val pflag = if (pkg.name == "main") "main" else pkg.importPath
        val complete = pkg.sFiles.isEmpty() &&
            !(pkg.standard && pkg.importPath in NO_COMPLETE_STD)
        val importCfg = File(workDir, "importcfg")
        importCfg.writeText(renderImportCfg(pkg, archiveFor))
        val compile = File(toolDir, "compile")
        val asm = File(toolDir, "asm")

        val hasAsm = pkg.sFiles.isNotEmpty()
        var symabis: File? = null
        if (hasAsm) {
            // The go command creates an EMPTY go_asm.h before gensymabis
            // (compile -asmhdr overwrites it later); without it the #include
            // in .s files hard-fails.
            File(workDir, "go_asm.h").writeText("")
            symabis = File(workDir, "symabis")
            val sArgs = buildList {
                add("-p"); add(pflag)
                add("-trimpath"); add(workDir.absolutePath + "=>")
                add("-I"); add(workDir.absolutePath + "/")
                add("-I"); add(File(goRoot, "pkg/include").absolutePath)
                add("-D"); add("GOOS_$goos")
                add("-D"); add("GOARCH_$goarch")
                add("-shared")
                add("-gensymabis")
                add("-o"); add(symabis!!.absolutePath)
                pkg.sFiles.forEach { add("./$it") }
            }
            val r = GoBuildEnvironment.executeTool(
                context, asm, sArgs, File(pkg.dir), env,
                onOutput = {},
            )
            if (r.exitCode != 0) {
                reportToolFailure(pkg, "asm symabis", r, onOutput)
                return false
            }
        }

        suspend fun runCompile(useComplete: Boolean): GoExecutionResult {
            val args = compileArgs(
                pkg, outArchive, importCfg, workDir, goVersion, lang, pflag,
                useComplete, symabis,
                if (hasAsm) File(workDir, "go_asm.h") else null
            )
            return GoBuildEnvironment.executeTool(
                context, compile, args, File(pkg.dir), env,
                onOutput = {},
            )
        }

        var r = runCompile(complete)
        if (r.exitCode != 0 && complete && r.looksLikeIncomplete()) {
            // Future toolchains may grow cmd/go's forward-declaration set.
            onOutput("[go] ${pkg.importPath}: retry without -complete")
            r = runCompile(false)
        }
        if (r.exitCode != 0 || !outArchive.exists()) {
            reportToolFailure(pkg, "compile", r, onOutput)
            return false
        }

        if (hasAsm) {
            val objects = mutableListOf<File>()
            for (base in pkg.sFiles) {
                val objName = (if (base.endsWith(".s")) base.dropLast(2) else base) + ".o"
                val obj = File(workDir, objName)
                val ar = GoBuildEnvironment.executeTool(
                    context,
                    asm,
                    listOf(
                        "-p", pflag,
                        "-trimpath", workDir.absolutePath + "=>",
                        "-I", workDir.absolutePath + "/",
                        "-I", File(goRoot, "pkg/include").absolutePath,
                        "-D", "GOOS_$goos",
                        "-D", "GOARCH_$goarch",
                        "-shared",
                        "-o", obj.absolutePath,
                        "./$base"
                    ),
                    File(pkg.dir), env,
                    onOutput = {},
                )
                if (ar.exitCode != 0 || !obj.exists()) {
                    reportToolFailure(pkg, "asm $base", ar, onOutput)
                    return false
                }
                objects.add(obj)
            }
            // Go 1.26 ships no `pack` tool; fold objects in directly.
            if (!GoArchive.appendObjects(outArchive, objects)) {
                onOutput("[go] ${pkg.importPath}: archive append failed")
                return false
            }
        }
        return true
    }

    private fun reportToolFailure(
        pkg: GoListPackage,
        tool: String,
        r: GoExecutionResult,
        onOutput: (String) -> Unit,
    ) {
        AppLogger.e(TAG, "$tool ${pkg.importPath} failed exit=${r.exitCode}\n${r.stderr}")
        onOutput("[go] ${pkg.importPath}: $tool failed (exit=${r.exitCode})")
        r.stderr.lineSequence()
            .filter { it.isNotBlank() }
            .take(8)
            .forEach { onOutput("[stderr] $it") }
    }

    // ---------- link ----------

    internal fun renderModinfo(
        mainImportPath: String,
        modulePath: String?,
        moduleVersion: String?,
        goDebug: String,
        goos: String,
        goarch: String,
    ): String {
        // NOTE: a SINGLE physical line; separators are literal backslash
        // escapes (\\t \\n as text), not real tabs/newlines — the linker
        // rejects anything else with "invalid modinfo: invalid syntax".
        val mod = modulePath ?: mainImportPath
        val ver = if (moduleVersion.isNullOrBlank()) "(devel)" else moduleVersion
        val sb = StringBuilder()
        sb.append("modinfo \"path\\t").append(mainImportPath)
        sb.append("\\nmod\\t").append(mod).append("\\t").append(ver).append("\\t")
        sb.append("\\nbuild\\t-buildmode=pie")
        sb.append("\\nbuild\\t-compiler=gc")
        if (goDebug.isNotBlank()) sb.append("\\nbuild\\tDefaultGODEBUG=").append(goDebug)
        sb.append("\\nbuild\\tCGO_ENABLED=0")
        sb.append("\\nbuild\\tGOARCH=").append(goarch)
        sb.append("\\nbuild\\tGOOS=").append(goos)
        if (goarch == "arm64") sb.append("\\nbuild\\tGOARM64=v8.0")
        if (goarch == "amd64") sb.append("\\nbuild\\tGOAMD64=v1")
        sb.append("\\n\"")
        return sb.toString()
    }

    private suspend fun linkMain(
        context: Context,
        main: GoListPackage,
        mainArchive: File,
        archiveFor: Map<String, File>,
        output: File,
        toolDir: File,
        goRoot: File,
        goos: String,
        goarch: String,
        goDebug: String,
        env: Map<String, String>,
        onOutput: (String) -> Unit,
    ): Boolean {
        onOutput("[go] linking ${main.importPath}")
        val cfg = StringBuilder("# import config\n")
        val seen = mutableSetOf<String>()
        fun emit(ip: String) {
            if (ip == "unsafe" || !seen.add(ip)) return
            val archive = archiveFor[ip] ?: return
            cfg.append("packagefile ").append(ip).append('=')
                .append(archive.absolutePath).append('\n')
        }
        for (dep in main.deps) emit(main.importMap[dep] ?: dep)
        emit(main.importPath)
        cfg.append(
            renderModinfo(
                main.importPath, main.modulePath, main.moduleVersion,
                goDebug.ifBlank { main.defaultGoDebug.orEmpty() }, goos, goarch
            )
        ).append('\n')
        val linkCfg = File(output.parentFile, "importcfg.link")
        linkCfg.writeText(cfg.toString())
        val args = buildList {
            add("-o"); add(output.absolutePath)
            add("-importcfg"); add(linkCfg.absolutePath)
            if (goDebug.isNotBlank()) add("-X=runtime.godebugDefault=$goDebug")
            else main.defaultGoDebug?.takeIf { it.isNotBlank() }?.let {
                add("-X=runtime.godebugDefault=$it")
            }
            add("-buildmode=pie")
            add(mainArchive.absolutePath)
        }
        val r = GoBuildEnvironment.executeTool(
            context, File(toolDir, "link"), args, File(main.dir), env, onOutput = {},
        )
        if (r.exitCode != 0 || !output.exists()) {
            AppLogger.e(TAG, "link failed exit=${r.exitCode}\n${r.stderr}")
            onOutput("[go] link failed (exit=${r.exitCode})")
            r.stderr.lineSequence()
                .filter { it.isNotBlank() }
                .take(8)
                .forEach { onOutput("[stderr] $it") }
            return false
        }
        return true
    }

    // ---------- helpers ----------

    internal fun sanitize(name: String): String {
        return name.replace(Regex("[^A-Za-z0-9_.-]"), "_").take(96)
    }

    internal fun findToolDir(goRoot: File): File? {
        val base = File(goRoot, "pkg/tool")
        // Prefer the canonical linux-arm64 toolchain layout, fall back to any
        // dir that actually contains the compile tool.
        val preferred = File(base, "linux_arm64")
        if (File(preferred, "compile").exists()) return preferred
        return base.listFiles()
            ?.filter { it.isDirectory && File(it, "compile").exists() }
            ?.firstOrNull()
    }

    internal suspend fun queryGoVersion(
        context: Context,
        projectDir: File,
        env: Map<String, String>,
    ): String? {
        val r = GoBuildEnvironment.executeGo(
            context, listOf("version"), projectDir, env, onOutput = {},
        )
        if (r.exitCode != 0) return null
        // "go version go1.26.4 linux/arm64"
        return Regex("""go\d+\.\d+(\.\d+)?""").find(r.stdout)?.value
    }

    internal fun resolveGoArch(): String {
        return when (GoDependencyManager.getDeviceAbi()) {
            "arm64-v8a" -> "arm64"
            "armeabi-v7a", "armeabi" -> "arm"
            "x86_64" -> "amd64"
            "x86" -> "386"
            else -> "arm64"
        }
    }

    internal fun fingerprint(
        pkg: GoListPackage,
        goVersion: String,
        lang: String,
        goos: String,
        goarch: String,
        fingerprintFor: Map<String, String>,
    ): String {
        val md = MessageDigest.getInstance("SHA-256")
        fun feed(s: String) = md.update(s.toByteArray())
        feed(goVersion); feed("\u0000"); feed(lang); feed("\u0000")
        feed(goos); feed("\u0000"); feed(goarch); feed("\u0000")
        feed(if (pkg.name == "main") "main" else pkg.importPath); feed("\u0000")
        feed(if (pkg.standard) "std" else "user"); feed("\u0000")
        val complete = pkg.sFiles.isEmpty() &&
            !(pkg.standard && pkg.importPath in NO_COMPLETE_STD)
        feed(if (complete) "complete" else "open"); feed("\u0000")
        for (f in pkg.goFiles + pkg.sFiles) {
            feed(f); feed("\u0000")
            hashFile(File(if (pkg.dir.isNotBlank()) pkg.dir else ".", f), md)
            feed("\u0000")
        }
        // Transitive: dep fingerprints pin the importcfg content.
        for (imp in pkg.imports) {
            val resolved = pkg.importMap[imp] ?: imp
            if (resolved == "unsafe") continue
            feed(resolved); feed("\u0000")
            feed(fingerprintFor[resolved].orEmpty()); feed("\u0000")
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun hashFile(f: File, md: MessageDigest) {
        try {
            f.inputStream().use { input ->
                val buf = ByteArray(65536)
                while (true) {
                    val r = input.read(buf)
                    if (r <= 0) break
                    md.update(buf, 0, r)
                }
            }
        } catch (_: Exception) {
            md.update(0)
        }
    }

    private fun pruneCache(cacheRoot: File, onOutput: (String) -> Unit) {
        try {
            var total = 0L
            cacheRoot.walkTopDown().filter { it.isFile }.forEach { total += it.length() }
            if (total > 800L * 1024 * 1024) {
                AppLogger.i(TAG, "pruning direct cache ≈${total / 1024 / 1024}MB")
                cacheRoot.deleteRecursively()
                cacheRoot.mkdirs()
                onOutput("[go] build cache pruned")
            }
        } catch (e: Exception) {
            AppLogger.w(TAG, "cache prune failed: ${e.message}")
        }
    }

    private fun GoExecutionResult.looksLikeIncomplete(): Boolean {
        return stderr.contains("missing function body")
    }
}
