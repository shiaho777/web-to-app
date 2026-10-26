package com.webtoapp.core.plugin

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.io.IOException
import org.junit.Test

class PluginPackageFilesTest {

    private fun tempRoot(): File =
        File(System.getProperty("java.io.tmpdir"), "wta-plugins-${System.nanoTime()}").also { it.mkdirs() }

    @Test
    fun `a failed publish leaves the live package in place`() {
        val root = tempRoot()
        try {
            val live = File(root, "dark").also { it.mkdirs() }
            File(live, "plugin.json").writeText("{\"name\":\"old\"}")
            val failed = runCatching {
                replacePluginPackageDir(root, "dark") {
                    File(it, "plugin.json").writeText("{\"name\":\"new\"}")
                    throw IOException("disk full")
                }
            }
            assertThat(failed.isFailure).isTrue()
            assertThat(File(live, "plugin.json").readText()).contains("old")
            assertThat(File(root, "dark.tmp").exists()).isFalse()
            assertThat(File(root, "dark.bak").exists()).isFalse()
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `publish swaps the new tree in and drops the backup`() {
        val root = tempRoot()
        try {
            val live = File(root, "dark").also { it.mkdirs() }
            File(live, "plugin.json").writeText("{\"name\":\"old\"}")
            replacePluginPackageDir(root, "dark") { dir ->
                File(dir, "plugin.html").writeText("<p>new</p>")
            }
            assertThat(File(root, "dark/plugin.html").readText()).isEqualTo("<p>new</p>")
            assertThat(File(root, "dark/plugin.json").exists()).isFalse()
            assertThat(File(root, "dark.bak").exists()).isFalse()
            assertThat(File(root, "dark.tmp").exists()).isFalse()
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `a leftover backup is restored when the live dir is missing`() {
        val root = tempRoot()
        try {
            val bak = File(root, "dark.bak").also { it.mkdirs() }
            File(bak, "plugin.json").writeText("{\"name\":\"kept\"}")
            File(root, "dark.tmp").mkdirs()
            recoverPluginPackageDirs(root)
            assertThat(File(root, "dark/plugin.json").readText()).contains("kept")
            assertThat(bak.exists()).isFalse()
            assertThat(File(root, "dark.tmp").exists()).isFalse()
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `a leftover backup is deleted when the live dir exists`() {
        val root = tempRoot()
        try {
            File(root, "dark").mkdirs()
            File(root, "dark/plugin.json").writeText("{\"name\":\"live\"}")
            File(root, "dark.bak").mkdirs()
            File(root, "dark.bak/plugin.json").writeText("{\"name\":\"stale\"}")
            recoverPluginPackageDirs(root)
            assertThat(File(root, "dark/plugin.json").readText()).contains("live")
            assertThat(File(root, "dark.bak").exists()).isFalse()
        } finally {
            root.deleteRecursively()
        }
    }
}
