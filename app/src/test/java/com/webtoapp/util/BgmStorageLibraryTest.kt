package com.webtoapp.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.BgmItem
import com.webtoapp.data.model.BgmTag
import com.webtoapp.data.model.LrcData
import com.webtoapp.data.model.LrcLine
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Regression coverage for the BGM library: the scanner used to list .mp3 files only,
 * so online-downloaded .m4a/.flac/... tracks were saved but never shown in the
 * selector, and lyrics/tag edits lived only in memory (or inside a single app's
 * config) because nothing persisted them to disk.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class BgmStorageLibraryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        BgmStorage.getBgmDir(context).deleteRecursively()
    }

    private fun writeAudio(fileName: String): File {
        return File(BgmStorage.getBgmDir(context), fileName)
            .apply { writeBytes(ByteArray(16) { it.toByte() }) }
    }

    @Test
    fun `scanUserBgm lists every supported audio extension`() {
        writeAudio("song.mp3")
        writeAudio("song.m4a")
        writeAudio("track.flac")
        writeAudio("voice.ogg")

        val scanned = BgmStorage.scanUserBgm(context)

        assertThat(scanned.map { File(it.path).name }.toSet())
            .containsExactly("song.mp3", "song.m4a", "track.flac", "voice.ogg")
    }

    @Test
    fun `saved lyrics sidecar round-trips through the scan`() {
        val audio = writeAudio("with_lrc.mp3")
        val lrc = LrcData(
            title = "Title",
            artist = "Artist",
            lines = listOf(LrcLine(startTime = 61230L, endTime = 70000L, text = "hello"))
        )

        assertThat(BgmStorage.saveLrc(context, audio.absolutePath, lrc)).isTrue()

        val scanned = BgmStorage.scanUserBgm(context).single()
        assertThat(scanned.lrcData?.title).isEqualTo("Title")
        assertThat(scanned.lrcData?.artist).isEqualTo("Artist")
        assertThat(scanned.lrcData?.lines?.single()?.text).isEqualTo("hello")
        assertThat(scanned.lrcData?.lines?.single()?.startTime).isEqualTo(61230L)
    }

    @Test
    fun `tag overrides persist across scans`() {
        val audio = writeAudio("tagged.m4a")
        val item = BgmItem(name = "tagged", path = audio.absolutePath, isAsset = false)

        BgmStorage.saveTagsForBgm(context, item, listOf(BgmTag.POP, BgmTag.ROCK))

        val scanned = BgmStorage.scanUserBgm(context).single()
        assertThat(scanned.tags).containsExactly(BgmTag.POP, BgmTag.ROCK).inOrder()
    }

    @Test
    fun `clearing tags persists the removal`() {
        val audio = writeAudio("untagged.mp3")
        val item = BgmItem(name = "untagged", path = audio.absolutePath, isAsset = false)
        BgmStorage.saveTagsForBgm(context, item, listOf(BgmTag.POP))

        BgmStorage.saveTagsForBgm(context, item, emptyList())

        assertThat(BgmStorage.scanUserBgm(context).single().tags).isEmpty()
    }

    @Test
    fun `deleteBgm removes audio cover and lyrics sidecar`() {
        val audio = writeAudio("gone.mp3")
        val cover = File(BgmStorage.getBgmDir(context), "gone.jpg").apply { writeBytes(ByteArray(4)) }
        val item = BgmItem(
            name = "gone",
            path = audio.absolutePath,
            coverPath = cover.absolutePath,
            isAsset = false
        )
        BgmStorage.saveLrc(
            context,
            audio.absolutePath,
            LrcData(lines = listOf(LrcLine(startTime = 0L, endTime = 1L, text = "x")))
        )

        assertThat(BgmStorage.deleteBgm(context, item)).isTrue()

        assertThat(audio.exists()).isFalse()
        assertThat(cover.exists()).isFalse()
        assertThat(BgmStorage.hasLrc(context, audio.absolutePath)).isFalse()
    }
}
