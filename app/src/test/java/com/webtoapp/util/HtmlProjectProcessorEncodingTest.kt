package com.webtoapp.util

import com.google.common.truth.Truth.assertThat
import java.io.File
import java.nio.charset.Charset
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class HtmlProjectProcessorEncodingTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `null encoding auto-detects gbk from meta charset`() {
        val html = "<html><head><meta charset=\"gbk\"></head><body>你好世界</body></html>"
        val file = temp.newFile("gbk.html").apply {
            writeBytes(html.toByteArray(Charset.forName("GBK")))
        }

        val content = HtmlProjectProcessor.readFileWithEncoding(file, null)

        assertThat(content).contains("你好世界")
        assertThat(content).doesNotContain("\uFFFD")
    }

    @Test
    fun `null encoding reads utf-8 bom file correctly`() {
        val html = "<html><body>你好</body></html>"
        val file = temp.newFile("bom.html").apply {
            writeBytes(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + html.toByteArray(Charsets.UTF_8))
        }

        val content = HtmlProjectProcessor.readFileWithEncoding(file, null)

        assertThat(content).contains("你好")
    }

    @Test
    fun `processed gbk html declares utf-8 after transcode`() {
        val html = "<html><head><meta charset=\"gbk\"></head><body>你好世界</body></html>"
        val file = temp.newFile("gbk2.html").apply {
            writeBytes(html.toByteArray(Charset.forName("GBK")))
        }

        val content = HtmlProjectProcessor.readFileWithEncoding(file, null)
        val processed = HtmlProjectProcessor.processHtmlContent(content, null, null, fixPaths = false)

        assertThat(processed).contains("你好世界")
        assertThat(processed.lowercase()).contains("charset=\"utf-8\"")
        assertThat(processed.lowercase()).doesNotContain("charset=\"gbk\"")
    }

    @Test
    fun `rewriteCharsetDeclaration handles meta charset and http-equiv`() {
        val meta = HtmlProjectProcessor.rewriteCharsetDeclaration(
            """<meta charset="gb2312"><meta charset='utf-16'>"""
        )
        assertThat(meta.lowercase()).contains("""<meta charset="utf-8"""")
        assertThat(meta.lowercase()).contains("""<meta charset='utf-8'""")
        assertThat(meta.lowercase()).doesNotContain("gb2312")

        val httpEquiv = HtmlProjectProcessor.rewriteCharsetDeclaration(
            """<meta http-equiv="Content-Type" content="text/html; charset=gbk">"""
        )
        assertThat(httpEquiv.lowercase()).contains("charset=utf-8")
        assertThat(httpEquiv.lowercase()).doesNotContain("charset=gbk")
    }

    @Test
    fun `rewriteCharsetDeclaration leaves utf-8 and absent charset untouched`() {
        val utf8 = """<meta charset="utf-8">"""
        assertThat(HtmlProjectProcessor.rewriteCharsetDeclaration(utf8)).isEqualTo(utf8)
        val none = "<html><head><title>x</title></head></html>"
        assertThat(HtmlProjectProcessor.rewriteCharsetDeclaration(none)).isEqualTo(none)
    }
}
