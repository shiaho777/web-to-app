package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import com.android.aapt.Resources.XmlElement
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A clone must show the name the user chose (#1459). The ARSC byte replacement
 * misses literal, activity-specific, and localized labels, so the manifest
 * label is the authoritative source.
 *
 * Fixture `axml/base_manifest.axml` is a real aapt2-compiled manifest for
 * `com.spike.base` with no application label and one launcher activity.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AxmlRebuilderAppLabelTest {

    private val rebuilder = AxmlRebuilder()

    private fun fixture(): ByteArray =
        javaClass.classLoader!!.getResourceAsStream("axml/base_manifest.axml")!!.readBytes()

    private fun XmlElement.attr(name: String) = attributeList.firstOrNull { it.name == name }

    private fun application(axml: ByteArray): XmlElement =
        AxmlToProtoXml.convert(axml).element.childList
            .first { it.hasElement() && it.element.name == "application" }
            .element

    private fun launcher(axml: ByteArray): XmlElement =
        application(axml).childList
            .first { it.hasElement() && it.element.name == "activity" }
            .element

    @Test
    fun `adds a label when the application has none`() {
        val modified = rebuilder.setAppLabel(fixture(), "Cloned Name")

        assertThat(application(modified).attr("label")?.value).isEqualTo("Cloned Name")
        assertThat(launcher(modified).attr("label")?.value).isEqualTo("Cloned Name")
    }

    @Test
    fun `replaces an existing label`() {
        val once = rebuilder.setAppLabel(fixture(), "First Name")
        val twice = rebuilder.setAppLabel(once, "Second Name")

        assertThat(application(twice).attr("label")?.value).isEqualTo("Second Name")
        assertThat(launcher(twice).attr("label")?.value).isEqualTo("Second Name")
    }

    @Test
    fun `label survives a package rewrite`() {
        val renamed = rebuilder.expandAndModify(
            fixture(),
            originalPackage = "com.spike.base",
            newPackage = "c.testclone"
        )
        val labelled = rebuilder.setAppLabel(renamed, "Cloned Name")

        assertThat(application(labelled).attr("label")?.value).isEqualTo("Cloned Name")
        assertThat(launcher(labelled).attr("label")?.value).isEqualTo("Cloned Name")
    }
}
