package com.webtoapp.core.playstore.aab.arsc

import com.android.aapt.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Regression guard for issue #293: when the AAB assembler skips a plaintext res XML file
 * (issue #272), the resource table must also drop the entry that references it. Otherwise the
 * AAB's resource table references a non-existing file and Google Play rejects the bundle
 * (bundletool: "Resource table of module 'base' contains references to non-existing files").
 */
class ArscToProtoTableExcludedFilesTest {

    private fun sampleTable(): ArscResourceTable = ArscResourceTable(
        valueStringPool = listOf("res/qF.xml", "res/keep.xml"),
        packages = listOf(
            ArscPackage(
                id = 0x7f,
                name = "com.webtoapp.test",
                typeNames = listOf("xml"),
                keyNames = listOf("marker", "keep"),
                typeSpecs = listOf(ArscTypeSpec(typeId = 1, configFlags = intArrayOf(0, 0))),
                types = listOf(
                    ArscType(
                        typeId = 1,
                        config = ArscConfig.DEFAULT,
                        entries = listOf(
                            ArscEntry(
                                index = 0,
                                name = "marker",
                                flags = 0,
                                // dataType 0x03 == TYPE_STRING; data indexes valueStringPool.
                                body = ArscEntryBody.Simple(ArscValue(dataType = 0x03, data = 0))
                            ),
                            ArscEntry(
                                index = 1,
                                name = "keep",
                                flags = 0,
                                body = ArscEntryBody.Simple(ArscValue(dataType = 0x03, data = 1))
                            )
                        )
                    )
                )
            )
        )
    )

    private fun Resources.ResourceTable.fileReferences(): Set<String> {
        val out = mutableSetOf<String>()
        for (pkg in packageList) {
            for (type in pkg.typeList) {
                for (entry in type.entryList) {
                    for (cv in entry.configValueList) {
                        if (cv.value.item.hasFile()) {
                            out.add(cv.value.item.file.path)
                        }
                    }
                }
            }
        }
        return out
    }

    @Test
    fun `without exclusion the resource table references the plaintext file`() {
        val proto = ArscToProtoTable.convert(sampleTable())
        assertThat(proto.fileReferences()).containsExactly("res/qF.xml", "res/keep.xml")
    }

    @Test
    fun `excluding a plaintext file drops its entry but keeps the rest`() {
        val proto = ArscToProtoTable.convert(sampleTable(), excludedResFiles = setOf("res/qF.xml"))
        assertThat(proto.fileReferences()).containsExactly("res/keep.xml")
    }

    @Test
    fun `collectReferencedResourceFiles sees the plaintext reference before exclusion`() {
        assertThat(sampleTable().collectReferencedResourceFiles()).contains("res/qF.xml")
    }
}
