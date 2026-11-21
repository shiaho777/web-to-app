package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeckoRuntimeProvisionerTest {

    private lateinit var dir: File
    private lateinit var omniJa: File
    private lateinit var hostApk: File
    private val bridgeManifestPath = "assets/web_extensions/wta_native_bridge/manifest.json"

    @Before
    fun setUp() {
        dir = createTempDir("gecko-prov")
        omniJa = File(dir, "omni.ja")
        hostApk = File(dir, "base.apk")
        writeHostApk("bridge-v1")
    }

    @After
    fun tearDown() {
        dir.deleteRecursively()
    }

    @Test
    fun `container stores uncompressed omni and built-in extension assets`() {
        val payload = ByteArray(4096) { (it % 251).toByte() }
        omniJa.writeBytes(payload)

        val out = checkNotNull(GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk))

        ZipFile(out).use { zip ->
            val entry = zip.getEntry("assets/omni.ja")
            assertThat(entry).isNotNull()
            assertThat(entry.method).isEqualTo(ZipEntry.STORED)
            assertThat(entry.compressedSize).isEqualTo(payload.size.toLong())
            assertThat(zip.getInputStream(entry).use { it.readBytes() }).isEqualTo(payload)
            assertThat(zip.getInputStream(zip.getEntry(bridgeManifestPath)).bufferedReader().use { it.readText() })
                .isEqualTo("bridge-v1")
        }
    }

    @Test
    fun `ensureOmniContainer returns null when omni missing or empty`() {
        assertThat(GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk)).isNull()

        omniJa.writeBytes(ByteArray(0))
        assertThat(GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk)).isNull()
    }

    @Test
    fun `ensureOmniContainer builds once and reuses stamp`() {
        omniJa.writeBytes(ByteArray(2048) { 7 })

        val first = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk)
        assertThat(first).isNotNull()
        val mtime = first!!.lastModified()

        val second = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk)
        assertThat(second).isEqualTo(first)
        assertThat(second!!.lastModified()).isEqualTo(mtime)
    }

    @Test
    fun `ensureOmniContainer rebuilds when omni changes`() {
        omniJa.writeBytes(ByteArray(1024) { 1 })
        val first = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk)
        assertThat(first).isNotNull()

        omniJa.writeBytes(ByteArray(2048) { 2 })
        val second = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk)
        assertThat(second).isNotNull()
        ZipFile(second!!).use { zip ->
            val entry = zip.getEntry("assets/omni.ja")
            assertThat(entry.size).isEqualTo(2048L)
            assertThat(zip.getInputStream(entry).use { it.readBytes() }).isEqualTo(ByteArray(2048) { 2 })
        }
    }

    @Test
    fun `container refreshes extension assets when the host APK changes`() {
        omniJa.writeBytes(byteArrayOf(1))
        assertThat(GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk)).isNotNull()

        writeHostApk("updated-bridge-manifest")

        val rebuilt = checkNotNull(GeckoRuntimeProvisioner.ensureOmniContainer(omniJa, hostApk))
        ZipFile(rebuilt).use { zip ->
            assertThat(zip.getInputStream(zip.getEntry(bridgeManifestPath)).bufferedReader().use { it.readText() })
                .isEqualTo("updated-bridge-manifest")
        }
    }

    private fun writeHostApk(manifest: String) {
        ZipOutputStream(hostApk.outputStream()).use { zip ->
            zip.putNextEntry(ZipEntry(bridgeManifestPath))
            zip.write(manifest.toByteArray())
            zip.closeEntry()
        }
    }
}
