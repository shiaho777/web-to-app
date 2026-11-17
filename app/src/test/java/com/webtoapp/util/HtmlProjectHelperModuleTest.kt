package com.webtoapp.util

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.HtmlFile
import com.webtoapp.data.model.HtmlFileType
import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HtmlProjectHelperModuleTest {

    @get:Rule
    val temp = TemporaryFolder()

    @Test
    fun `mjs files are classified as javascript`() {
        assertThat(HtmlProjectHelper.detectFileType("module.mjs")).isEqualTo(HtmlFileType.JS)
        assertThat(HtmlProjectHelper.detectFileType("MODULE.MJS")).isEqualTo(HtmlFileType.JS)
        assertThat(HtmlProjectHelper.detectFileType("app.js")).isEqualTo(HtmlFileType.JS)
    }

    @Test
    fun `projects with mjs scripts skip inlining and keep module references`() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val dir = temp.newFolder("module-project")
        val html = File(dir, "index.html").apply {
            writeText("""<html><head></head><body><script type="module" src="app.mjs"></script></body></html>""")
        }
        val mjs = File(dir, "app.mjs").apply { writeText("export const x = 1;") }
        val files = listOf(
            HtmlFile("index.html", html.absolutePath, HtmlFileType.HTML),
            HtmlFile("app.mjs", mjs.absolutePath, HtmlFileType.JS)
        )

        val saved = HtmlProjectHelper.processAndSaveFiles(context, files, "module-test-project")

        val savedHtml = saved.first { it.name == "index.html" }
        val content = File(savedHtml.path).readText()
        assertThat(content).contains("src=\"app.mjs\"")
        assertThat(content).doesNotContain("JS inlined")
        assertThat(saved.any { it.name == "app.mjs" }).isTrue()
    }
}
