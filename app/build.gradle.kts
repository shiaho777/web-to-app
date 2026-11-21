import java.util.Properties
import java.util.zip.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.protobuf")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

val releaseSigningStoreFile = localProperties.getProperty("signing.storeFile")
    ?.takeIf { it.isNotBlank() }
    ?.let { rootProject.file(it) }
val hasReleaseSigningConfig = releaseSigningStoreFile?.isFile == true &&
    !localProperties.getProperty("signing.storePassword").isNullOrBlank() &&
    !localProperties.getProperty("signing.keyAlias").isNullOrBlank() &&
    !localProperties.getProperty("signing.keyPassword").isNullOrBlank()

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

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = releaseSigningStoreFile
                storePassword = localProperties.getProperty("signing.storePassword")
                keyAlias = localProperties.getProperty("signing.keyAlias")
                keyPassword = localProperties.getProperty("signing.keyPassword")
                enableV1Signing = true
            }
        }
    }
    namespace = "com.webtoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.webtoapp"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 24

        targetSdk = 36
        versionCode = 87
        versionName = "2.8.8"
        buildConfigField("boolean", "SHELL_RUNTIME_ONLY", "false")
        buildConfigField("boolean", "FDROID", isFdroidBuild.toString())

        vectorDrawables {
            useSupportLibrary = true
        }

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        externalNativeBuild {
            cmake {
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("standard") {
        }
        create("gplay") {
            applicationId = "shiaho.webtoapp"
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = when {
                isFdroidBuild -> null
                hasReleaseSigningConfig -> signingConfigs.getByName("release")
                (project.findProperty("allowDebugSignedRelease") as? String) == "true" ->
                    signingConfigs.getByName("debug")
                else -> throw GradleException(
                    "Release build has no valid signing config. Configure signing.storeFile / " +
                        "signing.storePassword / signing.keyAlias / signing.keyPassword in local.properties " +
                        "before assembling a release APK for distribution. Refusing to silently sign with " +
                        "the debug key — a debug-signed release breaks upgrades for existing users " +
                        "('signatures do not match'). For a throwaway debug-signed build (e.g. CI smoke " +
                        "build, never distribute it), pass -PallowDebugSignedRelease=true."
                )
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
            excludes += "assets/omni.ja"
            excludes += "**/omni.ja"
            excludes += "**/org/bouncycastle/pqc/**"
            excludes += "**/*.po"
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

            excludes += "**/libcrypto_engine.so"

            excludes += "**/libcronet*.so"
        }
    }
    androidResources {
        ignoreAssetsPattern = "*.po:*.ja"

        localeFilters += listOf("zh", "en", "ar")
    }

    sourceSets {
        getByName("main") {
            if (isFdroidBuild) {
                java.srcDir("src/fdroid/java")
            } else {
                java.srcDir("src/google/java")
            }
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
}

val shellTemplateOutput = project(":shell").layout.buildDirectory.file("outputs/apk/release/shell-release.apk")
val skipShellTemplateSync = providers.gradleProperty("skipShellTemplateSync").map(String::toBoolean).orElse(false)

tasks.register<Copy>("syncShellTemplateApk") {
    description = "Builds the dedicated shell template APK and copies it into the app assets."
    group = "build"
    dependsOn(":shell:assembleRelease")
    from(shellTemplateOutput)
    into(file("src/main/assets/template"))
    rename { "webview_shell.apk" }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    if (!skipShellTemplateSync.get()) {
        dependsOn("syncShellTemplateApk")
    }
}

val cloneHostAar = rootProject.layout.projectDirectory.file("clone-host/build/outputs/aar/clone-host-release.aar")
val androidSdkDir = android.sdkDirectory

tasks.register<Copy>("syncCloneHostDex") {
    description = "Extracts classes.jar from clone-host AAR and converts it to a DEX asset for APK cloning."
    group = "build"
    dependsOn(":clone-host:assembleRelease")

    enabled = false

    val dexOutputDir = layout.projectDirectory.dir("src/main/assets/clone_host").asFile
    val intermediateDir = layout.buildDirectory.dir("intermediates/clone-host-extract")
    val sdkDir = androidSdkDir

    doFirst {
        intermediateDir.get().asFile.mkdirs()
        dexOutputDir.mkdirs()
    }

    from(cloneHostAar)
    into(intermediateDir)
    rename { "clone-host.aar" }

    doLast {
        val aarFile = intermediateDir.get().file("clone-host.aar").asFile
        if (!aarFile.exists()) {
            throw GradleException("clone-host AAR not found at ${aarFile.absolutePath}")
        }

        val classesJar = intermediateDir.get().file("classes.jar").asFile
        ZipFile(aarFile).use { zip ->
            val entry = zip.getEntry("classes.jar")
                ?: throw GradleException("classes.jar not found in clone-host AAR")
            zip.getInputStream(entry).use { input ->
                classesJar.outputStream().use { output -> input.copyTo(output) }
            }
        }

        val androidJar = sdkDir.resolve("platforms/android-36/android.jar")
        if (!androidJar.exists()) {
            throw GradleException("android.jar not found at ${androidJar.absolutePath}")
        }

        val d8 = sdkDir.resolve("build-tools")
            .listFiles()?.maxByOrNull { it.name }
            ?.resolve("d8")
            ?: throw GradleException("d8 not found in build-tools")

        dexOutputDir.mkdirs()

        val process = ProcessBuilder(
            d8.absolutePath,
            "--release",
            "--min-api", "23",
            "--lib", androidJar.absolutePath,
            "--output", dexOutputDir.absolutePath,
            classesJar.absolutePath
        ).redirectErrorStream(true).start()

        val output = process.inputStream.bufferedReader().readText()
        if (process.waitFor() != 0) {
            throw GradleException("d8 failed to dex clone-host: $output")
        }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("syncCloneHostDex")
}

tasks.register("testClasses") {
    group = "verification"
    description = "Compatibility alias for JVM-style test class compilation in the Android app module."
    dependsOn("compileDebugUnitTestSources")
}

tasks.register("unitTestClasses") {
    group = "verification"
    description = "Compatibility alias for Android unit test class compilation in the Android app module."
    dependsOn("compileDebugUnitTestSources")
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

abstract class BuildMuslBridgeTask : DefaultTask() {
    @get:Input
    abstract val muslVersion: org.gradle.api.provider.Property<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val patchFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val x80ShimFile: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val ndkToolchainDir: DirectoryProperty

    @get:Internal
    abstract val workDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    private fun run(cmd: List<String>, cwd: File? = null, env: Map<String, String> = emptyMap()) {
        val proc = ProcessBuilder(cmd).apply {
            if (cwd != null) directory(cwd)
            environment().putAll(env)
            redirectErrorStream(true)
        }.start()
        val out = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        if (code != 0) throw GradleException("command failed ($code): ${cmd.joinToString(" ")}\n$out")
    }

    @TaskAction
    fun build() {
        val tc = ndkToolchainDir.get().asFile
        if (!File(tc, "bin/clang").isFile) {
            throw GradleException("NDK toolchain not found at $tc (set NDKVersion or install the NDK)")
        }
        val libccDir = File(tc, "lib/clang")
            .listFiles { f -> f.isDirectory }?.map { File(it, "lib/linux") }.orEmpty()
        fun libcc(name: String): String =
            libccDir.firstOrNull { File(it, name).isFile }?.resolve(name)?.absolutePath
                ?: throw GradleException("compiler-rt $name not found under $tc")

        val work = workDir.get().asFile.apply { mkdirs() }
        val version = muslVersion.get()
        val tarball = File(work, "musl-$version.tar.gz")
        if (!tarball.isFile) {
            run(
                listOf(
                    "curl", "-fsSL", "--max-time", "180", "-o", tarball.absolutePath,
                    "https://musl.libc.org/releases/musl-$version.tar.gz"
                )
            )
        }
        val src = File(work, "musl-$version")
        src.deleteRecursively()
        run(listOf("tar", "xzf", tarball.absolutePath, "-C", work.absolutePath))
        run(listOf("patch", "-p1", "-d", src.absolutePath, "--input", patchFile.get().asFile.absolutePath))
        File(src, "src/math/x86_64").also { it.mkdirs() }
            .resolve(x80ShimFile.get().asFile.name).run {
                x80ShimFile.get().asFile.copyTo(this, overwrite = true)
            }

        val clang = File(tc, "bin/clang").absolutePath
        val strip = File(tc, "bin/llvm-strip").absolutePath
        val jobs = Runtime.getRuntime().availableProcessors().toString()
        val targets = listOf(
            "arm64-v8a" to ("aarch64-linux-musl" to "libclang_rt.builtins-aarch64-android.a"),
            "x86_64" to ("x86_64-linux-musl" to "libclang_rt.builtins-x86_64-android.a")
        )
        val out = outputDir.get().asFile
        out.deleteRecursively()
        for ((abi, target) in targets) {
            val (triple, rt) = target
            val buildDir = File(work, "build-$abi").apply { deleteRecursively(); mkdirs() }
            val libccPath = libcc(rt)
            val configureEnv = mapOf(
                "CC" to "$clang --target=$triple",
                "CROSS_COMPILE" to "${File(tc, "bin/llvm-").absolutePath}",
                "CFLAGS" to "-O2",
                "LDFLAGS" to "-Wl,-z,max-page-size=16384"
            )
            run(listOf("../musl-$version/configure", "--target=$triple"), cwd = buildDir, env = configureEnv)
            run(listOf("make", "-j$jobs", "LIBCC=$libccPath"), cwd = buildDir, env = configureEnv)
            val dest = File(out, "$abi/libmusl-linker.so")
            dest.parentFile.mkdirs()
            run(listOf(strip, File(buildDir, "lib/libc.so").absolutePath, "-o", dest.absolutePath))
            dest.setExecutable(true, false)
        }
    }
}

val muslBridgeTask = tasks.register<BuildMuslBridgeTask>("buildMuslBridge") {
    group = "build"
    description = "Builds the patched musl dynamic linker (libmusl-linker.so) with the W^X exec bridge into generated jniLibs."
    muslVersion.set("1.2.5")
    patchFile.set(rootProject.file("scripts/patches/musl-1.2.5-wta-exec-bridge.patch"))
    x80ShimFile.set(rootProject.file("scripts/musl-bridge/wta_mulxc3.c"))
    val hostTag = if (System.getProperty("os.name").lowercase().contains("mac")) "darwin-x86_64" else "linux-x86_64"
    ndkToolchainDir.set(File(android.ndkDirectory, "toolchains/llvm/prebuilt/$hostTag"))
    workDir.set(layout.buildDirectory.dir("musl-bridge"))
    outputDir.set(layout.buildDirectory.dir("generated/jniLibs/muslBridge"))
}

androidComponents {
    onVariants { variant ->
        val capName = variant.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val variantBuildTypeName = variant.buildType ?: "release"
        val cxxBuildType = if (variantBuildTypeName.equals("debug", ignoreCase = true)) "Debug" else "RelWithDebInfo"
        val nativeBuildTaskName = "buildCMake$cxxBuildType"

        tasks.matching { it.name == "merge${capName}NativeLibs" }.configureEach {
            dependsOn(muslBridgeTask)
        }
        variant.sources.jniLibs?.addGeneratedSourceDirectory(
            muslBridgeTask,
            BuildMuslBridgeTask::outputDir
        )
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

        tasks.matching { it.name == "merge${capName}NativeLibs" }.configureEach {
            dependsOn(syncNodeLauncherTask)
            dependsOn(syncGoLoaderTask)
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

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.5"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {

                create("java") {
                    option("lite")
                }
            }
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.documentfile:documentfile:1.0.1")

    implementation("com.google.protobuf:protobuf-javalite:3.25.5")

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
    implementation("androidx.navigation:navigation-compose:2.7.5")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.room:room-runtime:2.7.2")
    implementation("androidx.room:room-ktx:2.7.2")
    ksp("androidx.room:room-compiler:2.7.2")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-video:2.5.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:okhttp-dnsoverhttps:4.12.0")

    if (!isFdroidBuild) {
        implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
        implementation("com.google.firebase:firebase-messaging")
    }

    implementation("org.bouncycastle:bcpkix-jdk15to18:1.78.1")
    implementation("org.bouncycastle:bcprov-jdk15to18:1.78.1")

    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")

    implementation("androidx.webkit:webkit:1.9.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("org.apache.commons:commons-compress:1.26.0")
    implementation("org.tukaani:xz:1.9")

    implementation("com.android.tools.build:apksig:8.3.0")

    implementation("org.mozilla.geckoview:geckoview-arm64-v8a:142.0.20250827004350")

    implementation("org.chromium.net:cronet-embedded:143.7445.0")

    if (!isFdroidBuild) {
        implementation("androidx.credentials:credentials:1.5.0")
        implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
        implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    }

    implementation("androidx.media:media:1.7.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("com.google.truth:truth:1.1.5")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core:1.5.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

fun resolvePython3Command(): List<String> {
    val isWindows = System.getProperty("os.name").lowercase().contains("windows")
    val candidates: List<List<String>> = if (isWindows) {
        listOf(listOf("python3"), listOf("python"), listOf("py", "-3"))
    } else {
        listOf(listOf("python3"), listOf("python"))
    }
    for (candidate in candidates) {
        try {
            val probe = ProcessBuilder(candidate + "--version").redirectErrorStream(true).start()
            val output = probe.inputStream.bufferedReader().readText()
            if (probe.waitFor() == 0 && output.contains("Python 3")) {
                return candidate
            }
        } catch (_: Exception) {
        }
    }
    return listOf("python3")
}

tasks.register("checkConfigFieldDrift") {
    group = "verification"
    description = "Detect field-name drift between ApkConfig payload keys and ShellConfig @SerializedName (static guard for preview/export config consistency)"
    val script = rootProject.file("scripts/check_config_field_drift.py")
    val payloadFile = file("src/main/java/com/webtoapp/core/apkbuilder/ApkConfigJsonFactory.kt")
    val apkConfigFile = file("src/main/java/com/webtoapp/core/apkbuilder/ApkConfig.kt")
    val shellConfigFile = file("src/main/java/com/webtoapp/core/shell/ShellModeManager.kt")
    val allowlist = rootProject.file("scripts/config_field_drift_allowlist.json")
    val rootDir = rootProject.projectDir
    inputs.files(script, payloadFile, apkConfigFile, shellConfigFile, allowlist)
    outputs.upToDateWhen { false }
    doLast {
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        val candidates: List<List<String>> = if (isWindows) {
            listOf(listOf("python3"), listOf("python"), listOf("py", "-3"))
        } else {
            listOf(listOf("python3"), listOf("python"))
        }
        val python3Cmd = candidates.firstOrNull { candidate ->
            try {
                val probe = ProcessBuilder(candidate + "--version").redirectErrorStream(true).start()
                probe.waitFor() == 0 &&
                    probe.inputStream.bufferedReader().readText().contains("Python 3")
            } catch (_: Exception) {
                false
            }
        } ?: listOf("python3")
        val pb = ProcessBuilder(python3Cmd + script.absolutePath)
        pb.directory(rootDir)
        pb.redirectErrorStream(true)
        val proc = pb.start()
        val log = proc.inputStream.bufferedReader().readText()
        val code = proc.waitFor()
        logger.lifecycle(log.trim())
        if (code != 0) {
            throw GradleException("checkConfigFieldDrift failed ($code)")
        }
    }
}

val releaseVersionInputs = listOf(
    rootProject.file("app/build.gradle.kts"),
    rootProject.file("shell/build.gradle.kts"),
    rootProject.file("metadata/com.webtoapp.yml"),
    rootProject.file(".github/CONTRIBUTING.md")
)
val releaseChangelogDir = rootProject.file("fastlane/metadata/android/en-US/changelogs")

tasks.withType<Test>().configureEach {
    inputs.files(releaseVersionInputs)
    inputs.dir(releaseChangelogDir)
}
