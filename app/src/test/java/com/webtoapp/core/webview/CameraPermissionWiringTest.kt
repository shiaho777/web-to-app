package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

/**
 * Preview ([com.webtoapp.ui.webview.WebViewActivity]) and the generated shell
 * must keep the same camera hand-off. A flag-only EXTRA_OUTPUT, an uncaught
 * PermissionRequest.grant, or a resume probe that runs when the permission
 * dialog returns all close the installed app.
 */
class CameraPermissionWiringTest {

    private val shellDelegate = readSanitized("com/webtoapp/ui/shell/ShellPermissionDelegate.kt")
    private val shellScreen = readSanitized("com/webtoapp/ui/shell/ShellScreen.kt")
    private val preview = readSanitized("com/webtoapp/ui/webview/WebViewActivity.kt")
    private val gecko = readSanitized("com/webtoapp/core/engine/GeckoViewEngine.kt")

    @Test
    fun `camera output is granted through CameraCaptureIntents on both paths`() {
        assertWithMessage("shell file chooser")
            .that(shellDelegate).contains("CameraCaptureIntents.attachOutput")
        assertWithMessage("preview file chooser")
            .that(preview).contains("CameraCaptureIntents.attachOutput")
        assertWithMessage("shell must not go back to a flag-only camera grant")
            .that(shellDelegate).doesNotContain("FLAG_GRANT_WRITE_URI_PERMISSION")
        assertWithMessage("preview must not go back to a flag-only camera grant")
            .that(preview).doesNotContain("FLAG_GRANT_WRITE_URI_PERMISSION")
    }

    @Test
    fun `webview permission results are settled and the resume probe is skipped`() {
        assertWithMessage("shell")
            .that(shellDelegate).contains("WebPermissionRequests.settle")
        assertWithMessage("preview")
            .that(preview).contains("WebPermissionRequests.settle")
        assertWithMessage("shell resume")
            .that(shellScreen).contains("consumeResumeProbeSkip")
        assertWithMessage("preview resume")
            .that(preview).contains("consumeResumeProbeSkip")
        assertWithMessage("shell reloads when the dialog cancels the capture request")
            .that(shellDelegate).contains("onRuntimeResult")
        assertWithMessage("preview reloads when the dialog cancels the capture request")
            .that(preview).contains("onRuntimeResult")
    }

    @Test
    fun `gecko does not grant media devices before the os permission`() {
        val media = gecko.substringAfter("fun onMediaPermissionRequest")
            .substringBefore("fun onAndroidPermissionsRequest")
        assertWithMessage("missing camera permission must be requested, not granted")
            .that(media).contains("onAndroidPermissionsRequest")
        assertWithMessage("reject path must exist")
            .that(media).contains("callback.reject()")
    }

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Cannot find $relativePath").that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }
}
