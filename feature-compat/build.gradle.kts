@file:Suppress("DEPRECATION")

import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.webtoapp.feature.compat"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    sourceSets {
        getByName("main") {
            java.srcDir("src/main/java")
            java.srcDir("build/syncedHeavy/java")
        }
    }
}

val syncFeatureCompatSources by tasks.registering(Sync::class) {
    group = "build"
    description = "Sync heavy runtime sources excluded from LITE shell into feature-compat"
    from("../app/src/main/java") {
        include(
            "**/core/engine/GeckoViewEngine.kt",
            "**/core/engine/download/GeckoEngineDownloader.kt",
            "**/core/gecko/**",
            "**/core/notification/NotificationFcmManager.kt",
            "**/core/notification/NotificationFcmService.kt",
            "**/core/webview/TlsMitmBridge.kt",
            "**/core/webview/TlsMitmCaManager.kt",
            "**/core/webview/TlsUpstreamConnector.kt",

            "**/core/extension/BuiltInModules.kt",
            "**/core/extension/BuiltInChromeExtensions.kt",
            "**/core/extension/BrowserExtensionStore.kt",
            "**/core/extension/ExtensionPanelScript.kt",
            "**/core/extension/ChromeExtensionPolyfill.kt",
            "**/core/extension/ChromeExtensionMobileCompat.kt",
            "**/core/extension/UserScriptWindowScript.kt",
            "**/core/appearance/BrowserDisguiseJsGenerator.kt",
            "**/ui/shell/TranslateScriptProvider.kt",

            "**/core/forcedrun/ForcedRunHardwareController.kt",
            "**/core/forcedrun/NativeHardwareController.kt",

            "**/core/privacy/IsolationManager.kt",
            "**/core/privacy/FingerprintGenerator.kt",
            "**/core/privacy/IsolationScriptInjector.kt",
            "**/core/errorpage/ErrorPageGames.kt"
        )
    }
    into(layout.buildDirectory.dir("syncedHeavy/java"))
}

tasks.named("preBuild").configure { dependsOn(syncFeatureCompatSources) }

val shellReleaseClasses = project(":shell").layout.buildDirectory.dir("tmp/kotlin-classes/release")

val shellReleaseClassesJar by tasks.registering(Jar::class) {
    group = "build"
    description = "Jar LITE shell classes for feature-compat compileOnly classpath"
    dependsOn(":shell:compileReleaseKotlin")
    from(shellReleaseClasses)
    archiveFileName.set("shell-release-classes.jar")
    destinationDirectory.set(layout.buildDirectory.dir("shell-compile-only"))
}

val packDeps by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    implementation(project(":feature-api"))
    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:137.0.20250414091429")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("org.bouncycastle:bcpkix-jdk15to18:1.78.1")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.78.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    compileOnly(files(shellReleaseClassesJar.map { it.archiveFile.get().asFile }))

    packDeps("org.mozilla.geckoview:geckoview-arm64-v8a:137.0.20250414091429")
    packDeps(platform("com.google.firebase:firebase-bom:33.7.0"))
    packDeps("com.google.firebase:firebase-messaging")
    packDeps("org.bouncycastle:bcpkix-jdk15to18:1.78.1")
    packDeps("org.bouncycastle:bcprov-jdk15to18:1.78.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn(syncFeatureCompatSources)
    dependsOn(shellReleaseClassesJar)
}

val packOutputDir = rootProject.layout.projectDirectory.dir("app/src/main/assets/features/feature-compat")

tasks.register("packageFeatureCompatPack") {
    group = "build"
    description = "Package feature-compat dex (with heavy deps) into host assets"
    dependsOn("assembleRelease")
    outputs.dir(packOutputDir)
    doLast {
        val outDir = packOutputDir.asFile
        outDir.mkdirs()
        val aar = layout.buildDirectory.file("outputs/aar/feature-compat-release.aar").get().asFile
        if (!aar.isFile) throw GradleException("feature-compat release AAR missing: $aar")
        val work = layout.buildDirectory.dir("feature-pack-work").get().asFile
        work.deleteRecursively()
        work.mkdirs()
        copy {
            from(zipTree(aar))
            into(work)
        }
        val classesJar = work.resolve("classes.jar")
        if (!classesJar.isFile) throw GradleException("classes.jar missing in feature-compat AAR")

        val depJarDir = work.resolve("dep-jars").apply { mkdirs() }
        val depJars = mutableListOf(classesJar)

        val includeGroupPrefixes = listOf(
            "org.mozilla.geckoview",
            "com.google.firebase",
            "com.google.android.gms",
            "com.google.android.datatransport",
            "org.bouncycastle"
        )
        fun acceptGroup(group: String): Boolean =
            includeGroupPrefixes.any { group == it || group.startsWith("$it.") }

        packDeps.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
            val id = artifact.moduleVersion.id
            if (!acceptGroup(id.group)) return@forEach
            val file = artifact.file
            if (!file.isFile) return@forEach
            when (file.extension.lowercase()) {
                "jar" -> {
                    if (file.name.contains("sources") || file.name.contains("javadoc")) return@forEach
                    val dest = depJarDir.resolve("${id.group}-${id.name}-${id.version}.jar")
                    if (!dest.exists()) file.copyTo(dest)
                    depJars += dest
                }
                "aar" -> {
                    val extractDir = work.resolve("aar-${id.name}-${id.version}")
                    if (!extractDir.exists()) {
                        extractDir.mkdirs()
                        copy {
                            from(zipTree(file))
                            into(extractDir)
                        }
                    }
                    val nested = extractDir.resolve("classes.jar")
                    if (nested.isFile) {
                        val dest = depJarDir.resolve("${id.group}-${id.name}-${id.version}-classes.jar")
                        if (!dest.exists()) nested.copyTo(dest)
                        depJars += dest
                    }
                    val libs = extractDir.resolve("libs")
                    if (libs.isDirectory) {
                        libs.listFiles()?.filter { it.extension == "jar" }?.forEach { libJar ->
                            val dest = depJarDir.resolve("${id.name}-${libJar.name}")
                            if (!dest.exists()) libJar.copyTo(dest)
                            depJars += dest
                        }
                    }
                }
            }
        }

        val uniqueJars = depJars.distinctBy { it.absolutePath }
        logger.lifecycle("feature-compat d8 inputs (${uniqueJars.size}):")
        uniqueJars.forEach { jar ->
            logger.lifecycle("  - ${jar.name} (${jar.length() / 1024} KB)")
        }

        val localProps = rootProject.file("local.properties")
        val props = Properties()
        localProps.inputStream().use { props.load(it) }
        val sdkDir = file(props.getProperty("sdk.dir") ?: error("sdk.dir missing"))
        val buildTools = sdkDir.resolve("build-tools").listFiles()
            ?.filter { it.isDirectory }
            ?.maxByOrNull { it.name }
            ?: error("build-tools missing")
        val d8 = buildTools.resolve("d8")
        val dexOut = work.resolve("dex")
        dexOut.mkdirs()

        val cmd = mutableListOf(
            d8.absolutePath,
            "--release",
            "--min-api", "23",
            "--output", dexOut.absolutePath
        )
        uniqueJars.forEach { cmd += it.absolutePath }

        val pb = ProcessBuilder(cmd)
        pb.redirectErrorStream(true)
        val proc = pb.start()
        val output = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        if (code != 0) throw GradleException("d8 failed ($code): $output")

        val dexFiles = dexOut.listFiles()?.filter { it.extension == "dex" }?.sortedBy { it.name }.orEmpty()
        if (dexFiles.isEmpty()) throw GradleException("d8 produced no dex")
        outDir.listFiles()?.forEach { it.deleteRecursively() }
        dexFiles.forEach { it.copyTo(outDir.resolve(it.name), overwrite = true) }
        val dexJson = dexFiles.joinToString(prefix = "[", postfix = "]") { "\"${it.name}\"" }
        outDir.resolve("feature.json").writeText(
            """
            {
              "id": "feature-compat",
              "version": 1,
              "minLiteApi": 1,
              "dependsOn": [],
              "entryClass": "com.webtoapp.feature.compat.CompatFeatureModule",
              "loadOrder": 0,
              "dex": $dexJson,
              "nativeLibs": []
            }
            """.trimIndent() + "\n"
        )
        val totalKb = dexFiles.sumOf { it.length() } / 1024
        logger.lifecycle(
            "Packaged feature-compat -> $outDir (${dexFiles.map { it.name }}, ${totalKb} KB)"
        )
    }
}

tasks.register("packageFineFeaturePacks") {
    group = "build"
    description = "Split fine-grained feature packs from feature-compat classes"
    dependsOn("packageFeatureCompatPack")
    doLast {
        val script = rootProject.file("scripts/package_fine_feature_packs.py")
        val pb = ProcessBuilder("python3", script.absolutePath)
        pb.directory(rootProject.projectDir)
        pb.redirectErrorStream(true)
        val proc = pb.start()
        val log = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        logger.lifecycle(log.trim())
        if (code != 0) throw GradleException("packageFineFeaturePacks failed ($code)")
    }
}

tasks.named("packageFeatureCompatPack").configure {
    finalizedBy("packageFineFeaturePacks")
}
