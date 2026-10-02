package com.webtoapp.core.playstore.aab

import com.android.aapt.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Regression for issue #1160: when the source APK's resources.arsc references a
 * res/ file that was intentionally dropped from the payload (the SAEP raw
 * placeholder `res/rm.json` when SAEP is disabled, or shrinker leftovers in
 * clone APKs), the assembler must exclude that reference from resources.pb —
 * bundletool rejects dangling file references outright.
 */
class ApkToAabAssemblerMissingResTest {

    @get:Rule
    val temp = TemporaryFolder()

    private fun fileReferences(table: Resources.ResourceTable): Set<String> {
        val out = mutableSetOf<String>()
        for (pkg in table.packageList) {
            for (type in pkg.typeList) {
                for (entry in type.entryList) {
                    for (cv in entry.configValueList) {
                        val v = cv.value
                        if (v.hasItem() && v.item.hasFile()) out.add(v.item.file.path)
                        if (v.hasCompoundValue()) {
                            val c = v.compoundValue
                            c.style.entryList.forEach { if (it.item.hasFile()) out.add(it.item.file.path) }
                            c.array.elementList.forEach { if (it.item.hasFile()) out.add(it.item.file.path) }
                            c.plural.entryList.forEach { if (it.item.hasFile()) out.add(it.item.file.path) }
                        }
                    }
                }
            }
        }
        return out
    }

    /** Copies the template APK minus [droppedEntries], keeping resources.arsc intact. */
    private fun templateMinus(droppedEntries: Set<String>): File {
        val template = File("src/main/assets/template/webview_shell.apk")
        assumeTrue("shell template not built", template.exists())
        val out = temp.newFile("input-${droppedEntries.hashCode()}.apk")
        ZipFile(template).use { source ->
            ZipOutputStream(out.outputStream()).use { target ->
                source.entries().asSequence().forEach { entry ->
                    if (entry.name in droppedEntries) return@forEach
                    target.putNextEntry(ZipEntry(entry.name))
                    source.getInputStream(entry).use { it.copyTo(target) }
                    target.closeEntry()
                }
            }
        }
        return out
    }

    @Test
    fun `dangling arsc file references are excluded from resources pb`() {
        // Exact reproduction of the #1160 source APK: SAEP disabled builds drop
        // res/rm.json while raw/agent_saep_policy keeps pointing at it.
        val input = templateMinus(setOf("res/rm.json"))

        val aab = temp.newFile("out.aab")
        ApkToAabAssembler().assemble(input, aab)

        val entryNames = ZipFile(aab).use { zip ->
            zip.entries().toList().map { it.name }.toSet()
        }
        assertThat("base/res/rm.json" in entryNames).isFalse()

        val table = ZipFile(aab).use { zip ->
            Resources.ResourceTable.parseFrom(
                zip.getInputStream(zip.getEntry("base/resources.pb"))
            )
        }
        val refs = fileReferences(table)
        assertThat(refs).doesNotContain("res/rm.json")

        // Every remaining reference must resolve to a bundled file.
        val dangling = refs.filter { "base/$it" !in entryNames }
        assertThat(dangling).isEmpty()

        // The raw entry is gone entirely — nothing left referencing the file.
        val rawType = table.getPackage(0).typeList.firstOrNull { it.name == "raw" }
        assertThat(rawType).isNotNull()
        assertThat(rawType!!.entryList.map { it.name }).doesNotContain("agent_saep_policy")

        assertThat(AabValidationHelper.validateAab(aab).isValid).isTrue()
    }

    @Test
    fun `present referenced res files are still bundled`() {
        // Sanity guard for the same exclusion path: res/rm.json present in the
        // source stays referenced and is copied verbatim.
        val template = File("src/main/assets/template/webview_shell.apk")
        assumeTrue("shell template not built", template.exists())

        val aab = temp.newFile("out.aab")
        ApkToAabAssembler().assemble(template, aab)

        val entryNames = ZipFile(aab).use { zip ->
            zip.entries().toList().map { it.name }.toSet()
        }
        assertThat(entryNames).contains("base/res/rm.json")

        val table = ZipFile(aab).use { zip ->
            Resources.ResourceTable.parseFrom(
                zip.getInputStream(zip.getEntry("base/resources.pb"))
            )
        }
        assertThat(fileReferences(table)).contains("res/rm.json")
        assertThat(AabValidationHelper.validateAab(aab).isValid).isTrue()
    }
}
