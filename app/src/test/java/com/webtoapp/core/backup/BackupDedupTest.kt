package com.webtoapp.core.backup

import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/**
 * Re-importing a backup must not duplicate apps. Identity: remote apps dedup on
 * type+name+normalized URL; local-content apps (paths rewritten across export)
 * dedup on type+name.
 */
@RunWith(RobolectricTestRunner::class)
class BackupDedupTest {

    private val manager by lazy {
        DataBackupManager(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `same site different case or trailing slash dedups`() {
        val a = WebApp(name = "Docs", url = "https://example.com/")
        val b = WebApp(name = "docs ", url = "HTTPS://EXAMPLE.COM")
        assertThat(manager.backupDedupKey(a)).isEqualTo(manager.backupDedupKey(b))
    }

    @Test
    fun `different urls do not dedup`() {
        val a = WebApp(name = "Docs", url = "https://a.example.com")
        val b = WebApp(name = "Docs", url = "https://b.example.com")
        assertThat(manager.backupDedupKey(a)).isNotEqualTo(manager.backupDedupKey(b))
    }

    @Test
    fun `local path apps dedup on type and name`() {
        // Export rewrites media urls to resources/... — paths can't be the key.
        val original = WebApp(
            name = "My Video",
            url = "/data/files/video.mp4",
            appType = AppType.VIDEO
        )
        val reimported = original.copy(url = "resources/media/1_media.mp4")
        assertThat(manager.backupDedupKey(original))
            .isEqualTo(manager.backupDedupKey(reimported))
    }

    @Test
    fun `same name different type does not dedup`() {
        val a = WebApp(name = "Thing", url = "", appType = AppType.HTML)
        val b = WebApp(name = "Thing", url = "", appType = AppType.WEB)
        assertThat(manager.backupDedupKey(a)).isNotEqualTo(manager.backupDedupKey(b))
    }
}
