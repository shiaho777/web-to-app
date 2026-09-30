package com.webtoapp.core.engine

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import org.junit.After
import org.junit.Before
import org.junit.Test

class GeckoRuntimeProvisionerTest {

    private lateinit var dir: File
    private lateinit var omniJa: File

    @Before
    fun setUp() {
        dir = createTempDir("gecko-prov")
        omniJa = File(dir, "omni.ja")
    }

    @After
    fun tearDown() {
        dir.deleteRecursively()
    }

    @Test
    fun `container zip stores omni under assets path uncompressed`() {
        val payload = ByteArray(4096) { (it % 251).toByte() }
        omniJa.writeBytes(payload)

        val out = File(dir, "c.zip")
        GeckoRuntimeProvisioner.writeNestedOmniZip(out, omniJa)

        ZipFile(out).use { zip ->
            val entry = zip.getEntry("assets/omni.ja")
            assertThat(entry).isNotNull()
            assertThat(entry.method).isEqualTo(ZipEntry.STORED)
            assertThat(entry.compressedSize).isEqualTo(payload.size.toLong())
            assertThat(zip.getInputStream(entry).readBytes()).isEqualTo(payload)
        }
    }

    @Test
    fun `ensureOmniContainer returns null when omni missing or empty`() {
        assertThat(GeckoRuntimeProvisioner.ensureOmniContainer(omniJa)).isNull()

        omniJa.writeBytes(ByteArray(0))
        assertThat(GeckoRuntimeProvisioner.ensureOmniContainer(omniJa)).isNull()
    }

    @Test
    fun `ensureOmniContainer builds once and reuses stamp`() {
        omniJa.writeBytes(ByteArray(2048) { 7 })

        val first = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa)
        assertThat(first).isNotNull()
        val mtime = first!!.lastModified()

        val second = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa)
        assertThat(second).isEqualTo(first)
        assertThat(second!!.lastModified()).isEqualTo(mtime)
    }

    @Test
    fun `ensureOmniContainer rebuilds when omni changes`() {
        omniJa.writeBytes(ByteArray(1024) { 1 })
        val first = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa)
        assertThat(first).isNotNull()

        omniJa.writeBytes(ByteArray(2048) { 2 })
        val second = GeckoRuntimeProvisioner.ensureOmniContainer(omniJa)
        assertThat(second).isNotNull()
        ZipFile(second!!).use { zip ->
            val entry = zip.getEntry("assets/omni.ja")
            assertThat(entry.size).isEqualTo(2048L)
            assertThat(zip.getInputStream(entry).readBytes()).isEqualTo(ByteArray(2048) { 2 })
        }
    }
}
