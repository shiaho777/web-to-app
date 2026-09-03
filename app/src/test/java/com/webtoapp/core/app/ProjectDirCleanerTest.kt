package com.webtoapp.core.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.HtmlConfig
import com.webtoapp.data.model.MultiWebConfig
import com.webtoapp.data.model.MultiWebSite
import com.webtoapp.data.model.WebApp
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Regression coverage for issue #722: deleting a MULTI_WEB app that borrowed the
 * source HTML app's projectId (build-only-from-EXISTING-sites flow) must not wipe
 * the html_projects directory still owned by the source app.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProjectDirCleanerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun multiWebApp(projectId: String, sites: List<MultiWebSite>) = WebApp(
        id = 1L,
        name = "multi",
        url = "",
        appType = AppType.MULTI_WEB,
        htmlConfig = HtmlConfig(projectId = projectId),
        multiWebConfig = MultiWebConfig(sites = sites, projectId = projectId)
    )

    @Test
    fun `deleting borrowed-project multi-web app keeps the source project dir`() {
        val projectDir = File(context.filesDir, "html_projects/src-project-1").apply { mkdirs() }
        File(projectDir, "index.html").writeText("<html></html>")

        val app = multiWebApp(
            projectId = "src-project-1",
            sites = listOf(
                MultiWebSite(id = "s1", name = "site", type = "EXISTING", sourceProjectId = "src-project-1")
            )
        )

        val deleted = ProjectDirCleaner.deleteForApp(context, app)

        assertThat(deleted).isEmpty()
        assertThat(projectDir.exists()).isTrue()
        projectDir.deleteRecursively()
    }

    @Test
    fun `deleting own-project multi-web app removes its project dir`() {
        val projectDir = File(context.filesDir, "html_projects/own-project").apply { mkdirs() }
        File(projectDir, "index.html").writeText("<html></html>")

        val app = multiWebApp(
            projectId = "own-project",
            sites = listOf(
                MultiWebSite(id = "s1", name = "site", type = "LOCAL", localFilePath = "site/index.html")
            )
        )

        val deleted = ProjectDirCleaner.deleteForApp(context, app)

        assertThat(deleted).isNotEmpty()
        assertThat(projectDir.exists()).isFalse()
    }
}
