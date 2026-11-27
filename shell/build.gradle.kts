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

val isFdroidBuild: Boolean = run {
    val raw = findProperty("fdroid")?.toString()?.trim()?.lowercase()
    raw != null && raw != "false" && raw != "0" && raw != "no"
}

if (isFdroidBuild) {
    configurations.configureEach {
        exclude(group = "com.google.android.gms")
        exclude(group = "com.google.firebase")
        exclude(group = "com.google.android.libraries.identity.googleid")
    }
}

android {
    namespace = "com.webtoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp"
        minSdk = 23

        targetSdk = 35
        versionCode = 89
        versionName = "2.9.5"

        buildConfigField("boolean", "SHELL_RUNTIME_ONLY", "true")
        buildConfigField("boolean", "FDROID", isFdroidBuild.toString())


        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        externalNativeBuild {
            cmake {
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

            java.srcDirs(
                "src/main/java-overrides",
                "build/generated/shellRuntimeSrc",
                "build/generated/shellStrings",
            )
            res.srcDirs("../app/src/main/res")
            assets.srcDirs("src/main/assets", "build/generated/shellRuntimeAssets")
        }
        if (isFdroidBuild) {
            getByName("debug") {
                manifest.srcFile("src/fdroid/AndroidManifest.xml")
            }
            getByName("release") {
                manifest.srcFile("src/fdroid/AndroidManifest.xml")
            }
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
        freeCompilerArgs += "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
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
            excludes += "**/libcrashhelper.so"

            excludes += "**/libphp.so"

            excludes += "**/libstatic_exec.so"

            excludes += "**/libc++_shared.so"

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
    if (isFdroidBuild) {
        from("../app/src/fdroid/java")
    } else {
        from("../app/src/google/java")
    }

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
        "**/core/script/**",
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
        "**/core/kernel/**",
        "**/core/share/**",

        "com/webtoapp/data/model/**",
        "com/webtoapp/data/converter/**",

        "**/ui/components/announcement/AnnouncementTemplates.kt",
        "**/ui/components/PremiumComponents.kt",
        "**/ui/components/EnhancedActivationDialog.kt",
        "**/ui/components/RemainingDaysNoticeDialog.kt",
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

        "**/core/plugin/PluginStore.kt",
        "**/core/plugin/PluginImporter.kt",
        "**/core/plugin/PluginMigrator.kt",
        "**/core/plugin/ChromePluginRows.kt",

        "**/core/linux/StaticExecProcess.kt",
        "**/core/linux/NativeNodeEngine.kt",
        "**/core/linux/HtmlProjectOptimizer.kt",
        "**/core/linux/PerformanceOptimizerApk.kt",
        "**/core/linux/LinuxEnvironmentManager.kt",
        "**/core/linux/LocalBuildEnvironment.kt",
        "**/core/linux/NodeProjectBuilder.kt",
        "**/core/linux/PureBuildEngine.kt",

        "**/core/i18n/Strings*.kt",

        "**/core/i18n/HostLanguage.kt",

        "**/core/scraper/**",
        "**/core/frontend/**",
        "**/core/sample/**",
        "**/core/php/PhpSampleManager.kt",
        "**/core/nodejs/NodeSampleManager.kt",
        "**/core/wordpress/WordPressSampleManager.kt",
        "**/core/python/PythonSampleManager.kt",
        "**/core/golang/GoSampleManager.kt"
    )

    into(layout.buildDirectory.dir("generated/shellRuntimeSrc"))
}

tasks.named("preBuild") { dependsOn(syncShellRuntimeSources) }

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

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val overridesDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outDir: DirectoryProperty

    @TaskAction
    fun generate() {
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

        include("web_extensions/**")
    }

    into(layout.buildDirectory.dir("generated/shellRuntimeAssets"))
}

tasks.named("preBuild") { dependsOn(syncShellRuntimeAssets) }

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
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.documentfile:documentfile:1.0.1")

    if (!isFdroidBuild) {
        implementation("androidx.credentials:credentials:1.5.0")
        implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
        implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    }

    implementation("com.google.android.material:material:1.10.0")
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

    implementation("androidx.room:room-runtime:2.7.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("io.coil-kt:coil-compose:2.5.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")

    if (!isFdroidBuild) {
        implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
        implementation("com.google.firebase:firebase-messaging")
    }

    implementation("org.bouncycastle:bcpkix-jdk15to18:1.78.1")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.78.1")

    implementation("androidx.webkit:webkit:1.9.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("org.tukaani:xz:1.9")

    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:142.0.20250827004350")

    implementation("org.chromium.net:cronet-embedded:143.7445.0")

    implementation("androidx.media:media:1.7.0")
}
