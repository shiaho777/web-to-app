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
    fun `wordpress runtime disables native headers so router polyfill captures redirects and cookies`() {
        val runtime = WordPressPhpRuntime(context)
        val method = WordPressPhpRuntime::class.java.getDeclaredMethod(
            "buildPhpCommand",
            List::class.java,
            Int::class.javaPrimitiveType,
            String::class.java,
            String::class.java
        )
        method.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val command = method.invoke(
            runtime,
            listOf("/tmp/php"),
            18500,
            context.filesDir.absolutePath,
            File(context.cacheDir, "php_router_server.php").absolutePath
        ) as List<String>

        assertThat(command).contains("disable_functions=header,headers_list,headers_sent,header_remove,setcookie,setrawcookie")
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
    fun `php downloaded into app data dir is treated as ready when nativeLib is empty`() {

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
