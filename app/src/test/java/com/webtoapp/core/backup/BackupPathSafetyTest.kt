package com.webtoapp.core.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Zip-slip guards for backup restore. A crafted backup must never write outside
 * its target directory — notably the shared_prefs prefix branch, which used to
 * accept names like "gm_storage_/../evil.xml" (prefix + suffix checks pass,
 * File() does not normalize ..).
 */
@RunWith(RobolectricTestRunner::class)
class BackupPathSafetyTest {

    private val context = RuntimeEnvironment.getApplication()
    private val manager by lazy { DataBackupManager(context) }

    private fun stagedEntryStream(content: ByteArray = "x".toByteArray()): ZipInputStream {
        val buf = ByteArrayOutputStream()
        ZipOutputStream(buf).use { zip ->
            zip.putNextEntry(ZipEntry("payload"))
            zip.write(content)
            zip.closeEntry()
        }
        return ZipInputStream(ByteArrayInputStream(buf.toByteArray())).also {
            check(it.nextEntry != null)
        }
    }

    @Test
    fun `resolveSafeChild allows normal nested paths`() {
        val base = File(context.filesDir, "t1")
        val out = manager.resolveSafeChild(base, "a/b/c.txt")
        assertThat(out?.absolutePath?.startsWith(base.canonicalPath)).isTrue()
    }

    @Test
    fun `resolveSafeChild blocks parent escape`() {
        val base = File(context.filesDir, "t2")
        assertThat(manager.resolveSafeChild(base, "../evil.xml")).isNull()
        assertThat(manager.resolveSafeChild(base, "a/../../evil.xml")).isNull()
    }

    @Test
    fun `resolveSafeChild blocks absolute paths and sibling prefixes`() {
        val base = File(context.filesDir, "shared_prefs")
        assertThat(manager.resolveSafeChild(base, "/etc/passwd")).isNull()
        assertThat(
            manager.resolveSafeChild(File(context.filesDir, "x"), "../shared_prefs_evil/y.xml")
        ).isNull()
    }

    @Test
    fun `shared prefs prefix traversal is rejected`() {
        // Shallow .. stays inside but is still rejected (no legit entry has ..).
        assertThat(
            manager.stageLocalBackupEntry(
                "local/shared_prefs/gm_storage_/../evil.xml",
                stagedEntryStream()
            )
        ).isNull()
        // Deep .. escapes shared_prefs: the original zip-slip hole.
        assertThat(
            manager.stageLocalBackupEntry(
                "local/shared_prefs/gm_storage_/../../../../evil.xml",
                stagedEntryStream()
            )
        ).isNull()
    }

    @Test
    fun `shared prefs legit entries stage under shared_prefs`() {
        val staged = manager.stageLocalBackupEntry(
            "local/shared_prefs/gm_storage_script1.xml",
            stagedEntryStream()
        )
        assertThat(staged).isNotNull()
        val target = staged!!.targetFile.canonicalPath
        val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs").canonicalPath
        assertThat(target.startsWith("$prefsDir${File.separator}")).isTrue()
        staged.tempFile.delete()
    }

    @Test
    fun `keystore and adblock branches accept exact names only`() {
        val ks = manager.stageLocalBackupEntry(
            "local/keystores/webtoapp_keystore.p12",
            stagedEntryStream()
        )
        assertThat(ks).isNotNull()
        assertThat(ks!!.targetFile.name).isEqualTo("webtoapp_keystore.p12")
        ks.tempFile.delete()

        assertThat(
            manager.stageLocalBackupEntry("local/keystores/../evil.p12", stagedEntryStream())
        ).isNull()
        assertThat(
            manager.stageLocalBackupEntry("local/keystores/other.p12", stagedEntryStream())
        ).isNull()

        val ad = manager.stageLocalBackupEntry(
            "local/adblock/adblock_hosts.txt",
            stagedEntryStream()
        )
        assertThat(ad).isNotNull()
        ad!!.tempFile.delete()
        assertThat(
            manager.stageLocalBackupEntry("local/adblock/evil.txt", stagedEntryStream())
        ).isNull()
    }

    @Test
    fun `datastore branch accepts exact names only`() {
        val staged = manager.stageLocalBackupEntry(
            "local/datastore/theme_settings.preferences_pb",
            stagedEntryStream()
        )
        assertThat(staged).isNotNull()
        staged!!.tempFile.delete()
        assertThat(
            manager.stageLocalBackupEntry("local/datastore/evil.preferences_pb", stagedEntryStream())
        ).isNull()
    }
}
