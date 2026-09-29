import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.inject.Inject

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.webtoapp.stack.admob"
    compileSdk = 36

    defaultConfig {
        // Matches the shell template floor so the injected DEX runs everywhere
        // generated APKs do.
        minSdk = 23
    }

    // The runtime contract is authored once under app/ (it ships in the shell
    // template) and compiled into the stack as well; the template's copy wins
    // at runtime via normal class-path shadowing.
    sourceSets {
        getByName("main") {
            java.srcDir("../../app/src/main/java/com/webtoapp/core/ads/api")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // ads-lite is a marker POM; the real client lives in play-services-ads-api.
    // Its version still tracks the marker, so we pin both via the marker dep.
    implementation("com.google.android.gms:play-services-ads-lite:24.8.0")
}

// ---------------------------------------------------------------------------
// Feature-stack bundle
//
// Produces `build/outputs/stack/admob.zip` consumed by `:app:syncStackBundles`:
//
//   dex/classesN.dex   d8 over adapter + embedded SDK jars (multidex-safe)
//   stack.arsc         resource table compiled at --package-id 0x80, holding
//                      every dep's res so R constants resolve after grafting
//   res/**             the stack's resource files (namespaced on graft)
//   manifest.xml       curated manifest fragment (application components,
//                      permissions, queries) injected by AxmlRebuilder
//   stack.json         {id, packageId, resPrefix, version}
// ---------------------------------------------------------------------------

val stackEmbedGroups = listOf("com.google.android.gms")

abstract class BundleFeatureStackTask : DefaultTask() {

    @get:Inject
    abstract val execOps: ExecOperations

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val stackAar: RegularFileProperty

    /** Dependency artifacts to embed (jars + aars, fully resolved). */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val depFiles: ConfigurableFileCollection

    @get:Input
    abstract val stackId: Property<String>

    @get:Input
    abstract val stackPackageId: Property<String>

    @get:Input
    abstract val resPrefix: Property<String>

    @get:Input
    abstract val minSdk: Property<Int>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifestFragment: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val aapt2Bin: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val d8Bin: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val androidJar: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val javacBin: RegularFileProperty

    @get:OutputFile
    abstract val bundleZip: RegularFileProperty

    @TaskAction
    fun bundle() {
        val work = temporaryDir
        work.deleteRecursively()
        work.mkdirs()

        // -- 1. unpack the stack's own AAR (adapter classes + optional res) ----
        val aarDir = work.resolve("aar").apply { mkdirs() }
        unzip(stackAar.get().asFile, aarDir)
        val adapterJar = aarDir.resolve("classes.jar")
        check(adapterJar.isFile) { "stack AAR has no classes.jar" }

        // -- 2. unpack dep AARs; collect class jars + res dirs ------------------
        val depClassJars = mutableListOf<File>()
        val resDirs = mutableListOf<File>()
        val depPackages = mutableListOf<String>()
        aarDir.resolve("res").takeIf { it.isDirectory }?.let { resDirs += it }
        aarDir.resolve("AndroidManifest.xml")
            .takeIf { it.isFile }
            ?.let { depPackages += readManifestPackage(it) }

        depFiles.files.sortedBy { it.name }.forEachIndexed { i, f ->
            when {
                f.name.endsWith(".aar") -> {
                    val dir = work.resolve("dep$i").apply { mkdirs() }
                    unzip(f, dir)
                    dir.resolve("classes.jar").takeIf { it.isFile }?.let { depClassJars += it }
                    dir.resolve("res").takeIf { it.isDirectory }?.let { resDirs += it }
                    dir.resolve("AndroidManifest.xml")
                        .takeIf { it.isFile }
                        ?.let { depPackages += readManifestPackage(it) }
                }
                f.name.endsWith(".jar") -> depClassJars += f
            }
        }

        // -- 3. compile all res dirs, link them as one package at 0x8N ---------
        val flatDir = work.resolve("flats").apply { mkdirs() }
        val flats = resDirs.mapIndexedNotNull { i, res ->
            val out = flatDir.resolve("res$i.zip")
            exec(aapt2Bin.get().asFile.absolutePath, "compile", "--dir", res.absolutePath, "-o", out.absolutePath)
            out.takeIf { it.isFile }
        }

        val stubManifest = work.resolve("StubManifest.xml").apply {
            writeText("<manifest package=\"com.webtoapp.stack.${stackId.get()}\"/>")
        }
        val resApk = work.resolve("stack-res.apk")
        val genSrc = work.resolve("genSrc").apply { mkdirs() }
        val linkArgs = mutableListOf(
            "link", "-o", resApk.absolutePath,
            "--manifest", stubManifest.absolutePath,
            "--package-id", stackPackageId.get(),
            "-I", androidJar.get().asFile.absolutePath,
            "--java", genSrc.absolutePath,
            "--min-sdk-version", minSdk.get().toString()
        )
        depPackages.distinct().filter { it.isNotBlank() }.forEach {
            linkArgs += listOf("--extra-packages", it)
        }
        linkArgs += flats.map { it.absolutePath }
        exec(aapt2Bin.get().asFile.absolutePath, *linkArgs.toTypedArray())

        // -- 4. javac the generated R.java files --------------------------------
        val rSources = genSrc.walkTopDown().filter { it.name == "R.java" }.toList()
        val genClasses = work.resolve("genClasses").apply { mkdirs() }
        if (rSources.isNotEmpty()) {
            exec(
                javacBin.get().asFile.absolutePath,
                "-cp", androidJar.get().asFile.absolutePath,
                "-d", genClasses.absolutePath,
                *rSources.map { it.absolutePath }.toTypedArray()
            )
        }

        // -- 5. d8: adapter + embedded deps + generated R ------------------------
        val dexDir = work.resolve("dex").apply { mkdirs() }
        val genRJar = work.resolve("gen-r.jar")
        zipDir(genClasses.toPath(), genRJar)
        val d8Inputs = mutableListOf(adapterJar) + depClassJars + listOf(genRJar)
        exec(
            d8Bin.get().asFile.absolutePath,
            "--min-api", minSdk.get().toString(),
            "--lib", androidJar.get().asFile.absolutePath,
            "--output", dexDir.absolutePath,
            *d8Inputs.map { it.absolutePath }.toTypedArray()
        )
        val dexes = dexDir.listFiles { f -> f.name.matches(Regex("classes\\d*\\.dex")) }
            ?.sortedBy { it.name } ?: emptyList()
        check(dexes.isNotEmpty()) { "d8 produced no dex output" }

        // -- 6. assemble the bundle --------------------------------------------
        val resApkDir = work.toPath().resolve("resApk")
        unzip(resApk.toPath(), resApkDir)

        val out = bundleZip.get().asFile.toPath()
        Files.createDirectories(out.parent)
        Files.deleteIfExists(out)
        FileSystems.newFileSystem(
            URI.create("jar:" + out.toUri().toString()),
            mapOf("create" to "true")
        ).use { zip ->
            fun put(name: String, bytes: ByteArray) {
                val p = zip.getPath("/$name")
                Files.createDirectories(p.parent)
                Files.write(p, bytes)
            }
            dexes.forEach { put("dex/${it.name}", it.readBytes()) }
            put("stack.arsc", Files.readAllBytes(resApkDir.resolve("resources.arsc")))
            val resRoot = resApkDir.resolve("res")
            if (Files.isDirectory(resRoot)) {
                Files.walk(resRoot).use { stream ->
                    stream.filter { Files.isRegularFile(it) }.forEach { f ->
                        put(
                            "res/" + resRoot.relativize(f).toString().replace('\\', '/'),
                            Files.readAllBytes(f)
                        )
                    }
                }
            }
            put("manifest.xml", manifestFragment.get().asFile.readBytes())
            put(
                "stack.json",
                ("""{"id":"${stackId.get()}","version":1,""" +
                    """"packageId":"${stackPackageId.get()}","resPrefix":"${resPrefix.get()}"}""")
                    .toByteArray()
            )
        }
        logger.lifecycle(
            "bundled stack ${stackId.get()}: ${dexes.size} dex, " +
                "${resDirs.size} res dirs -> ${out.fileName} (${Files.size(out)} bytes)"
        )
    }

    private fun exec(bin: String, vararg args: String) {
        val captured = ByteArrayOutputStream()
        val result = execOps.exec {
            executable = bin
            this.args = args.toList()
            isIgnoreExitValue = true
            standardOutput = captured
            errorOutput = captured
        }
        check(result.exitValue == 0) {
            "$bin ${args.joinToString(" ")} failed (${result.exitValue}): $captured"
        }
    }

    private fun unzip(zipFile: File, dest: File) = unzip(zipFile.toPath(), dest.toPath())

    private fun zipDir(src: Path, outZip: File) {
        Files.deleteIfExists(outZip.toPath())
        FileSystems.newFileSystem(
            URI.create("jar:" + outZip.toURI().toString()),
            mapOf("create" to "true")
        ).use { zip ->
            Files.walk(src).use { stream ->
                stream.filter { Files.isRegularFile(it) }.forEach { f ->
                    val p = zip.getPath("/" + src.relativize(f).toString().replace('\\', '/'))
                    Files.createDirectories(p.parent)
                    Files.copy(f, p)
                }
            }
        }
    }

    private fun unzip(zipPath: Path, dest: Path) {
        FileSystems.newFileSystem(zipPath, null as ClassLoader?).use { fs ->
            for (root in fs.rootDirectories) {
                Files.walk(root).use { stream ->
                    stream.forEach { src ->
                        val rel = src.toString().removePrefix("/")
                        if (rel.isEmpty()) return@forEach
                        val target = dest.resolve(rel)
                        if (Files.isDirectory(src)) {
                            Files.createDirectories(target)
                        } else {
                            Files.createDirectories(target.parent)
                            Files.copy(src, target, StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            }
        }
    }

    private fun readManifestPackage(manifest: File): String =
        Regex("""package\s*=\s*"([^"]+)"""").find(manifest.readText())?.groupValues?.get(1) ?: ""
}

val bundleAdmobStack = tasks.register<BundleFeatureStackTask>("bundleAdmobStack") {
    group = "build"
    description = "Packages the admob feature stack (dex + 0x80 res package + manifest fragment)."

    dependsOn("bundleReleaseAar")
    stackAar.set(layout.buildDirectory.file("outputs/aar/admob-release.aar"))

    // Embed every artifact of the release runtime classpath belonging to the
    // stack's dependency groups; kotlin-stdlib / androidx resolve from the
    // template at runtime (they are already in every generated APK). Resolved
    // eagerly: a lazy provider would capture `project` and break the
    // configuration cache.
    val embeddedArtifacts = configurations.getByName("releaseRuntimeClasspath")
        .incoming.artifactView { isLenient = true }
        .artifacts
        .mapNotNull { result ->
            val owner = result.id.componentIdentifier
            val group = (owner as? ModuleComponentIdentifier)?.group
                ?: return@mapNotNull null
            result.file.takeIf { group in stackEmbedGroups }
        }
    depFiles.from(embeddedArtifacts)

    stackId.set("admob")
    stackPackageId.set("0x80")
    resPrefix.set("res/wtastack_admob/")
    minSdk.set(23)
    manifestFragment.set(layout.projectDirectory.file("manifest-fragment.xml"))

    val sdkDir = android.sdkDirectory
    val buildTools = sdkDir.resolve("build-tools").listFiles()
        ?.filter { it.isDirectory }
        ?.maxByOrNull { dir -> dir.name.split('.').mapNotNull { it.toIntOrNull() }.fold(0L) { acc, v -> acc * 1000 + v } }
        ?: error("no build-tools under ${sdkDir.absolutePath}")
    aapt2Bin.set(buildTools.resolve("aapt2"))
    d8Bin.set(buildTools.resolve("d8"))
    javacBin.set(
        File(
            providers.gradleProperty("org.gradle.java.home").orNull
                ?: System.getProperty("java.home"),
            "bin/javac"
        )
    )
    androidJar.set(
        layout.file(
            providers.provider {
                val jars = sdkDir.resolve("platforms").listFiles()
                    ?.mapNotNull { dir -> dir.resolve("android.jar").takeIf { it.isFile } }
                    ?: emptyList()
                jars.maxByOrNull { it.parentFile.name.removePrefix("android-").toIntOrNull() ?: 0 }
                    ?: error("no android.jar under ${sdkDir.absolutePath}/platforms")
            }
        )
    )

    bundleZip.set(layout.buildDirectory.file("outputs/stack/admob.zip"))
}
