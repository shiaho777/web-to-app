package com.webtoapp.core.linux

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.reflect.Method
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LocalBuildEnvironmentTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        LocalBuildEnvironment.getRootDir(context).deleteRecursively()
    }

    @Test
    fun `cli paths point to unpacked package locations`() {
        assertThat(LocalBuildEnvironment.getNpmCliPath(context).path).endsWith("npm/package/bin/npm-cli.js")
        assertThat(LocalBuildEnvironment.getPnpmCliPath(context).path).endsWith("pnpm/package/bin/pnpm.cjs")
        assertThat(LocalBuildEnvironment.getYarnCliPath(context).path).endsWith("yarn/package/bin/yarn.js")
    }

    @Test
    fun `composer in china uses the versioned phar mirror with an official fallback`() {
        val official = "https://getcomposer.org/download/2.10.2/composer.phar"
        val github = "https://github.com/composer/composer/releases/download/2.10.2/composer.phar"
        val china = LocalBuildEnvironment.composerPharUrls(preferChinaMirror = true)
        assertThat(china.take(2)).containsExactly(
            "https://mirror.nju.edu.cn/freebsd-ports/distfiles/composer-2.10.2/composer.phar",
            official
        ).inOrder()
        assertThat(china.last()).isEqualTo(github)
        assertThat(LocalBuildEnvironment.composerPharUrls(preferChinaMirror = false))
            .containsExactly(official, github).inOrder()
    }

    @Test
    fun `composer probe surfaces stderr on php failure even when stdout mentions the pinned version`() {
        val error = "PHP Fatal error: unable to read phar"
        val result = ExecutionResult(255, "Composer version 2.10.2", error, 0)

        val message = LocalBuildEnvironment.composerRuntimeError(result)

        assertThat(message).contains("php composer.phar --version [255]")
        assertThat(message).contains(error)
    }

    @Test
    fun `composer probe accepts only an exact version line from a successful process`() {
        val version = "Composer version 2.10.2 2026-07-01"
        assertThat(LocalBuildEnvironment.composerRuntimeError(ExecutionResult(0, "$version\nPHP version 8.4.16", "", 0))).isNull()
        assertThat(LocalBuildEnvironment.composerRuntimeError(ExecutionResult(0, "", version, 0))).isNull()
        assertThat(LocalBuildEnvironment.composerRuntimeError(ExecutionResult(0, "Composer version 2.10.20", "", 0)))
            .contains("Composer 2.10.20")
        assertThat(LocalBuildEnvironment.composerRuntimeError(ExecutionResult(0, "Warning: expected $version", "", 0)))
            .contains("php composer.phar --version")
    }

    @Test
    fun `composer version uses the same php flags and environment as dependency installation`() = runBlocking {
        val appInfo = context.applicationInfo
        val previousLibDir = appInfo.nativeLibraryDir
        val previousTarget = appInfo.targetSdkVersion
        val fakeNativeDir = File(context.cacheDir, "test-composer-native").apply { mkdirs() }
        val composer = LocalBuildEnvironment.getComposerPharPath(context)
        composer.parentFile?.mkdirs()
        composer.writeText("fixture")
        File(composer.parentFile, ".installed-version").writeText("2.10.2")
        File(fakeNativeDir, "libphp.so").apply {
            writeText(
                """
                #!/bin/sh
                test -n "${'$'}COMPOSER_HOME" || exit 1
                for arg in "${'$'}@"; do
                    case "${'$'}arg" in
                        pcre.jit=0) jit_disabled=1 ;;
                        --no-ansi) no_ansi=1 ;;
                    esac
                done
                test "${'$'}jit_disabled:${'$'}no_ansi" = "1:1" || exit 2
                printf '%s\n' 'Composer version 2.10.2'
                """.trimIndent()
            )
            setExecutable(true)
        }
        appInfo.nativeLibraryDir = fakeNativeDir.absolutePath
        appInfo.targetSdkVersion = 28
        try {
            assertThat(LocalBuildEnvironment.detectToolVersion(context, BuildTool.COMPOSER)).isEqualTo("2.10.2")
        } finally {
            appInfo.targetSdkVersion = previousTarget
            appInfo.nativeLibraryDir = previousLibDir
            fakeNativeDir.deleteRecursively()
        }
    }

    @Test
    fun `parse package script returns named script`() {
        val projectDir = File(context.cacheDir, "pkg-${System.nanoTime()}").apply { mkdirs() }
        val packageJson = File(projectDir, "package.json").apply {
            writeText(
                """
                {
                  "scripts": {
                    "build": "vite build",
                    "dev": "vite"
                  }
                }
                """.trimIndent()
            )
        }

        val method: Method = LocalBuildEnvironment::class.java.getDeclaredMethod(
            "parsePackageScript",
            File::class.java,
            String::class.java
        )
        method.isAccessible = true
        val buildScript = method.invoke(LocalBuildEnvironment, packageJson, "build") as String?
        val missingScript = method.invoke(LocalBuildEnvironment, packageJson, "lint") as String?

        assertThat(buildScript).isEqualTo("vite build")
        assertThat(missingScript).isNull()
    }
}
