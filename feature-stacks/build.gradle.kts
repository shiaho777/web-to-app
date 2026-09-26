import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

/**
 * Feature-stack implementations compiled into standalone DEX archives.
 *
 * Each stack ships inside the shell template as `assets/feature_stacks/<id>.dex`
 * and is loaded through DexClassLoader at runtime — ApkBuilder strips the asset
 * when the corresponding 功能栈 option is off, so disabling a stack physically
 * shrinks the generated APK (the code is not merely config-gated).
 *
 * Impls only see platform classes, the shared `core.featurestack.api` contract
 * (lives in this module, synced into the shell via syncShellRuntimeSources and
 * kept unrenamed by ProGuard) and their own stack dependencies. They must never
 * reference other com.webtoapp classes: those are renamed in the shell.
 */
android {
    namespace = "com.webtoapp.featurestack"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// Per-stack dependency sets. Resolving these configurations yields the jars that
// get shrunk + dexed into the feature dex; everything else (android framework,
// kotlin-stdlib, coroutines, the api contract) is supplied by the main dex.
val googleSigninStackDeps by configurations.creating {
    isCanBeConsumed = false
}
val fcmStackDeps by configurations.creating {
    isCanBeConsumed = false
}
val cronetStackDeps by configurations.creating {
    isCanBeConsumed = false
}
// Standalone R8 for shrinking + dexing feature stacks (AGP 9.2.x-era tool).
val r8Tool by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    // Host keeps the impls on its main classpath for preview.
    "googleSigninStackDeps"("androidx.credentials:credentials:1.5.0")
    "googleSigninStackDeps"("androidx.credentials:credentials-play-services-auth:1.5.0")
    "googleSigninStackDeps"("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    "fcmStackDeps"(platform("com.google.firebase:firebase-bom:33.7.0"))
    "fcmStackDeps"("com.google.firebase:firebase-messaging")
    "cronetStackDeps"("org.chromium.net:cronet-embedded:143.7445.0")

    "r8Tool"("com.android.tools:r8:9.2.25")

    // Compile-time visibility for the impl sources (also satisfied by the
    // per-stack configs at dex time).
    compileOnly("androidx.credentials:credentials:1.5.0")
    compileOnly("androidx.credentials:credentials-play-services-auth:1.5.0")
    compileOnly("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    compileOnly(platform("com.google.firebase:firebase-bom:33.7.0"))
    compileOnly("com.google.firebase:firebase-messaging")
    compileOnly("org.chromium.net:cronet-embedded:143.7445.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}

/**
 * Groups whose artifacts are physically embedded into each feature dex.
 * Everything else stays on the classpath (provided by the main dex).
 */
val stackInputGroups = mapOf(
    "google_signin" to listOf(
        "androidx.credentials",
        "com.google.android.libraries.identity",
        "com.google.android.gms",
        "com.google.android.fido"
    ),
    "fcm" to listOf(
        "com.google.firebase",
        "com.google.android.gms",
        "com.google.android.datatransport",
        "com.google.protobuf",
        "com.google.guava",
        "javax.inject",
        "androidx.concurrent"
    ),
    "cronet" to listOf(
        "org.chromium.net"
    )
)

val stackDeps = mapOf(
    "google_signin" to googleSigninStackDeps,
    "fcm" to fcmStackDeps,
    "cronet" to cronetStackDeps
)

val stackEntryClasses = mapOf(
    "google_signin" to "com.webtoapp.featurestack.signin.GoogleSignInStackImpl",
    "fcm" to "com.webtoapp.featurestack.fcm.FcmStackImpl",
    "cronet" to "com.webtoapp.featurestack.cronet.CronetStackImpl"
)

val stackImplPackages = mapOf(
    "google_signin" to "com/webtoapp/featurestack/signin/",
    "fcm" to "com/webtoapp/featurestack/fcm/",
    "cronet" to "com/webtoapp/featurestack/cronet/"
)

val featureDexDir = layout.buildDirectory.dir("generated/feature_dexes")
val featureDexInputs = layout.buildDirectory.dir("intermediates/feature_stack_inputs")

/**
 * Compiles each feature stack into `build/generated/feature_dexes/feature_stacks/<id>.dex`.
 * The output file is actually a ZIP (classes.dex + merged META-INF/services entries),
 * which DexClassLoader accepts — this keeps ServiceLoader-based providers (Cronet)
 * working inside the dex archive.
 *
 * R8 is resolved off the buildscript classpath (the same R8 AGP uses) so the dex
 * output format always matches the toolchain building the shell.
 */
tasks.register("generateFeatureStackDexes") {
    description = "Shrinks + dexes each feature stack into assets/feature_stacks/<id>.dex"
    group = "build"
    dependsOn("bundleReleaseAar")

    val outDir = featureDexDir.get().asFile
    val workDir = featureDexInputs.get().asFile
    val sdkDir = android.sdkDirectory
    val aarFile = layout.buildDirectory.file("outputs/aar/feature-stacks-release.aar")

    // Resolve at configuration time into plain data — the task action must not
    // capture Configuration objects (configuration-cache requirement).
    val resolvedStacks: Map<String, List<Pair<String, File>>> = stackDeps.mapValues { (_, cfg) ->
        cfg.resolvedConfiguration.resolvedArtifacts.map { artifact ->
            artifact.moduleVersion.id.group to artifact.file
        }
    }
    val r8Jars = r8Tool.files.toList()
    // Task-local copies — the action closure must not reference script-level vals.
    val implPackages = stackImplPackages
    val inputGroups = stackInputGroups
    val entryClasses = stackEntryClasses

    inputs.files(aarFile)
    inputs.files(r8Jars)
    inputs.files(resolvedStacks.values.flatten().map { it.second })
    outputs.dir(outDir)

    doLast {
        val aar = aarFile.get().asFile
        require(aar.isFile) { "feature-stacks AAR not found at ${aar.absolutePath}" }
        val androidJar = sdkDir.resolve("platforms/android-36/android.jar")
        require(androidJar.isFile) { "android.jar not found at ${androidJar.absolutePath}" }
        require(r8Jars.isNotEmpty()) { "R8 tool jar not resolved" }

        val moduleClasses = File(workDir, "module/classes.jar")
        moduleClasses.parentFile.mkdirs()
        ZipFile(aar).use { zip ->
            zip.getInputStream(zip.getEntry("classes.jar")).use { input ->
                moduleClasses.outputStream().use { input.copyTo(it) }
            }
        }

        outDir.deleteRecursively()
        val assetsDir = File(outDir, "feature_stacks").apply { mkdirs() }

        for ((stackId, depFiles) in resolvedStacks) {
            val pkgPrefix = implPackages.getValue(stackId)
            val groups = inputGroups.getValue(stackId)

            // Split this stack's deps into "embed into dex" vs "provided by host".
            val inputArtifacts = depFiles.filter { (group, _) ->
                groups.any { g -> group.startsWith(g) }
            }.map { it.second }
            val libArtifacts = depFiles.filter { (group, _) ->
                groups.none { g -> group.startsWith(g) }
            }.map { it.second }

            val stackWork = File(workDir, stackId).apply { deleteRecursively(); mkdirs() }
            val inputJars = mutableListOf<File>()
            val libJars = mutableListOf(androidJar)
            val serviceFiles = mutableMapOf<String, ByteArray>()
            // Consumer proguard rules shipped inside dependency AARs — a standalone
            // R8 run does not pick these up automatically the way AGP does.
            val consumerRules = mutableListOf<File>()
            var ruleIdx = 0

            fun addJarBytes(jar: File, intoInputs: Boolean) {
                if (!jar.isFile) return
                // resolvedArtifacts may list one artifact more than once — R8
                // rejects the same class origin seen twice.
                if (inputJars.any { it.absolutePath == jar.absolutePath } ||
                    libJars.any { it.absolutePath == jar.absolutePath }) return
                if (intoInputs) inputJars += jar else libJars += jar
            }

            fun classesFromArtifact(file: File): File? {
                if (!file.isFile) return null
                return if (file.name.endsWith(".aar")) {
                    val raw = File(stackWork, "extracted/${file.nameWithoutExtension}-raw.jar")
                    val dest = File(stackWork, "extracted/${file.nameWithoutExtension}-classes.jar")
                    raw.parentFile.mkdirs()
                    var ok = false
                    ZipFile(file).use { zip ->
                        zip.getEntry("classes.jar")?.let { entry ->
                            zip.getInputStream(entry).use { input ->
                                raw.outputStream().use { input.copyTo(it) }
                            }
                            ok = true
                        }
                        // Keep ServiceLoader registrations from this aar.
                        for (entry in zip.entries()) {
                            if (entry.name.startsWith("META-INF/services/") && !entry.isDirectory) {
                                serviceFiles[entry.name] = zip.getInputStream(entry).readBytes()
                            }
                        }
                        // Consumer keep rules ship as proguard.txt at aar root.
                        zip.getEntry("proguard.txt")?.let { entry ->
                            val rules = File(stackWork, "consumer-${ruleIdx++}-${file.nameWithoutExtension}.pro")
                            zip.getInputStream(entry).use { input ->
                                rules.outputStream().use { input.copyTo(it) }
                            }
                            consumerRules += rules
                        }
                    }
                    if (ok) {
                        // AAR classes.jars may carry duplicate entry names; rewrite
                        // deduped or R8 rejects the input ("defined multiple times").
                        ZipFile(raw).use { zip ->
                            val seen = HashSet<String>()
                            ZipOutputStream(dest.outputStream().buffered()).use { out ->
                                for (entry in zip.entries()) {
                                    if (entry.isDirectory || !seen.add(entry.name)) continue
                                    // ServiceLoader files can also live inside classes.jar.
                                    if (entry.name.startsWith("META-INF/services/")) {
                                        serviceFiles.putIfAbsent(entry.name, zip.getInputStream(entry).readBytes())
                                        continue
                                    }
                                    out.putNextEntry(ZipEntry(entry.name))
                                    zip.getInputStream(entry).use { it.copyTo(out) }
                                    out.closeEntry()
                                }
                            }
                        }
                        raw.delete()
                    }
                    dest.takeIf { ok }
                } else {
                    // Collect service registrations from plain jars too.
                    try {
                        ZipFile(file).use { zip ->
                            for (entry in zip.entries()) {
                                if (entry.name.startsWith("META-INF/services/") && !entry.isDirectory) {
                                    serviceFiles[entry.name] = zip.getInputStream(entry).readBytes()
                                }
                            }
                        }
                    } catch (_: Exception) {}
                    file
                }
            }

            // Stack impl classes from this module (api package excluded — supplied
            // by the main dex at runtime, re-embedding it would break type identity).
            val implJar = File(stackWork, "impl.jar")
            ZipFile(moduleClasses).use { zip ->
                ZipOutputStream(implJar.outputStream().buffered()).use { out ->
                    for (entry in zip.entries()) {
                        if (entry.isDirectory) continue
                        if (!entry.name.startsWith(pkgPrefix)) continue
                        out.putNextEntry(ZipEntry(entry.name))
                        zip.getInputStream(entry).use { it.copyTo(out) }
                        out.closeEntry()
                    }
                }
            }
            inputJars += implJar

            inputArtifacts.forEach { classesFromArtifact(it)?.let { addJarBytes(it, true) } }
            libArtifacts.forEach { classesFromArtifact(it)?.let { addJarBytes(it, false) } }

            // The api contract jar for --lib so impl references resolve.
            val apiJar = File(stackWork, "api.jar")
            ZipFile(moduleClasses).use { zip ->
                ZipOutputStream(apiJar.outputStream().buffered()).use { out ->
                    for (entry in zip.entries()) {
                        if (entry.isDirectory) continue
                        if (!entry.name.startsWith("com/webtoapp/core/featurestack/api/")) continue
                        out.putNextEntry(ZipEntry(entry.name))
                        zip.getInputStream(entry).use { it.copyTo(out) }
                        out.closeEntry()
                    }
                }
            }
            libJars += apiJar

            val pgConf = File(stackWork, "stack.pro").apply {
                writeText(
                    buildString {
                        append("-keep class ${entryClasses.getValue(stackId)} { *; }\n")
                        append("-keepclassmembers class * extends com.webtoapp.core.featurestack.api.FeatureStack { <init>(); }\n")
                        append("-keepattributes Exceptions,InnerClasses,Signature,EnclosingMethod,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations\n")
                        // Firebase discovers registrars via manifest metadata →
                        // Class.forName on stored names; parcelables + provider
                        // impls in credentials are also reflection-loaded.
                        if (stackId == "fcm") {
                            append("-keep class * implements com.google.firebase.components.ComponentRegistrar { *; }\n")
                            append("-keep class * implements android.os.Parcelable { *; }\n")
                        }
                        if (stackId == "google_signin") {
                            append("-keep class androidx.credentials.playservices.** { *; }\n")
                            append("-keep class * implements androidx.credentials.CredentialProvider { *; }\n")
                            append("-keep class * implements android.os.Parcelable { *; }\n")
                        }
                        if (stackId == "cronet") {
                            append("-keep class org.chromium.net.impl.CronetProvider { *; }\n")
                            append("-keep class org.chromium.net.** extends java.net.URLStreamHandlerFactory { *; }\n")
                        }
                        append("-dontwarn java.lang.invoke.**\n")
                        append("-dontwarn kotlin.**\n")
                        append("-dontwarn kotlinx.**\n")
                        append("-dontwarn org.jetbrains.**\n")
                        append("-dontwarn javax.inject.**\n")
                        append("-dontwarn com.google.errorprone.**\n")
                        append("-dontwarn com.google.j2objc.**\n")
                        append("-dontwarn org.checkerframework.**\n")
                        append("-dontwarn org.codehaus.mojo.animal_sniffer.*\n")
                        append("-dontwarn com.google.android.play.core.**\n")
                        append("-dontwarn android.**\n")
                        if (stackId == "fcm") {
                            append("-dontwarn com.google.firebase.**\n")
                            append("-dontwarn com.google.android.gms.**\n")
                            append("-dontwarn com.google.android.datatransport.**\n")
                            append("-dontwarn com.google.protobuf.**\n")
                        }
                        if (stackId == "google_signin") {
                            append("-dontwarn com.google.android.gms.**\n")
                            append("-dontwarn androidx.credentials.**\n")
                            append("-dontwarn com.google.android.libraries.identity.**\n")
                            append("-dontwarn androidx.datastore.**\n")
                        }
                        if (stackId == "cronet") {
                            append("-dontwarn org.chromium.**\n")
                        }
                    }
                )
            }

            val r8Out = File(stackWork, "r8_out").apply { mkdirs() }
            val javaExe = File(System.getProperty("java.home"), "bin/java")
            val cmd = buildList {
                add(javaExe.absolutePath)
                add("-cp"); add(r8Jars.joinToString(File.pathSeparator) { it.absolutePath })
                add("com.android.tools.r8.R8")
                add("--release")
                add("--min-api"); add("23")
                add("--pg-conf"); add(pgConf.absolutePath)
                consumerRules.forEach { add("--pg-conf"); add(it.absolutePath) }
                add("--output"); add(r8Out.absolutePath)
                libJars.forEach { add("--lib"); add(it.absolutePath) }
                inputJars.forEach { add(it.absolutePath) }
            }
            val proc = ProcessBuilder(cmd).redirectErrorStream(true).start()
            val r8Log = proc.inputStream.bufferedReader().readText()
            if (proc.waitFor() != 0) {
                throw GradleException("R8 failed for stack $stackId:\n$r8Log")
            }
            if (r8Log.isNotBlank()) {
                logger.lifecycle("R8 [$stackId]: $r8Log")
            }
            val classesDex = File(r8Out, "classes.dex")
            require(classesDex.isFile) { "R8 produced no classes.dex for stack $stackId" }

            // Final asset: zip(classes.dex + META-INF/services from input artifacts).
            val outDex = File(assetsDir, "$stackId.dex")
            ZipOutputStream(outDex.outputStream().buffered()).use { out ->
                out.putNextEntry(ZipEntry("classes.dex"))
                classesDex.inputStream().use { it.copyTo(out) }
                out.closeEntry()
                serviceFiles.forEach { (name, bytes) ->
                    out.putNextEntry(ZipEntry(name))
                    out.write(bytes)
                    out.closeEntry()
                }
            }
        }
    }
}
