package com.webtoapp.ui.webview

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Separate WebApp tasks must reuse the recents card for the same document.
 * FLAG_ACTIVITY_MULTIPLE_TASK, and documentLaunchMode="always", create a new
 * card on every open (#1250).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WebViewDocumentLaunchTest {

    @Test
    fun `same document reuses its recents task`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val uri = Uri.parse("webtoapp://webapp/7")
        val intent = WebViewActivity.buildLaunchIntent(
            context,
            separateTasks = true,
            documentUri = uri
        ) {}

        assertThat(intent.component?.className).contains("WebViewDocumentActivity")
        assertThat(intent.data).isEqualTo(uri)
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_DOCUMENT).isNotEqualTo(0)
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_MULTIPLE_TASK).isEqualTo(0)

        val manifest = repoFile("app/src/main/AndroidManifest.xml").readText()
        val document = manifest.substringAfter("WebViewDocumentActivity")
            .substringBefore("</activity>")
        assertThat(document).contains("android:documentLaunchMode=\"intoExisting\"")
        assertThat(document).doesNotContain("android:documentLaunchMode=\"always\"")
    }

    @Test
    fun `desktop shortcut always opens one document task for that app`() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = WebViewActivity.buildShortcutLaunchIntent(context, 42L)

        assertThat(intent.component?.className).isEqualTo(WebViewDocumentActivity::class.java.name)
        assertThat(intent.data).isEqualTo(Uri.parse("webtoapp://webapp/42"))
        assertThat(intent.getLongExtra("app_id", -1L)).isEqualTo(42L)
        assertThat(intent.getBooleanExtra(WebViewActivity.EXTRA_SHORTCUT_TASK, false)).isTrue()
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_DOCUMENT).isNotEqualTo(0)
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK).isNotEqualTo(0)
        assertThat(intent.flags and Intent.FLAG_ACTIVITY_MULTIPLE_TASK).isEqualTo(0)
        // The relaunch is already a document task, so the detach must not fire again.
        assertThat(
            WebViewActivity.shouldDetachExternalLaunch(
                isDocumentActivity = true,
                isTaskRoot = false,
                intent = intent,
            )
        ).isFalse()
    }

    @Test
    fun `old shortcut stacked on the builder detaches onto a document task`() {
        val stacked = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("app_id", 7L)
        }
        assertThat(
            WebViewActivity.shouldDetachExternalLaunch(
                isDocumentActivity = false,
                isTaskRoot = false,
                intent = stacked,
            )
        ).isTrue()
    }

    @Test
    fun `in-app preview and a task-root launch stay put`() {
        val inApp = Intent().apply { putExtra("app_id", 7L) }
        assertThat(
            WebViewActivity.shouldDetachExternalLaunch(
                isDocumentActivity = false,
                isTaskRoot = false,
                intent = inApp,
            )
        ).isFalse()

        val coldStart = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("app_id", 7L)
        }
        assertThat(
            WebViewActivity.shouldDetachExternalLaunch(
                isDocumentActivity = false,
                isTaskRoot = true,
                intent = coldStart,
            )
        ).isFalse()

        val preview = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("app_id", 7L)
            putExtra("preview_app_json", "{}")
        }
        assertThat(
            WebViewActivity.shouldDetachExternalLaunch(
                isDocumentActivity = false,
                isTaskRoot = false,
                intent = preview,
            )
        ).isFalse()

        val noApp = Intent().apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        assertThat(
            WebViewActivity.shouldDetachExternalLaunch(
                isDocumentActivity = false,
                isTaskRoot = false,
                intent = noApp,
            )
        ).isFalse()
    }

    private fun repoFile(relativePath: String): File {
        val roots = listOf(File("."), File(".."))
        return roots.asSequence()
            .map { File(it, relativePath) }
            .firstOrNull { it.isFile }
            ?: error("Cannot locate $relativePath")
    }
}
