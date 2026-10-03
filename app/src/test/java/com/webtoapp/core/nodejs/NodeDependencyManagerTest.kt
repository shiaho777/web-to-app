package com.webtoapp.core.nodejs

import android.content.Context
import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.Locale
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NodeDependencyManagerTest {

    @Rule @JvmField
    val koinRule = com.webtoapp.util.KoinCleanupRule()

    private lateinit var context: Context
    private var originalLocale: Locale = Locale.getDefault()

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        originalLocale = Locale.getDefault()
        NodeDependencyManager.setMirrorRegion(null)
        NodeDependencyManager.clearCache(context)
    }

    @After
    fun tearDown() {
        NodeDependencyManager.setMirrorRegion(null)
        Locale.setDefault(originalLocale)
        NodeDependencyManager.clearCache(context)
    }

    @Test
    fun `manual mirror region selection takes precedence`() {
        NodeDependencyManager.setMirrorRegion(NodeDependencyManager.MirrorRegion.CN)
        val cnConfig = NodeDependencyManager.getMirrorConfig()
        assertThat(NodeDependencyManager.getMirrorRegion()).isEqualTo(NodeDependencyManager.MirrorRegion.CN)
        assertThat(cnConfig.nodeUrls.size).isGreaterThan(1)

        NodeDependencyManager.setMirrorRegion(NodeDependencyManager.MirrorRegion.GLOBAL)
        val globalConfig = NodeDependencyManager.getMirrorConfig()
        assertThat(NodeDependencyManager.getMirrorRegion()).isEqualTo(NodeDependencyManager.MirrorRegion.GLOBAL)
        assertThat(globalConfig.nodeUrls).hasSize(1)
    }

    @Test
    fun `auto mirror region follows default locale language`() {
        NodeDependencyManager.setMirrorRegion(null)

        Locale.setDefault(Locale.CHINESE)
        assertThat(NodeDependencyManager.getMirrorRegion()).isEqualTo(NodeDependencyManager.MirrorRegion.CN)

        Locale.setDefault(Locale.ENGLISH)
        assertThat(NodeDependencyManager.getMirrorRegion()).isEqualTo(NodeDependencyManager.MirrorRegion.GLOBAL)
    }

    @Test
    fun `node directory helpers create expected paths`() {
        val depsDir = NodeDependencyManager.getDepsDir(context)
        val nodeDir = NodeDependencyManager.getNodeDir(context)
        val projectDir = NodeDependencyManager.getNodeProjectsDir(context)

        assertThat(depsDir.exists()).isTrue()
        assertThat(nodeDir.exists()).isTrue()
        assertThat(projectDir.exists()).isTrue()
        assertThat(nodeDir.absolutePath).contains("/nodejs_deps/node/")
    }

    @Test
    fun `getNodeLibraryPath returns null when runtime is not ready`() {
        val path = NodeDependencyManager.getNodeLibraryPath(context)

        assertThat(path).isNull()
        assertThat(NodeDependencyManager.isNodeReady(context)).isFalse()
    }

    @Test
    fun `node runtime becomes ready when libnode_so is in nativeLibraryDir`() {

        val nativeLibDir = File(context.cacheDir, "test-native-lib").apply {
            deleteRecursively()
            mkdirs()
        }
        context.applicationInfo.nativeLibraryDir = nativeLibDir.absolutePath
        val nativeLib = File(nativeLibDir, NodeDependencyManager.NODE_BINARY_NAME).apply {
            writeBytes(byteArrayOf(1))
        }

        try {
            assertThat(NodeDependencyManager.isNodeReady(context)).isTrue()
            assertThat(NodeDependencyManager.getNodeLibraryPath(context)).isEqualTo(nativeLib.absolutePath)
        } finally {
            nativeLibDir.deleteRecursively()
        }
    }

    @Test
    fun `node downloaded into deps dir is treated as ready when nativeLibraryDir is empty`() {

        val nativeLibDir = File(context.cacheDir, "test-native-lib-empty").apply {
            deleteRecursively()
            mkdirs()
        }
        context.applicationInfo.nativeLibraryDir = nativeLibDir.absolutePath

        val downloaded = File(NodeDependencyManager.getNodeDir(context), NodeDependencyManager.NODE_BINARY_NAME).apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1))
        }

        try {
            assertThat(downloaded.exists()).isTrue()
            assertThat(NodeDependencyManager.isNodeReady(context)).isTrue()
        } finally {
            nativeLibDir.deleteRecursively()
        }
    }

    @Test
    fun `nodeAbiOfEntry matches whole path segments and never confuses x86 with x86_64`() {
        assertThat(
            NodeDependencyManager.nodeAbiOfEntry("nodejs-mobile-v18.20.4-android/bin/arm64-v8a/libnode.so")
        ).isEqualTo("arm64-v8a")
        assertThat(NodeDependencyManager.nodeAbiOfEntry("bin/x86_64/libnode.so")).isEqualTo("x86_64")
        assertThat(NodeDependencyManager.nodeAbiOfEntry("bin/x86/libnode.so")).isEqualTo("x86")
        assertThat(NodeDependencyManager.nodeAbiOfEntry("bin/armeabi-v7a/libnode.so")).isEqualTo("armeabi-v7a")
        assertThat(NodeDependencyManager.nodeAbiOfEntry("x86_64/libnode.so")).isEqualTo("x86_64")
        assertThat(NodeDependencyManager.nodeAbiOfEntry("include/node/node_api.h")).isNull()
        assertThat(NodeDependencyManager.nodeAbiOfEntry("libnode.so")).isNull()
    }

    @Test
    fun `missingExportAbis reports only satisfiable abis lacking a cached libnode`() {
        // Empty cache: every upstream ABI is missing; x86 is never reported because
        // upstream ships no 32-bit x86 libnode and the gap can never be fixed.
        assertThat(
            NodeDependencyManager.missingExportAbis(context, listOf("arm64-v8a", "x86_64", "x86"))
        ).containsExactly("arm64-v8a", "x86_64")

        // A cached libnode.so satisfies that ABI.
        File(
            NodeDependencyManager.getNodeDir(context, "x86_64"),
            NodeDependencyManager.NODE_BINARY_NAME
        ).apply {
            parentFile?.mkdirs()
            writeBytes(byteArrayOf(1))
        }

        assertThat(NodeDependencyManager.missingExportAbis(context, listOf("x86_64"))).isEmpty()
        assertThat(
            NodeDependencyManager.missingExportAbis(context, listOf("arm64-v8a", "x86_64"))
        ).containsExactly("arm64-v8a")
    }
}
