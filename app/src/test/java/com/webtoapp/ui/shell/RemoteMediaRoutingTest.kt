package com.webtoapp.ui.shell

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

class RemoteMediaRoutingTest {

    private fun readSanitized(relativePath: String): String {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists)
            ?: error("Cannot locate java directory")
        val file = File(javaRoot, relativePath)
        assertWithMessage("Parity harness cannot find source file: $relativePath")
            .that(file.isFile).isTrue()
        var s = file.readText()
        s = Regex("\"\"\".*?\"\"\"", RegexOption.DOT_MATCHES_ALL).replace(s, "\"\"")
        s = Regex("\"(?:\\\\.|[^\"\\\\])*\"").replace(s, "\"\"")
        s = Regex("/\\*.*?\\*/", RegexOption.DOT_MATCHES_ALL).replace(s, " ")
        s = s.lines().joinToString("\n") { it.substringBefore("//") }
        return s
    }

    @Test
    fun `media router passes a remote target url to the display instead of a missing asset`() {
        val javaRoot = listOf("app/src/main/java", "src/main/java").asSequence()
            .map(::File).firstOrNull(File::exists) ?: error("Cannot locate java directory")
        val router = File(javaRoot, "com/webtoapp/ui/shell/ShellContentRouter.kt").readText()
        val mediaBranch = router.substringAfter("appType == \"IMAGE\" || appType == \"VIDEO\"")
            .substringBefore("appType == \"GALLERY\"", "")
        assertWithMessage("the media branch must derive a remote URL from targetUrl")
            .that(mediaBranch).contains("remoteMediaUrl")
        assertWithMessage("the media branch must hand the remote URL to MediaContentDisplay")
            .that(mediaBranch).contains("?: remoteMediaUrl")
    }

    @Test
    fun `media display loads remote images via coil and remote video via direct data source`() {
        val media = readSanitized("com/webtoapp/ui/shell/ShellMediaContent.kt")
        assertWithMessage("MediaContentDisplay must recognize http(s) media paths")
            .that(media).contains("remoteUrl")
        assertWithMessage("remote images must load through Coil")
            .that(media).contains("rememberAsyncImagePainter")
        assertWithMessage("remote video must play through a direct data source")
            .that(media).contains("val directSource = remoteUrl ?: hostFile?.absolutePath")
    }

    @Test
    fun `dead perf asset is no longer written or whitelisted`() {
        val builder = readSanitized("com/webtoapp/core/apkbuilder/ApkBuilder.kt")
        assertWithMessage("the unused wta_perf_optimize.js asset must not be written")
            .that(builder).doesNotContain("wta_perf_optimize")
        val cache = readSanitized("com/webtoapp/core/apkbuilder/ApkBuildCache.kt")
        assertWithMessage("the unused wta_perf_optimize.js asset must not be cache-whitelisted")
            .that(cache).doesNotContain("wta_perf_optimize")
    }
}
