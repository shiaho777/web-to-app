package com.webtoapp.ui.shared

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = Application::class)
class GalleryWatchHistoryTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `playback follows the video ID and nested folder after the listing changes`() {
        val history = GalleryWatchHistory(context, "preview/42/device/Documents")
        val watched = GalleryWatchEntry("video-lesson-2", "Algebra lesson", 1_122_000, listOf("Courses", "Algebra"))
        history.checkpoint(watched)
        assertThat(history.latestEntry.value).isEqualTo(watched)
        val reordered = listOf("video-new", "video-lesson-1", "video-lesson-2")
        val reopened = GalleryWatchHistory(context, "preview/42/device/Documents")

        assertThat(reopened.latestEntry.value).isEqualTo(watched)
        assertThat(galleryResumePosition(reopened.position(reordered[2]), 1_800_000)).isEqualTo(1_122_000)
        assertThat(reopened.position(reordered[0])).isEqualTo(0)
        assertThat(galleryWatchTime(reopened.position(reordered[2]))).isEqualTo("18:42")
        assertThat(GalleryWatchHistory(context, "preview/42/device/Movies").latestEntry.value).isNull()
    }

    @Test
    fun `completion clears the latest entry and preserves another video's saved position`() {
        val history = GalleryWatchHistory(context, "shell/bundled")
        val first = GalleryWatchEntry("first", "First", 22_000, emptyList())
        val second = GalleryWatchEntry("second", "Second", 38_000, emptyList())
        history.checkpoint(first)
        history.checkpoint(second)
        history.complete(second.videoId)

        assertThat(history.latestEntry.value).isNull()
        assertThat(history.position(second.videoId)).isEqualTo(0)
        assertThat(history.position(first.videoId)).isEqualTo(22_000)
        assertThat(galleryResumePosition(22_000, 15_000)).isEqualTo(0)
        assertThat(galleryMatchesSearch("Lesson Algebra.mp4", "  algebra ")).isTrue()
    }
}
