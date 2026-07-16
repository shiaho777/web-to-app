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
    sourceSets {
        getByName("main") {
            java.srcDir("src/shellOnly/java")
        }
    }
    namespace = "com.webtoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp"
        minSdk = 23

        targetSdk = 28
        versionCode = 48
        versionName = "2.2.0"

        buildConfigField("boolean", "SHELL_RUNTIME_ONLY", "true")

        vectorDrawables {
            useSupportLibrary = true
        }


        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        getByName("debug")
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

            java.srcDirs("src/main/java", "src/main/java-overrides")
            res.srcDirs("src/main/res")
            assets.srcDirs("src/main/assets")
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
            excludes += "org/bouncycastle/pqc/**"
            excludes += "**/lowmcL*.properties"
            excludes += "kotlin/**"
            excludes += "DebugProbesKt.bin"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/versions/**"
            excludes += "**/dexopt/baseline.prof"
            excludes += "**/dexopt/baseline.profm"
            excludes += "dexopt/**"
            excludes += "assets/dexopt/**"
            excludes += "kotlin-tooling-metadata.json"
            excludes += "META-INF/version-control-info.textproto"
            excludes += "META-INF/com/android/build/gradle/app-metadata.properties"
            excludes += "META-INF/*.version"
            excludes += "META-INF/androidx*.version"
            excludes += "META-INF/com.*.version"
            excludes += "META-INF/kotlinx_*.version"
            excludes += "META-INF/services/kotlinx.coroutines.*"
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

            excludes += "**/libphp.so"
            excludes += "**/libnode_bridge.so"
            excludes += "**/libnode_launcher.so"
            excludes += "**/libgo_exec_loader.so"
            excludes += "**/libapk_optimizer.so"
            excludes += "**/libcrypto_optimized.so"
            excludes += "**/libperf_engine.so"
            excludes += "**/libsys_optimizer.so"
            excludes += "**/libhardware_control.so"
            excludes += "**/libpython3.so"
            excludes += "**/libmusl-linker.so"
            excludes += "**/libc++_shared.so"
            excludes += "**/libcrypto_engine.so"
            excludes += "**/libbrowser_kernel.so"
        }
    }
    androidResources {
        ignoreAssetsPattern = ""

        localeFilters += listOf("en")
        // keep default density set; locales trimmed above

    }
}


val slimShellStrings by tasks.registering {
    group = "build"
    description = "Split shell i18n into per-language packs (all 10 languages)"
    val input = rootProject.file("app/src/main/java/com/webtoapp/core/i18n/Strings.kt")
    val slimRoot = layout.buildDirectory.dir("generated/slim-i18n")
    val output = layout.buildDirectory.file("generated/slim-i18n/com/webtoapp/core/i18n/Strings.kt")
    val tableOut = layout.buildDirectory.file("generated/slim-i18n/com/webtoapp/core/i18n/ShellStringTable.kt")
    val assetsOut = layout.buildDirectory.dir("generated/slim-i18n/assets/i18n")
    inputs.file(input)
    inputs.file(rootProject.file("scripts/slim_shell_strings.py"))
    outputs.file(output)
    outputs.file(tableOut)
    outputs.dir(assetsOut)
    doLast {
        val outFile = output.get().asFile
        outFile.parentFile.mkdirs()
        val cmd = listOf(
            "python3",
            rootProject.file("scripts/slim_shell_strings.py").absolutePath,
            input.absolutePath,
            outFile.absolutePath
        )
        val pb = ProcessBuilder(cmd)
        pb.redirectErrorStream(true)
        val proc = pb.start()
        val log = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        val tableFile = tableOut.get().asFile
        val assetsDir = assetsOut.get().asFile
        if (code != 0 || !outFile.isFile || !tableFile.isFile || !assetsDir.isDirectory) {
            throw GradleException("slimShellStrings failed ($code): $log")
        }
        logger.lifecycle("[shell-slim] $log".trim())
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

        "**/core/shell/**",
        "**/core/activation/**",
        "**/core/announcement/**",
        "**/core/adblock/**",
        "**/core/webview/**",
        "**/core/crypto/**",
        "**/core/i18n/**",
        "**/core/logging/**",
        "**/core/dns/**",
        "**/core/forcedrun/**",
        "**/core/floatingwindow/**",
        "**/core/privacy/**",
        "**/core/appearance/**",
        "**/core/actions/**",
        "**/core/perf/**",
        "**/core/port/**",
        "**/core/extension/**",
        "**/core/notification/**",
        "**/core/backgroundrun/**",
        "**/core/translate/**",
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
        "**/core/linux/LocalDnsBridgeProxy.kt",
        "**/core/download/**",
        "**/core/kernel/**",

        "com/webtoapp/data/model/**",
        "com/webtoapp/data/converter/**",

                                "**/ui/components/announcement/AnnouncementTemplates.kt",
        "**/ui/components/PremiumComponents.kt",
        "**/ui/components/EdgeSwipeRefreshLayout.kt",
        "**/ui/components/VirtualNavigationBar.kt",
        "**/ui/components/StatusBarBackground.kt",
        "**/ui/components/LongPressMenu.kt",
        "**/ui/components/ForcedRunCountdownOverlay.kt",
        "**/ui/components/AutoRefreshCountdownOverlay.kt",

        "**/util/**"
    )

    exclude(
        "**/WebToAppApplication.kt",

        "**/core/crypto/EncryptedApkBuilder.kt",
        "**/core/crypto/SecurityInitializer.kt",
        "**/core/crypto/AssetEncryptor.kt",

        "**/core/sample/**",
        "**/core/frontend/**",
        "**/*SampleManager.kt",
        "**/ui/theme/ThemeManager.kt",
        "**/core/linux/PerformanceOptimizer.kt",

        "**/core/extension/BuiltInModules.kt",
        "**/core/extension/BuiltInChromeExtensions.kt",
        "**/core/extension/BrowserExtensionStore.kt",
        "**/core/extension/ExtensionPanelScript.kt",
        "**/core/extension/ChromeExtensionPolyfill.kt",
        "**/core/extension/ChromeExtensionMobileCompat.kt",
        "**/core/extension/UserScriptWindowScript.kt",
        "**/core/extension/panel/**",
        "**/core/appearance/BrowserDisguiseJsGenerator.kt",
        "**/ui/shell/TranslateScriptProvider.kt",

        "**/core/privacy/IsolationManager.kt",
        "**/core/privacy/FingerprintGenerator.kt",
        "**/core/privacy/IsolationScriptInjector.kt",

        "**/core/errorpage/ErrorPageGames.kt",

        "**/core/bgm/OnlineMusicApi.kt",
        "**/core/bgm/OnlineMusicDownloader.kt",
        "**/core/bgm/BgmPlayer.kt",

        "**/core/forcedrun/ForcedRunHardwareController.kt",
        "**/core/forcedrun/NativeHardwareController.kt",


        "**/core/autostart/AutoStartLauncher.kt",
        "**/core/autostart/BootReceiver.kt",
        "**/core/autostart/ScheduledStartReceiver.kt",

        "**/util/AppUpdateChecker.kt",
        "**/util/FaviconFetcher.kt",
        "**/util/UrlMetadataFetcher.kt",
        "**/util/ZipProjectImporter.kt",
        "**/util/HtmlProjectHelper.kt",
        "**/util/OfflineManager.kt",
        "**/util/SvgIconMapper.kt",
        "**/util/IconLibraryStorage.kt",
        "**/util/IconStorage.kt",
        "**/util/ConfigPresetStorage.kt",
        "**/util/PermissionPresetStorage.kt",
        "**/util/SplashStorage.kt",
        "**/util/HtmlProjectProcessor.kt",
        "**/util/CacheManager.kt",
        "**/util/HtmlStorage.kt",
        "**/util/NetworkTrustStorage.kt",
        "**/util/TextFileClassifier.kt",
        "**/util/MediaStorage.kt",
        "**/util/Constants.kt",

        "**/core/frontend/GitHubRepoFetcher.kt",

        "**/core/extension/QrCodeUtils.kt",
        "**/core/extension/CodeSnippets.kt",
        "**/core/extension/ModuleTemplates.kt",
        "**/core/extension/DebugTestPages.kt",
        "**/core/extension/ModulePreset.kt",

        "**/core/engine/GeckoViewEngine.kt",
        "**/core/engine/download/GeckoEngineDownloader.kt",
        "**/core/gecko/**",
        "**/core/notification/NotificationFcmManager.kt",
        "**/core/notification/NotificationFcmService.kt",
        "**/core/webview/TlsMitmBridge.kt",
        "**/core/webview/TlsMitmCaManager.kt",
        "**/core/webview/TlsUpstreamConnector.kt",

        "**/core/scraper/**",
        "**/core/ads/**",

        "**/core/i18n/Strings.kt",
        "**/core/i18n/AiPromptManager.kt",
        "**/core/i18n/RandomAppNameGenerator.kt",
        "**/data/model/AiConfig.kt",
        "**/data/model/AppCategory.kt",
        "**/ui/design/WtaPreviews.kt",
        "**/ui/theme/ThemeAnimations.kt",
        "**/ui/theme/ThemeRevealAnimation.kt",
        "**/ui/components/ThemeSelector.kt",
        "**/ui/components/StatusBarPreview.kt",
        "**/ui/components/PermissionRationale.kt"
    )

    into("src/main/java")
}

tasks.named("syncShellRuntimeSources").configure {
    dependsOn(slimShellStrings)
    doLast {
        val slim = layout.buildDirectory.file("generated/slim-i18n/com/webtoapp/core/i18n/Strings.kt").get().asFile
        val table = layout.buildDirectory.file("generated/slim-i18n/com/webtoapp/core/i18n/ShellStringTable.kt").get().asFile
        val destDir = file("src/main/java/com/webtoapp/core/i18n")
        destDir.mkdirs()
        slim.copyTo(destDir.resolve("Strings.kt"), overwrite = true)
        table.copyTo(destDir.resolve("ShellStringTable.kt"), overwrite = true)
    }
}

tasks.matching { it.name.startsWith("compile") && it.name.contains("Kotlin") }.configureEach {
    dependsOn(syncShellRuntimeSources)
}

val syncShellRuntimeAssets by tasks.registering {
    description = "Mirror runtime-only asset files from app module to shell template (single source of truth: app/src/main/assets)."
    group = "build"
    dependsOn(slimShellStrings)
    val phpSrc = rootProject.file("app/src/main/assets/php_router_server.php")
    val i18nSrc = layout.buildDirectory.dir("generated/slim-i18n/assets/i18n")
    inputs.file(phpSrc)
    inputs.dir(i18nSrc)
    outputs.file(file("src/main/assets/php_router_server.php"))
    outputs.dir(file("src/main/assets/i18n"))
    doLast {
        val assetsDir = file("src/main/assets")
        assetsDir.mkdirs()
        val phpOut = assetsDir.resolve("php_router_server.php")
        if (phpOut.exists()) {
            phpOut.delete()
        }
        val i18nOut = assetsDir.resolve("i18n")
        i18nOut.mkdirs()
        i18nOut.listFiles()?.forEach { it.delete() }
        val enPack = i18nSrc.get().asFile.resolve("en.pack")
        if (enPack.isFile) {
            enPack.copyTo(i18nOut.resolve("en.pack"), overwrite = true)
        } else {
            i18nSrc.get().asFile.listFiles()?.forEach { src ->
                if (src.isFile && src.name.endsWith(".pack")) {
                    src.copyTo(i18nOut.resolve(src.name), overwrite = true)
                }
            }
        }
        val hostI18n = rootProject.file("app/src/main/assets/shell_i18n")
        hostI18n.mkdirs()
        hostI18n.listFiles()?.forEach { it.delete() }
        i18nSrc.get().asFile.listFiles()?.forEach { src ->
            if (src.isFile && src.name.endsWith(".pack")) {
                src.copyTo(hostI18n.resolve(src.name), overwrite = true)
            }
        }
    }
}

tasks.matching { it.name.startsWith("merge") && it.name.contains("Assets") }.configureEach {
    dependsOn(syncShellRuntimeAssets)
}
tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn(syncShellRuntimeAssets)
}

tasks.matching { it.name.startsWith("merge") && it.name.contains("Assets") }.configureEach {
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
}

dependencies {
    implementation(project(":feature-api"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    compileOnly("androidx.documentfile:documentfile:1.0.1")

    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    compileOnly("androidx.room:room-common:2.6.1")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")

    implementation("androidx.webkit:webkit:1.9.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    compileOnly("org.apache.commons:commons-compress:1.26.0")
}

tasks.register("stripShellTemplateBloat") {
    group = "build"
    description = "Remove non-runtime bloat entries from shell release APK"
    dependsOn("packageRelease")
    val apk = layout.buildDirectory.file("outputs/apk/release/shell-release.apk")
    inputs.file(apk)
    outputs.file(apk)
    doLast {
        val file = apk.get().asFile
        if (!file.isFile) return@doLast
        val script = rootProject.file("scripts/strip_shell_apk_bloat.py")
        val pb = ProcessBuilder("python3", script.absolutePath, file.absolutePath)
        pb.redirectErrorStream(true)
        val proc = pb.start()
        val log = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        if (code != 0) {
            throw GradleException("stripShellTemplateBloat failed ($code): $log")
        }
        logger.lifecycle("[shell-slim] $log".trim())
    }
}

tasks.matching { it.name == "packageRelease" }.configureEach {
    finalizedBy("stripShellTemplateBloat")
}
