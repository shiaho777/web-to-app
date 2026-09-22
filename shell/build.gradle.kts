plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

android {
    namespace = "com.webtoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp"
        minSdk = 23

        targetSdk = 28
        versionCode = 69
        versionName = "2.6.7"

        buildConfigField("boolean", "SHELL_RUNTIME_ONLY", "true")

        // Shell template never ships to Google Play — it is the runtime host for *generated* apps,
        // which always use targetSdk 28 (fork+exec).

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                // Shell natives statically link libc++ so the template can drop
                // libc++_shared.so (~4.4MB raw across 4 ABIs). Only crypto_engine and
                // node_bridge used it. The HOST keeps c++_shared: ApkBuilder injects
                // the host's libc++_shared.so into NODEJS_APP exports for libnode.so.
                arguments += "-DANDROID_STL=c++_static"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("../app/src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        getByName("debug") {
            // The template is re-signed by ApkBuilder at export; its own v1 JAR
            // signature (MANIFEST.MF + CERT.SF/RSA, ~200KB raw) is dead weight.
            enableV1Signing = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")

            // Runtime Kotlin is synced from ../app into build/generated (see
            // syncShellRuntimeSources below) and ordered via preBuild deps —
            // never into the source tree, so git stays clean. Plain string
            // paths here: AGP forbids Provider instances in srcDirs.
            java.srcDirs(
                "src/main/java-overrides",
                "build/generated/shellRuntimeSrc",
                "build/generated/shellStrings",
            )
            // Shell-local res first: values/shell_theme_compat.xml declares the
            // Material3 color attrs / theme parents the shared app themes.xml
            // needs, so the material library can stay out of the template.
            res.srcDirs("src/main/res", "../app/src/main/res")
            assets.srcDirs("src/main/assets", "build/generated/shellRuntimeAssets")
        }
    }

    splits {
        abi {
            isEnable = false
        }
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {

        disable += "NullSafeMutableLiveData"

        disable += "ExpiredTargetSdkVersion"
        disable += "ExpiringTargetSdkVersion"
        disable += "OldTargetApi"
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Bundled-dependency legal boilerplate (META-INF/androidx/*/LICENSE.txt
            // and friends) — not needed at runtime, ~150KB raw across the file set.
            excludes += "META-INF/**/*.txt"
            excludes += "META-INF/**/LICENSE"
            excludes += "META-INF/**/NOTICE"
            excludes += "assets/omni.ja"
            excludes += "**/omni.ja"
            excludes += "**/org/bouncycastle/pqc/**"
        }
        jniLibs {
            useLegacyPackaging = true

            excludes += "**/libxul.so"
            excludes += "**/libmozglue.so"
            excludes += "**/libgeckoffi.so"
            excludes += "**/libmozavutil.so"
            excludes += "**/libmozavcodec.so"

            excludes += "**/libgkcodecs.so"
            excludes += "**/libminidump_analyzer.so"
            excludes += "**/libnss3.so"
            excludes += "**/libfreebl3.so"
            excludes += "**/libsoftokn3.so"
            excludes += "**/liblgpllibs.so"
            excludes += "**/libplugin-container.so"
            // GeckoView breakpad helper — depends on libmozglue.so which is already
            // excluded, so it can never load; dead weight carried by every APK.
            excludes += "**/libcrashhelper.so"

            excludes += "**/libphp.so"

            // Host-preview-only user-mode exec loader; generated APKs
            // (targetSdk 28) always execve directly and never load it.
            excludes += "**/libstatic_exec.so"

            // All shell natives are c++_static (see defaultConfig cmake arguments);
            // nothing in the template may DT_NEEDED libc++_shared.so. NODEJS_APP
            // exports still get it — ApkBuilder.injectNodeJsNativeLibs embeds the
            // host copy for libnode.so.
            excludes += "**/libc++_shared.so"

            // Cronet natives are injected into exported APKs by ApkBuilder when
            // 强制 HTTP/3 is enabled; the template never carries them.
            excludes += "**/libcronet*.so"
        }
    }
    androidResources {
        ignoreAssetsPattern = ""

        localeFilters += listOf("zh", "en", "ar")
    }
}

val syncShellRuntimeSources by tasks.registering(Sync::class) {
    description = "Sync runtime-only Kotlin sources from app module to shell"
    group = "build"

    from("../app/src/main/java")

    include(

        "**/ui/shell/**",
        "**/ui/theme/**",
        "**/ui/shared/**",
        "**/ui/design/**",
        "**/ui/plugin/**",

        "**/core/shell/**",
        "**/core/activation/**",
        "**/core/announcement/**",
        "**/core/adblock/**",
        "**/core/webview/**",
        "**/core/crypto/**",
        "**/core/i18n/**",
        "**/core/logging/**",
        "**/core/dns/**",
        "**/core/floatingwindow/**",
        "**/core/privacy/**",
        "**/core/appearance/**",
        "**/core/perf/**",
        "**/core/port/**",
        "**/core/extension/**",
        "**/core/plugin/**",
        "**/core/notification/**",
        "**/core/bgm/**",
        "**/core/engine/**",
        "**/core/scraper/**",
        "**/core/script/**",
        "**/core/ads/**",
        "**/core/network/**",
        "**/core/errorpage/**",
        "**/core/golang/**",
        "**/core/python/**",
        "**/core/nodejs/**",
        "**/core/php/**",
        "**/core/wordpress/**",
        "**/core/autostart/**",
        "**/core/background/**",
        "**/core/linux/**",
        "**/core/download/**",
        "**/core/sample/**",
        "**/core/frontend/**",
        "**/core/kernel/**",
        "**/core/share/**",

        "com/webtoapp/data/model/**",
        "com/webtoapp/data/converter/**",

        "**/ui/components/announcement/AnnouncementTemplates.kt",
        "**/ui/components/PremiumComponents.kt",
        "**/ui/components/EnhancedActivationDialog.kt",
        "**/ui/components/WebSwipeRefreshLayout.kt",
        "**/ui/components/VirtualNavigationBar.kt",
        "**/ui/components/StatusBarBackground.kt",
        "**/ui/components/LongPressMenu.kt",
        "**/ui/components/AutoRefreshCountdownOverlay.kt",

        "**/util/**"
    )

    exclude(

        "**/WebToAppApplication.kt",

        "**/core/crypto/EncryptedApkBuilder.kt",
        "**/core/crypto/SecurityInitializer.kt",

        "**/core/autostart/AutoStartLauncher.kt",
        "**/core/autostart/BootReceiver.kt",
        "**/core/autostart/ScheduledStartReceiver.kt",

        "**/util/FaviconFetcher.kt",
        "**/util/UrlMetadataFetcher.kt",
        "**/util/MediaStorage.kt",
        "**/util/ZipProjectImporter.kt",
        "**/util/HtmlProjectHelper.kt",
        "**/util/OfflineManager.kt",

        "**/core/frontend/GitHubRepoFetcher.kt",

        "**/core/extension/QrCodeUtils.kt",
        "**/core/extension/CodeSnippets.kt",
        "**/core/extension/ModuleTemplates.kt",
        "**/core/extension/DebugTestPages.kt",
        "**/core/extension/ModulePreset.kt",

        // Host-side plugin management: packages, import and legacy migration
        // only exist where the store exists. Generated APKs run embedded
        // payloads, never a store.
        "**/core/plugin/PluginStore.kt",
        "**/core/plugin/PluginImporter.kt",
        "**/core/plugin/PluginMigrator.kt",

        // Strings.kt / StringsA-E.kt carry the full 10-language editor surface
        // (~4.3 MB source, mostly editor-only text). Shell gets reduced copies
        // emitted by generateShellStrings below (referenced members only).
        "**/core/i18n/Strings*.kt"
    )

    into(layout.buildDirectory.dir("generated/shellRuntimeSrc"))
}

// Explicit wiring (no tasks.matching scan, which breaks configuration cache):
// generated sources must exist before any compilation.
tasks.named("preBuild") { dependsOn(syncShellRuntimeSources) }

/**
 * Emits reduced Strings.kt / StringsA-E.kt into build/generated/shellStrings:
 * only the members referenced by the synced runtime sources (plus the Strings
 * language-state infrastructure). Editor-only strings stay out of the shell
 * template and therefore out of every generated APK.
 */
abstract class GenerateShellStringsTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val script: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val i18nDir: DirectoryProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val syncedSrcDir: DirectoryProperty

    // Shell-only sources (src/main/java-overrides) also compile against
    // Strings — they must feed the reference scan too.
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val overridesDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @TaskAction
    fun generate() {
        // Self-contained python3 resolution (script closures can't serialize
        // for the configuration cache; mirrors resolvePython3Command).
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        val candidates: List<List<String>> = if (isWindows) {
            listOf(listOf("python3"), listOf("python"), listOf("py", "-3"))
        } else {
            listOf(listOf("python3"), listOf("python"))
        }
        var python = listOf("python3")
        for (candidate in candidates) {
            try {
                val probe = ProcessBuilder(candidate + "--version").redirectErrorStream(true).start()
                val output = probe.inputStream.bufferedReader().readText()
                if (probe.waitFor() == 0 && output.contains("Python 3")) {
                    python = candidate
                    break
                }
            } catch (_: Exception) {
                // Candidate unavailable; try the next one.
            }
        }

        val pb = ProcessBuilder(
            python + listOf(
                script.get().asFile.absolutePath,
                "--synced-src", syncedSrcDir.get().asFile.absolutePath,
                "--also-scan", overridesDir.get().asFile.absolutePath,
                "--app-i18n", i18nDir.get().asFile.absolutePath,
                "--out", outDir.get().asFile.absolutePath,
            )
        )
        pb.redirectErrorStream(true)
        val proc = pb.start()
        val log = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        logger.lifecycle(log.trim())
        if (code != 0) {
            throw GradleException("generateShellStrings failed ($code)")
        }
    }
}

val generateShellStrings by tasks.registering(GenerateShellStringsTask::class) {
    description = "Generate reduced Strings*.kt for shell runtime (referenced members only)"
    group = "build"
    dependsOn(syncShellRuntimeSources)

    script.set(rootProject.layout.projectDirectory.file("scripts/generate_shell_strings.py"))
    i18nDir.set(rootProject.layout.projectDirectory.dir("app/src/main/java/com/webtoapp/core/i18n"))
    syncedSrcDir.set(layout.buildDirectory.dir("generated/shellRuntimeSrc"))
    overridesDir.set(layout.projectDirectory.dir("src/main/java-overrides"))
    outDir.set(layout.buildDirectory.dir("generated/shellStrings"))
}

tasks.named("preBuild") { dependsOn(generateShellStrings) }

val syncShellRuntimeAssets by tasks.registering(Copy::class) {
    description = "Mirror runtime-only asset files from app module to shell template (single source of truth: app/src/main/assets)."
    group = "build"

    from("../app/src/main/assets") {

        include("php_router_server.php")

        // GeckoViewEngine installs this built-in WebExtension from
        // resource://android/assets/web_extensions/wta_native_bridge/ when the CORS
        // bypass / private-network bridge is enabled — without the asset in the shell
        // template, exported Gecko apps silently fail to install it (host-only asset).
        include("web_extensions/**")
    }

    into(layout.buildDirectory.dir("generated/shellRuntimeAssets"))
}

tasks.named("preBuild") { dependsOn(syncShellRuntimeAssets) }

// omni.ja arrives via the GeckoView AAR's assets and packaging.resources.excludes
// does not cover AAR assets, so strip it from the merged dir. Named wiring +
// execution-time-only access keeps this configuration-cache safe; the merge
// task stays up-to-date-aware (a skipped merge means outputs already stripped).
// Lazy exact-name match: the merge task is created after configuration, so
// named() would fail. The closure touches only task-scoped state.
tasks.matching { it.name == "mergeReleaseAssets" }.configureEach {
    doLast {
        val mergedAssetsDir = outputs.files.files.firstOrNull { it.isDirectory }
        val omniJa = mergedAssetsDir?.resolve("omni.ja")
        if (omniJa != null && omniJa.exists()) {
            val sizeKb = omniJa.length() / 1024
            if (omniJa.delete()) {
                logger.lifecycle("[shell-slim] Removed bundled GeckoView omni.ja from template assets (${sizeKb} KB)")
            } else {
                logger.warn("[shell-slim] Failed to remove omni.ja from $mergedAssetsDir")
            }
        }
    }
}

abstract class SyncNativeExecutableJniLibsTask : DefaultTask() {
    @get:Input
    abstract val variantName: org.gradle.api.provider.Property<String>

    @get:Input
    abstract val buildTypeName: org.gradle.api.provider.Property<String>

    @get:Input
    abstract val executableName: org.gradle.api.provider.Property<String>

    @get:Input
    abstract val packagedLibraryName: org.gradle.api.provider.Property<String>

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val cxxRoot: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun sync() {
        val cxxRootDir = cxxRoot.asFile.get()
        if (!cxxRootDir.exists()) {
            throw GradleException("CXX output not found for ${variantName.get()}: ${cxxRootDir.absolutePath}")
        }

        val executableTargets = cxxRootDir.walkTopDown()
            .filter { file ->
                file.isFile &&
                    file.name == executableName.get() &&
                    file.parentFile?.parentFile?.name == "obj"
            }
            .toList()

        if (executableTargets.isEmpty()) {
            throw GradleException("${executableName.get()} artifacts not found for ${variantName.get()} under ${cxxRootDir.absolutePath}")
        }

        val outputRoot = outputDir.get().asFile
        outputRoot.deleteRecursively()
        outputRoot.mkdirs()

        executableTargets.forEach { binary ->
            val abi = binary.parentFile.name
            val destFile = outputRoot.resolve("$abi/${packagedLibraryName.get()}")
            destFile.parentFile.mkdirs()
            binary.copyTo(destFile, overwrite = true)
            destFile.setExecutable(true, false)
        }
    }
}

androidComponents {
    onVariants { variant ->
        val capName = variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val variantBuildTypeName = variant.buildType ?: "release"
        val cxxBuildType = if (variantBuildTypeName.equals("debug", ignoreCase = true)) "Debug" else "RelWithDebInfo"
        val nativeBuildTaskName = "buildCMake$cxxBuildType"
        val syncNodeLauncherTask = tasks.register<SyncNativeExecutableJniLibsTask>("syncNodeLauncherJniLibs$capName") {
            group = "build"
            description = "Copies ABI-specific node launcher executables into generated jniLibs for ${variant.name}."
            variantName.set(variant.name)
            buildTypeName.set(variantBuildTypeName)
            executableName.set("node_launcher")
            packagedLibraryName.set("libnode_launcher.so")
            cxxRoot.set(layout.buildDirectory.dir("intermediates/cxx/$cxxBuildType"))
            outputDir.set(layout.buildDirectory.dir("generated/jniLibs/nodeLauncher/${variant.name}"))
            dependsOn(nativeBuildTaskName)
        }

        val syncGoLoaderTask = tasks.register<SyncNativeExecutableJniLibsTask>("syncGoExecLoaderJniLibs$capName") {
            group = "build"
            description = "Copies ABI-specific Go exec loader executables into generated jniLibs for ${variant.name}."
            variantName.set(variant.name)
            buildTypeName.set(variantBuildTypeName)
            executableName.set("go_exec_loader")
            packagedLibraryName.set("libgo_exec_loader.so")
            cxxRoot.set(layout.buildDirectory.dir("intermediates/cxx/$cxxBuildType"))
            outputDir.set(layout.buildDirectory.dir("generated/jniLibs/goExecLoader/${variant.name}"))
            dependsOn(nativeBuildTaskName)
        }

        // NOTE: no explicit merge${capName}NativeLibs wiring — the
        // addGeneratedSourceDirectory calls below already infer task deps.

        variant.sources.jniLibs?.addGeneratedSourceDirectory(
            syncNodeLauncherTask,
            SyncNativeExecutableJniLibsTask::outputDir
        )
        variant.sources.jniLibs?.addGeneratedSourceDirectory(
            syncGoLoaderTask,
            SyncNativeExecutableJniLibsTask::outputDir
        )
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Native Google sign-in through the Jetpack Credential Manager (NativeBridge).
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.5.0-alpha17")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-messaging")

    implementation("org.bouncycastle:bcpkix-jdk15to18:1.78.1")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.78.1")

    implementation("androidx.webkit:webkit:1.9.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("org.tukaani:xz:1.9")

    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:142.0.20250827004350")

    // Forced HTTP/3 upstream (see app/build.gradle.kts): classes only, natives are
    // injected into exported APKs by ApkBuilder when 强制 HTTP/3 is enabled.
    implementation("org.chromium.net:cronet-embedded:143.7445.0")

    implementation("androidx.media:media:1.7.0")
}
