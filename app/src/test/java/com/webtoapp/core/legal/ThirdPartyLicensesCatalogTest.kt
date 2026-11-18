package com.webtoapp.core.legal

import com.google.common.truth.Truth.assertWithMessage
import com.google.gson.Gson
import org.junit.Test
import java.io.File

class ThirdPartyLicensesCatalogTest {

    private fun resolve(relative: String): File {
        val candidates = listOf("app/$relative", relative)
        return candidates.map(::File).firstOrNull(File::exists)
            ?: error("Could not locate $relative from: $candidates")
    }

    private fun catalog(): ThirdPartyLicenseCatalog {
        val json = resolve("src/main/assets/legal/third_party_licenses.json").readText()
        return Gson().fromJson(json, ThirdPartyLicenseCatalog::class.java)
    }

    private fun declaredDependencies(): List<String> {
        val source = resolve("build.gradle.kts").readText()
        val dependenciesBlock = source.substringAfterLast("dependencies {")
        val pattern = Regex("""(?:implementation|debugImplementation)\("([^"]+)"\)""")
        return pattern.findAll(dependenciesBlock).map { it.groupValues[1] }.toList()
    }

    private fun artifactName(coordinate: String): String {
        val parts = coordinate.split(":")
        return if (parts.size >= 2) parts[1] else coordinate
    }

    private fun covers(covered: String, artifact: String): Boolean {
        val stripped = artifact.removePrefix("androidx.")
        if (covered.contains(stripped)) return true
        val tokens = stripped.split("-")
        return tokens.any { token ->
            val alpha = token.filter { it.isLetter() }
            alpha.length >= 4 && covered.contains(alpha)
        }
    }

    @Test
    fun `catalog parses and every license text asset exists`() {
        val catalog = catalog()
        assertWithMessage("project license must be present").that(catalog.projectLicense).isNotNull()
        assertWithMessage("catalog must list bundled libraries").that(catalog.libraries).isNotEmpty()
        assertWithMessage("catalog must list runtimes").that(catalog.runtimes).isNotEmpty()

        val assets = catalog.licenseTexts.mapNotNull { it.asset } + catalog.projectLicense.textAsset
        assets.forEach { asset ->
            assertWithMessage("license text asset missing: $asset")
                .that(resolve("src/main/assets/$asset").isFile)
                .isTrue()
        }
    }

    @Test
    fun `every entry references a declared license text`() {
        val catalog = catalog()
        val known = catalog.licenseTexts.map { it.spdx }.toSet() + catalog.projectLicense.spdx
        val entries = catalog.libraries + catalog.runtimes
        entries.forEach { entry ->
            assertWithMessage("no license text declared for ${entry.name} (${entry.spdx})")
                .that(known)
                .contains(entry.spdx)
            assertWithMessage("entry must carry a reference url: ${entry.name}")
                .that(entry.url)
                .startsWith("https://")
        }
    }

    @Test
    fun `every bundled runtime dependency is covered by the catalog`() {
        val catalog = catalog()
        val covered = (catalog.libraries + catalog.runtimes).joinToString(" ") { it.name.lowercase() }
        val missing = declaredDependencies()
            .filterNot { it.endsWith("-bom") || it.contains(":room-compiler") }
            .filterNot { it.split(":").size == 2 }
            .filterNot { artifactName(it).startsWith("ui-tooling") }
            .filterNot { coordinate ->
                val artifact = artifactName(coordinate).lowercase()
                covers(covered, artifact)
            }
        assertWithMessage(
            "Dependencies in app/build.gradle.kts are missing from legal/third_party_licenses.json: $missing"
        ).that(missing).isEmpty()
    }
}
