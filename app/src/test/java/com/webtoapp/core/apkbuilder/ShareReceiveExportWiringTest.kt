package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.ShareDeliveryMode
import com.webtoapp.data.model.WebApp
import com.webtoapp.data.model.WebViewConfig
import com.webtoapp.util.GsonProvider
import org.junit.Test

/**
 * Pins the export wiring for the inbound share sheet (issue #943), end to end but without the
 * shell template, so it also runs on CI (which skips the template sync).
 *
 * The chain is `WebViewConfig → ShareReceiveBlock → shell JSON → ShellConfig.webViewConfig`.
 * Two things here are easy to get wrong and are exactly what this file locks down:
 *
 * 1. The four runtime keys must land inside the nested `webViewConfig` object — they are read
 *    from [com.webtoapp.core.shell.WebViewShellConfig], not from the flat `ShellConfig`. Put
 *    them at the top level and Gson silently drops all four; the app then accepts the share,
 *    finds no delivery mode, and does nothing. (That is what the first draft of this change
 *    did; `WebViewConfigBooleanCoverageTest` caught it.)
 * 2. The resolved mime list is derived from the two toggles, and an app that receives neither
 *    type must resolve to `enabled = false` so the manifest is left untouched.
 */
class ShareReceiveExportWiringTest {

    private fun webApp(
        images: Boolean = false,
        text: Boolean = false,
        mode: ShareDeliveryMode = ShareDeliveryMode.BOTH,
        prompt: Boolean = true
    ) = WebApp(
        name = "share",
        url = "https://example.com",
        webViewConfig = WebViewConfig(
            receiveShareImages = images,
            receiveShareText = text,
            shareDeliveryMode = mode,
            sharePromptBeforeUse = prompt
        )
    )

    private fun shellConfigOf(app: WebApp): ShellConfig {
        val apk = app.toApkConfig("com.example.share")
        val json = ApkConfigJsonFactory.toShellConfigJson(apk)
        return GsonProvider.gson.fromJson(json, ShellConfig::class.java)!!
    }

    @Test
    fun `the image toggle alone registers the image wildcard`() {
        val block = webApp(images = true).toApkConfig("com.example.share").shareReceive

        assertThat(block.enabled).isTrue()
        assertThat(block.images).isTrue()
        assertThat(block.text).isFalse()
        assertThat(block.mimeTypes).containsExactly("image/*")
    }

    @Test
    fun `the text toggle adds the plain text filter`() {
        val block = webApp(images = true, text = true).toApkConfig("com.example.share").shareReceive

        assertThat(block.mimeTypes).containsExactly("image/*", "text/plain")
    }

    @Test
    fun `text without images is accepted on its own`() {
        val block = webApp(text = true).toApkConfig("com.example.share").shareReceive

        assertThat(block.enabled).isTrue()
        assertThat(block.mimeTypes).containsExactly("text/plain")
    }

    @Test
    fun `an app that receives neither type leaves the manifest alone`() {
        val block = webApp().toApkConfig("com.example.share").shareReceive

        assertThat(block.enabled).isFalse()
        assertThat(block.mimeTypes).isEmpty()
    }

    @Test
    fun `delivery mode and prompt survive the export pipeline`() {
        val app = webApp(
            images = true,
            mode = ShareDeliveryMode.FILE_CHOOSER_PREFILL,
            prompt = false
        )

        val block = app.toApkConfig("com.example.share").shareReceive
        assertThat(block.deliveryMode).isEqualTo("FILE_CHOOSER_PREFILL")
        assertThat(block.promptBeforeUse).isFalse()

        val shellWv = shellConfigOf(app).webViewConfig
        assertThat(shellWv.shareDeliveryMode).isEqualTo("FILE_CHOOSER_PREFILL")
        assertThat(shellWv.sharePromptBeforeUse).isFalse()
        assertThat(shellWv.receiveShareImages).isTrue()
        assertThat(shellWv.receiveShareText).isFalse()
    }

    @Test
    fun `the four runtime keys land in the nested webViewConfig payload`() {
        val app = webApp(images = true, text = true)
        val json = ApkConfigJsonFactory.toShellConfigJson(app.toApkConfig("com.example.share"))

        // Structured, not substring: the keys have to be reachable from the webViewConfig
        // object that Gson binds, whichever map happens to emit them.
        val root = GsonProvider.gson.fromJson(json, MutableMap::class.java)
        val nested = root["webViewConfig"] as? Map<*, *>
        assertThat(nested).isNotNull()
        assertThat(nested!!.keys).containsAtLeast(
            "receiveShareImages",
            "receiveShareText",
            "shareDeliveryMode",
            "sharePromptBeforeUse"
        )
    }

    @Test
    fun `defaults keep the feature off for a freshly created app`() {
        val app = WebApp(name = "fresh", url = "https://example.com")

        assertThat(app.webViewConfig.receiveShareImages).isFalse()
        assertThat(app.webViewConfig.receiveShareText).isFalse()
        assertThat(app.webViewConfig.enableShareReceive).isFalse()
        assertThat(app.toApkConfig("com.example.share").shareReceive.enabled).isFalse()
    }

    @Test
    fun `the derived helpers describe which channels are active`() {
        val eventOnly = WebViewConfig(
            receiveShareImages = true,
            shareDeliveryMode = ShareDeliveryMode.JS_EVENT
        )
        assertThat(eventOnly.broadcastsShareEvent).isTrue()
        assertThat(eventOnly.prefillsFileChooser).isFalse()

        val chooserOnly = WebViewConfig(
            receiveShareImages = true,
            shareDeliveryMode = ShareDeliveryMode.FILE_CHOOSER_PREFILL
        )
        assertThat(chooserOnly.broadcastsShareEvent).isFalse()
        assertThat(chooserOnly.prefillsFileChooser).isTrue()

        val both = WebViewConfig(receiveShareImages = true)
        assertThat(both.broadcastsShareEvent).isTrue()
        assertThat(both.prefillsFileChooser).isTrue()

        // Nothing enabled: both channels stay off regardless of the mode.
        val off = WebViewConfig(receiveShareImages = false, receiveShareText = false)
        assertThat(off.broadcastsShareEvent).isFalse()
        assertThat(off.prefillsFileChooser).isFalse()
    }
}
