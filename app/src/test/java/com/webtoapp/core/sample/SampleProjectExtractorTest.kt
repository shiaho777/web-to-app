package com.webtoapp.core.sample

import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SampleProjectExtractorTest {

    @Rule @JvmField
    val koinRule = com.webtoapp.util.KoinCleanupRule()

    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        SampleSharedPackManager.manifestUnreachable = true
        SampleProjectExtractor.clearExtractedProjects(context)
    }

    @After
    fun tearDown() {
        SampleSharedPackManager.manifestUnreachable = false
        SampleProjectExtractor.clearExtractedProjects(context)
    }

    @Test
    fun `python fastapi sample extraction succeeds without shared pack`() = runBlocking {
        val result = SampleProjectExtractor.extractSampleProject(context, "python-fastapi")

        assertThat(result.isSuccess).isTrue()
        val projectDir = File(requireNotNull(result.getOrNull()))
        assertThat(File(projectDir, "main.py").exists()).isTrue()
        val sitePackagesDir = File(projectDir, ".pypackages")
        assertThat(sitePackagesDir.walkTopDown().any { it.isFile }).isFalse()
    }

    @Test
    fun `python fastapi sample extraction reuses cached shared pack`() = runBlocking {
        seedSharedPack("python-fastapi-shared", ".pypackages/requests/__init__.py")

        val result = SampleProjectExtractor.extractSampleProject(context, "python-fastapi")

        assertThat(result.isSuccess).isTrue()
        val projectDir = File(requireNotNull(result.getOrNull()))
        assertThat(File(projectDir, ".pypackages/requests/__init__.py").isFile).isTrue()
    }

    @Test
    fun `python django sample extraction succeeds without shared pack`() = runBlocking {
        val result = SampleProjectExtractor.extractSampleProject(context, "python-django")

        assertThat(result.isSuccess).isTrue()
        val projectDir = File(requireNotNull(result.getOrNull()))
        assertThat(File(projectDir, "manage.py").exists()).isTrue()
        val sitePackagesDir = File(projectDir, ".pypackages")
        assertThat(sitePackagesDir.walkTopDown().any { it.isFile }).isFalse()
    }

    private fun seedSharedPack(packName: String, vararg files: String) {
        val payloadDir =
            File(context.filesDir, "sample_shared_packs/$packName/$packName")
        files.forEach { rel ->
            File(payloadDir, rel).apply {
                parentFile?.mkdirs()
                writeText("seed")
            }
        }
    }
}
