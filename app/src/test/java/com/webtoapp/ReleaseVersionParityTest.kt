package com.webtoapp

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import java.io.File

class ReleaseVersionParityTest {

    private data class Version(val code: Int, val name: String)

    private companion object {
        val VERSION_CODE = Regex("versionCode\\s*=\\s*(\\d+)")
        val VERSION_NAME = Regex("versionName\\s*=\\s*\"([^\"]+)\"")
    }

    private val repoRoot: File by lazy {
        generateSequence(File(".").absoluteFile) { it.parentFile }
            .firstOrNull {
                File(it, "settings.gradle.kts").isFile && File(it, "app/build.gradle.kts").isFile
            }
            ?: error("Cannot locate repository root containing settings.gradle.kts and app/build.gradle.kts")
    }

    private val hostVersion: Version by lazy { gradleVersion("app/build.gradle.kts") }

    private fun source(relativePath: String): String {
        val file = File(repoRoot, relativePath)
        assertWithMessage("Release parity harness cannot find $relativePath")
            .that(file.isFile).isTrue()
        return file.readText()
    }

    private fun gradleVersion(relativePath: String): Version {
        val text = source(relativePath)
        val code = VERSION_CODE.find(text)?.groupValues?.get(1)?.toIntOrNull()
            ?: error("No versionCode found in $relativePath")
        val name = VERSION_NAME.find(text)?.groupValues?.get(1)
            ?: error("No versionName found in $relativePath")
        return Version(code, name)
    }

    @Test
    fun `shell template version matches the host release version`() {
        val shell = gradleVersion("shell/build.gradle.kts")
        assertWithMessage(
            "shell/build.gradle.kts versionCode must be bumped with app/build.gradle.kts " +
                "(AGENTS.md recipe 13)"
        ).that(shell.code).isEqualTo(hostVersion.code)
        assertWithMessage(
            "shell/build.gradle.kts versionName must be bumped with app/build.gradle.kts " +
                "(AGENTS.md recipe 13)"
        ).that(shell.name).isEqualTo(hostVersion.name)
    }

    @Test
    fun `contributing guide states the current release in both languages`() {
        val text = source(".github/CONTRIBUTING.md")
        assertWithMessage("CONTRIBUTING.md must state the current versionName in English and Chinese")
            .that(text.split("WebToApp ${hostVersion.name}").size - 1).isEqualTo(2)
        assertWithMessage("CONTRIBUTING.md must state the current versionCode in English and Chinese")
            .that(text.split("versionCode ${hostVersion.code}").size - 1).isEqualTo(2)
    }

    @Test
    fun `f-droid metadata tracks the current release`() {
        val text = source("metadata/com.webtoapp.yml")
        listOf(
            "versionName: \"${hostVersion.name}\"",
            "versionCode: ${hostVersion.code}",
            "commit: v${hostVersion.name}",
            "CurrentVersion: \"${hostVersion.name}\"",
            "CurrentVersionCode: ${hostVersion.code}"
        ).forEach { expected ->
            assertWithMessage("metadata/com.webtoapp.yml is missing release metadata: $expected")
                .that(text).contains(expected)
        }
    }

    @Test
    fun `release notes exist for the current versionCode`() {
        val notes = File(repoRoot, "fastlane/metadata/android/en-US/changelogs/${hostVersion.code}.txt")
        assertWithMessage("Add fastlane release notes for versionCode ${hostVersion.code} (AGENTS.md recipe 13)")
            .that(notes.isFile).isTrue()
    }
}
