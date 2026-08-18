package com.webtoapp.core.download

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DownloadFallbackPolicyTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun dest(): Pair<File, File> {
        val destFile = tmp.newFile("pkg.zip")
        destFile.delete()
        val tmpFile = File(destFile.parentFile, "${destFile.name}.tmp")
        return destFile to tmpFile
    }

    private fun engine() = DependencyDownloadEngine

    @Test
    fun `slow source keeps the partial tmp and switches to the next url`() = runBlocking {
        val (destFile, tmpFile) = dest()
        tmpFile.writeBytes(ByteArray(64))
        val calls = mutableListOf<String>()

        val ok = engine().downloadFileWithFallback(
            listOf("https://slow/proxied", "https://fast/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 2, retryDelayMs = 0,
            fetch = { url, _ ->
                calls.add(url)
                if (url.endsWith("slow/proxied")) {
                    // Partial bytes must survive the slow switch.
                    assertThat(tmpFile.exists()).isTrue()
                    DependencyDownloadEngine.Outcome.SLOW
                } else {
                    assertThat(tmpFile.exists()).isTrue()
                    DependencyDownloadEngine.Outcome.SUCCESS
                }
            }
        )
        assertThat(ok).isTrue()
        assertThat(calls).containsExactly("https://slow/proxied", "https://fast/proxied").inOrder()
    }

    @Test
    fun `failed source retries then drops tmp before switching`() = runBlocking {
        val (destFile, tmpFile) = dest()
        tmpFile.writeBytes(ByteArray(64))
        val calls = mutableListOf<String>()

        val ok = engine().downloadFileWithFallback(
            listOf("https://bad/proxied", "https://good/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 2, retryDelayMs = 0,
            fetch = { url, _ ->
                calls.add(url)
                if (url.endsWith("bad/proxied")) {
                    DependencyDownloadEngine.Outcome.FAILED
                } else {
                    // Retries exhausted on the previous source: the partial
                    // tmp must be gone so this source starts clean.
                    assertThat(tmpFile.exists()).isFalse()
                    DependencyDownloadEngine.Outcome.SUCCESS
                }
            }
        )
        assertThat(ok).isTrue()
        assertThat(calls).containsExactly(
            "https://bad/proxied", "https://bad/proxied", "https://good/proxied"
        ).inOrder()
    }

    @Test
    fun `slow on the last source resumes until retries are exhausted`() = runBlocking {
        val (destFile, _) = dest()
        var calls = 0

        val ok = engine().downloadFileWithFallback(
            listOf("https://only/proxied"),
            destFile, "Unit", null,
            maxRetryPerUrl = 3, retryDelayMs = 0,
            fetch = { _, _ ->
                calls++
                DependencyDownloadEngine.Outcome.SLOW
            }
        )
        assertThat(ok).isFalse()
        assertThat(calls).isEqualTo(3)
        assertThat(engine().state.value).isInstanceOf(DependencyDownloadEngine.State.Error::class.java)
        engine().reset()
    }

    @Test
    fun `success on first try makes a single call`() = runBlocking {
        val (destFile, _) = dest()
        var calls = 0

        val ok = engine().downloadFileWithFallback(
            listOf("https://good/proxied"),
            destFile, "Unit", null,
            fetch = { _, _ -> calls++; DependencyDownloadEngine.Outcome.SUCCESS }
        )
        assertThat(ok).isTrue()
        assertThat(calls).isEqualTo(1)
    }
}
