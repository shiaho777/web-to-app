package com.webtoapp.core.wordpress

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WordPressPhpRuntimeTest {

    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        WordPressDependencyManager.getDepsDir(context).deleteRecursively()
    }

    @Test
    fun `wordpress runtime launches the native cli-server sapi with native headers intact`() {
        val runtime = WordPressPhpRuntime(context)
        val method = WordPressPhpRuntime::class.java.getDeclaredMethod(
            "buildPhpCommand",
            List::class.java,
            Int::class.javaPrimitiveType,
            String::class.java,
            String::class.java,
            List::class.java,
            File::class.java
        )
        method.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val command = method.invoke(
            runtime,
            listOf("/tmp/php"),
            18500,
            context.filesDir.absolutePath,
            File(context.cacheDir, "php_router_server.php").absolutePath,
            emptyList<com.webtoapp.data.model.CustomPhpExtension>(),
            null
        ) as List<String>

        val router = File(context.cacheDir, "php_router_server.php").absolutePath
        assertThat(command.none { it.startsWith("disable_functions=") }).isTrue()
        assertThat(command.takeLast(5))
            .containsExactly("-S", "127.0.0.1:18500", "-t", context.filesDir.absolutePath, router)
            .inOrder()
    }

    @Test
    fun `php ready becomes true when nativeLibraryDir libphp_so is present and executable`() {

        val tempNativeDir = File(context.cacheDir, "test-native-lib").apply {
            deleteRecursively()
            mkdirs()
        }
        context.applicationInfo.nativeLibraryDir = tempNativeDir.absolutePath
        val nativePhp = File(tempNativeDir, "libphp.so").apply {

            writeBytes(ByteArray(1024))
            setExecutable(true, false)
        }

        try {
            assertThat(WordPressDependencyManager.isPhpReady(context)).isTrue()
            assertThat(WordPressDependencyManager.getPhpExecutablePath(context))
                .isEqualTo(nativePhp.absolutePath)
        } finally {
            tempNativeDir.deleteRecursively()
        }
    }

    @Test
    fun `php downloaded into app data dir is treated as ready when nativeLib is empty and runtime is current`() {

        val emptyNativeDir = File(context.cacheDir, "test-native-lib-empty").apply {
            deleteRecursively()
            mkdirs()
        }
        context.applicationInfo.nativeLibraryDir = emptyNativeDir.absolutePath

        val downloadedPhp = File(WordPressDependencyManager.getPhpDir(context), "bin/php").apply {
            parentFile?.mkdirs()

            writeBytes(ByteArray(1024 * 1024 + 1) { 0 })
            setExecutable(true, false)
        }
        File(WordPressDependencyManager.getPhpDir(context), ".runtime_sha256")
            .writeText(WordPressDependencyManager.PHP_TARBALL_SHA256)

        try {
            assertThat(downloadedPhp.canExecute()).isTrue()
            assertThat(WordPressDependencyManager.isPhpReady(context)).isTrue()
            assertThat(WordPressDependencyManager.getPhpExecutablePath(context))
                .isEqualTo(downloadedPhp.absolutePath)
        } finally {
            emptyNativeDir.deleteRecursively()
        }
    }

    @Test
    fun `previously downloaded php from another runtime build is not ready`() {
        val emptyNativeDir = File(context.cacheDir, "test-native-lib-stale").apply {
            deleteRecursively()
            mkdirs()
        }
        context.applicationInfo.nativeLibraryDir = emptyNativeDir.absolutePath
        val phpDir = WordPressDependencyManager.getPhpDir(context)
        File(phpDir, "php").apply {
            writeBytes(ByteArray(1024 * 1024 + 1) { 0 })
            setExecutable(true, false)
        }

        try {
            assertThat(WordPressDependencyManager.isPhpReady(context)).isFalse()
            File(phpDir, ".runtime_sha256").writeText("d8867966340121f821591b9bb29c80a58ad77abd6cc8e0e44d1cbbb3aaedd70c")
            assertThat(WordPressDependencyManager.isPhpReady(context)).isFalse()
        } finally {
            emptyNativeDir.deleteRecursively()
        }
    }

    @Test
    fun `php is not ready when neither nativeLib nor downloaded binary exists`() {
        val emptyNativeDir = File(context.cacheDir, "test-native-lib-none").apply {
            deleteRecursively()
            mkdirs()
        }
        context.applicationInfo.nativeLibraryDir = emptyNativeDir.absolutePath
        WordPressDependencyManager.getPhpDir(context).deleteRecursively()

        try {
            assertThat(WordPressDependencyManager.isPhpReady(context)).isFalse()
        } finally {
            emptyNativeDir.deleteRecursively()
        }
    }
}
