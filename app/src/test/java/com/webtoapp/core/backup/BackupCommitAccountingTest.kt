package com.webtoapp.core.backup

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

/**
 * commitPendingLocalEntries reports whether anything was actually committed,
 * so the UI restarts the app only when restored files need it.
 */
@RunWith(RobolectricTestRunner::class)
class BackupCommitAccountingTest {

    private val manager by lazy {
        DataBackupManager(RuntimeEnvironment.getApplication())
    }

    private fun stagedEntry(name: String, content: String): DataBackupManager.PendingLocalEntry {
        val target = File(RuntimeEnvironment.getApplication().filesDir, "commit_probe/$name")
        val temp = File.createTempFile("wta_commit_", ".part", RuntimeEnvironment.getApplication().cacheDir)
        temp.writeText(content)
        return DataBackupManager.PendingLocalEntry(
            zipPath = "local/files/commit_probe/$name",
            targetFile = target,
            tempFile = temp
        )
    }

    @Test
    fun `empty pending list reports nothing committed`() {
        assertThat(manager.commitPendingLocalEntries(emptyList())).isFalse()
    }

    @Test
    fun `successful commit reports true and lands the file`() {
        val committed = manager.commitPendingLocalEntries(
            listOf(stagedEntry("a.txt", "hello"))
        )
        assertThat(committed).isTrue()
        val landed = File(RuntimeEnvironment.getApplication().filesDir, "commit_probe/a.txt")
        try {
            assertThat(landed.readText()).isEqualTo("hello")
        } finally {
            File(RuntimeEnvironment.getApplication().filesDir, "commit_probe").deleteRecursively()
        }
    }

    @Test
    fun `failed commit reports false`() {
        // Target inside a regular file: mkdirs + copy cannot succeed.
        val blocker = File(RuntimeEnvironment.getApplication().filesDir, "commit_blocker")
        try {
            blocker.writeText("x")
            val temp = File.createTempFile("wta_commit_", ".part", RuntimeEnvironment.getApplication().cacheDir)
            temp.writeText("y")
            val entry = DataBackupManager.PendingLocalEntry(
                zipPath = "local/files/commit_blocker/child.txt",
                targetFile = File(blocker, "child.txt"),
                tempFile = temp
            )
            assertThat(manager.commitPendingLocalEntries(listOf(entry))).isFalse()
        } finally {
            blocker.delete()
        }
    }
}
