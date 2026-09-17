package com.webtoapp.core.share

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Covers the inbound share inbox (issue #943).
 *
 * The two things that must never regress here are the ones that make an untrusted third party
 * safe to accept data from: the mime filter (anything outside the app's declared filters is
 * dropped) and the filename sanitiser (a sender-supplied name must never escape the inbox
 * directory). Both are exercised directly, plus a round trip through [SharedContentInbox]
 * with a shadow [android.content.ContentResolver].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SharedContentInboxTest {

    private lateinit var context: Context

    private val imageUri: Uri = Uri.parse("content://media/external/images/media/42")

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        File(context.cacheDir, ShareReceiveContract.INBOX_DIR_NAME).deleteRecursively()
    }

    private fun registerImage(bytes: ByteArray = "fake-png-bytes".toByteArray()) {
        shadowOf(context.contentResolver)
            .registerInputStream(imageUri, ByteArrayInputStream(bytes))
    }

    private fun sendImageIntent(declaredType: String = "image/png"): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = declaredType
            putExtra(Intent.EXTRA_STREAM, imageUri)
        }

    // ── mime filter ─────────────────────────────────────────────────────────────

    @Test
    fun `mime filter accepts every subtype of a declared wildcard family`() {
        val allowed = listOf(ShareReceiveContract.MIME_IMAGES)

        assertThat(SharedContentInbox.mimeAllowed(allowed, "image/png")).isTrue()
        assertThat(SharedContentInbox.mimeAllowed(allowed, "image/jpeg")).isTrue()
        assertThat(SharedContentInbox.mimeAllowed(allowed, "IMAGE/PNG")).isTrue()
    }

    @Test
    fun `mime filter rejects anything outside the declared filters`() {
        val allowed = listOf(ShareReceiveContract.MIME_IMAGES)

        assertThat(SharedContentInbox.mimeAllowed(allowed, "application/pdf")).isFalse()
        assertThat(SharedContentInbox.mimeAllowed(allowed, "video/mp4")).isFalse()
        assertThat(SharedContentInbox.mimeAllowed(allowed, ShareReceiveContract.MIME_TEXT)).isFalse()
        assertThat(SharedContentInbox.mimeAllowed(emptyList(), "image/png")).isFalse()
    }

    @Test
    fun `text is only admitted when the text filter is declared`() {
        assertThat(
            SharedContentInbox.mimeAllowed(listOf(ShareReceiveContract.MIME_TEXT), "text/plain")
        ).isTrue()
        // A wildcard image filter must not let text through, which is exactly the case a
        // naive `startsWith` comparison would get wrong.
        assertThat(
            SharedContentInbox.mimeAllowed(listOf(ShareReceiveContract.MIME_IMAGES), "text/plain")
        ).isFalse()
    }

    // ── chooser matching ────────────────────────────────────────────────────────

    @Test
    fun `an unconstrained chooser accepts anything`() {
        assertThat(SharedContentInbox.chooserAccepts(null, "image/png")).isTrue()
        assertThat(SharedContentInbox.chooserAccepts(emptyList(), "image/png")).isTrue()
        assertThat(SharedContentInbox.chooserAccepts(listOf("*/*"), "application/pdf")).isTrue()
    }

    @Test
    fun `a constrained chooser matches concrete wildcard and extension accepts`() {
        assertThat(SharedContentInbox.chooserAccepts(listOf("image/png"), "image/png")).isTrue()
        assertThat(SharedContentInbox.chooserAccepts(listOf("image/*"), "image/jpeg")).isTrue()
        assertThat(SharedContentInbox.chooserAccepts(listOf(".png"), "image/png")).isTrue()
        assertThat(SharedContentInbox.chooserAccepts(listOf("image/png"), "image/jpeg")).isFalse()
        assertThat(SharedContentInbox.chooserAccepts(listOf("application/pdf"), "image/png")).isFalse()
    }

    // ── filename sanitiser ──────────────────────────────────────────────────────

    @Test
    fun `sender supplied names cannot escape the inbox directory`() {
        val cases = listOf(
            "../../../../etc/passwd",
            "/data/data/com.example/files/evil.png",
            "..\\..\\windows\\system32\\evil.png",
            "nested/dir/shot.png"
        )

        for (raw in cases) {
            val name = SharedContentInbox.sanitizeFileName(raw, "fallback", "image/png")
            assertThat(name).doesNotContain("/")
            assertThat(name).doesNotContain("\\")
            assertThat(name).doesNotContain("..")
            assertThat(File(name).name).isEqualTo(name)
        }
    }

    @Test
    fun `control characters are stripped and an extension is derived from the mime type`() {
        val name = SharedContentInbox.sanitizeFileName("scr\u0000een\u001Fshot", "id1", "image/png")
        assertThat(name).doesNotContain("\u0000")
        assertThat(name).doesNotContain("\u001F")
        assertThat(name).endsWith(".png")
    }

    @Test
    fun `a missing or unusable name falls back to the generated id`() {
        val fallback = SharedContentInbox.sanitizeFileName(null, "id42", "image/jpeg")
        assertThat(fallback).isEqualTo("id42.jpg")

        // A name that is nothing but separators and dots sanitises away to the id too.
        val dots = SharedContentInbox.sanitizeFileName("../..", "id43", "image/png")
        assertThat(dots).startsWith("id43")
    }

    // ── end-to-end round trip ───────────────────────────────────────────────────

    @Test
    fun `an accepted image is copied into the inbox and offered to the chooser`() = runBlocking {
        registerImage("png-bytes".toByteArray())

        val accepted = SharedContentInbox.acceptIntent(
            context,
            sendImageIntent(),
            listOf(ShareReceiveContract.MIME_IMAGES)
        )

        assertThat(accepted).hasSize(1)
        val item = accepted.single()
        assertThat(item.kind).isEqualTo(ShareReceiveContract.KIND_FILE)
        assertThat(item.mimeType).isEqualTo("image/png")
        assertThat(File(item.path!!).readText()).isEqualTo("png-bytes")

        // The chooser takes it, and taking it removes both the queue entry and the copy.
        val claimed = SharedContentInbox.findForFileChooser(context, listOf("image/*"))
        assertThat(claimed?.id).isEqualTo(item.id)

        SharedContentInbox.claim(context, item.id)
        assertThat(SharedContentInbox.pending(context)).isEmpty()
        assertThat(File(item.path).exists()).isFalse()
    }

    @Test
    fun `a share whose type is not declared is dropped without touching the inbox`() = runBlocking {
        registerImage()

        val accepted = SharedContentInbox.acceptIntent(
            context,
            sendImageIntent(declaredType = "application/pdf"),
            listOf(ShareReceiveContract.MIME_IMAGES)
        )

        assertThat(accepted).isEmpty()
        assertThat(SharedContentInbox.pending(context)).isEmpty()
        assertThat(SharedContentInbox.inboxDir(context).listFiles().orEmpty()).isEmpty()
    }

    @Test
    fun `only send actions are honoured`() = runBlocking {
        registerImage()

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, imageUri)
        }

        val accepted = SharedContentInbox.acceptIntent(
            context,
            viewIntent,
            listOf(ShareReceiveContract.MIME_IMAGES)
        )

        assertThat(accepted).isEmpty()
    }

    @Test
    fun `a text share is queued as a text item and never offered to a chooser`() = runBlocking {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = ShareReceiveContract.MIME_TEXT
            putExtra(Intent.EXTRA_TEXT, "https://example.com/story")
        }

        val accepted = SharedContentInbox.acceptIntent(
            context,
            intent,
            listOf(ShareReceiveContract.MIME_TEXT)
        )

        assertThat(accepted).hasSize(1)
        assertThat(accepted.single().text).isEqualTo("https://example.com/story")
        assertThat(accepted.single().isText).isTrue()

        // Text has no byte payload, so it must not be handed to a file chooser.
        assertThat(SharedContentInbox.findForFileChooser(context, null)).isNull()
        // Nor is it a payload file on disk.
        assertThat(File(accepted.single().path ?: "").exists()).isFalse()
    }

    @Test
    fun `multiple shares are capped and the newest is offered first`() = runBlocking {
        val second = Uri.parse("content://media/external/images/media/43")
        shadowOf(context.contentResolver)
            .registerInputStream(second, ByteArrayInputStream("second".toByteArray()))
        registerImage("first".toByteArray())

        SharedContentInbox.acceptIntent(
            context,
            sendImageIntent(),
            listOf(ShareReceiveContract.MIME_IMAGES)
        )
        val secondAccepted = SharedContentInbox.acceptIntent(
            context,
            Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, second)
            },
            listOf(ShareReceiveContract.MIME_IMAGES)
        )

        assertThat(secondAccepted).hasSize(1)
        val claimed = SharedContentInbox.findForFileChooser(context, listOf("image/*"))
        assertThat(claimed?.name).contains(".jpg")
        assertThat(SharedContentInbox.pending(context)).hasSize(2)
    }

    @Test
    fun `clearing the inbox removes every payload`() = runBlocking {
        registerImage()
        SharedContentInbox.acceptIntent(
            context,
            sendImageIntent(),
            listOf(ShareReceiveContract.MIME_IMAGES)
        )

        SharedContentInbox.clear(context)

        assertThat(SharedContentInbox.pending(context)).isEmpty()
        assertThat(SharedContentInbox.inboxDir(context).listFiles().orEmpty()).isEmpty()
    }
}
