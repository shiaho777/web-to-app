package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.playstore.aab.axml.AxmlBuilder
import com.webtoapp.core.playstore.aab.axml.AxmlToProtoXml
import org.junit.Assert.assertThrows
import org.junit.Test

class AxmlRebuilderSaepTest {
    private val rebuilder = AxmlRebuilder()
    // Synthetic fixture resource ID; production always resolves the template's real ID.
    private val resourceId = 0x7f12000f

    private fun manifest(policy: Boolean = true, stringValue: Boolean = false, duplicate: Boolean = false): ByteArray {
        val strings = listOf(
            "name", "resource", "value", "manifest", "application", "meta-data", "android",
            "http://schemas.android.com/apk/res/android", "example.keep", "untouched",
            SaepPolicy.TEMPLATE_METADATA, "@raw/agent_saep_policy"
        )
        val builder = AxmlBuilder().stringPool(strings)
            .resourceMap(intArrayOf(android.R.attr.name, android.R.attr.resource, android.R.attr.value))
            .startNamespace(6, 7).startElement(-1, 3, emptyList()).startElement(-1, 4, emptyList())
        fun metadata(name: Int, attr: Int, type: Int, data: Int) {
            builder.startElement(-1, 5, listOf(
                AxmlBuilder.Attr(7, 0, name, 0x03, name),
                AxmlBuilder.Attr(7, attr, if (type == 0x03) data else -1, type, data)
            )).endElement(-1, 5)
        }
        metadata(8, 2, 0x03, 9)
        if (policy) repeat(if (duplicate) 2 else 1) {
            metadata(10, if (stringValue) 2 else 1, if (stringValue) 0x03 else 0x01,
                if (stringValue) 11 else resourceId)
        }
        return builder.endElement(-1, 4).endElement(-1, 3).endNamespace(6, 7).build()
    }

    private fun metadata(bytes: ByteArray) = AxmlToProtoXml.convert(bytes).element.childList
        .single { it.hasElement() && it.element.name == "application" }.element.childList
        .filter { it.hasElement() && it.element.name == "meta-data" }
        .map { it.element }
        .associateBy { it.attributeList.single { attr -> attr.name == "name" }.value }

    @Test
    fun `opt in preserves compiled resource id namespace and unrelated metadata`() {
        val original = manifest()
        val result = rebuilder.rewriteSaepPolicyMetadata(original, resourceId)
        val values = metadata(result)
        assertThat(values.keys).containsExactly("example.keep", SaepPolicy.POLICY_METADATA)
        assertThat(values["example.keep"]).isEqualTo(metadata(original)["example.keep"])
        val ref = values.getValue(SaepPolicy.POLICY_METADATA).attributeList.single { it.name == "resource" }
        assertThat(ref.namespaceUri).isEqualTo("http://schemas.android.com/apk/res/android")
        assertThat(ref.resourceId).isEqualTo(android.R.attr.resource)
        assertThat(ref.compiledItem.hasRef()).isTrue()
        assertThat(ref.compiledItem.ref.id).isEqualTo(resourceId)
        assertThat(metadata(original).keys).contains(SaepPolicy.TEMPLATE_METADATA)
        assertThat(rebuilder.rewriteSaepPolicyMetadata(result, resourceId)).isEqualTo(result)
    }

    @Test
    fun `off removes both inert and previously active declarations`() {
        val original = manifest()
        val enabled = rebuilder.rewriteSaepPolicyMetadata(original, resourceId)
        for (input in listOf(original, enabled)) {
            val values = metadata(rebuilder.rewriteSaepPolicyMetadata(input, null))
            assertThat(values.keys).containsExactly("example.keep")
            assertThat(values["example.keep"]).isEqualTo(metadata(original)["example.keep"])
        }
    }

    @Test
    fun `old templates are unchanged when off but fail explicitly when enabled`() {
        val original = manifest(policy = false)
        assertThat(rebuilder.rewriteSaepPolicyMetadata(original, null)).isEqualTo(original)
        assertThrows(IllegalStateException::class.java) {
            rebuilder.rewriteSaepPolicyMetadata(original, resourceId)
        }
    }

    @Test
    fun `string values mismatched ids and duplicate declarations are rejected`() {
        assertThrows(IllegalStateException::class.java) {
            rebuilder.rewriteSaepPolicyMetadata(manifest(stringValue = true), resourceId)
        }
        assertThrows(IllegalStateException::class.java) {
            rebuilder.rewriteSaepPolicyMetadata(manifest(), resourceId + 1)
        }
        assertThrows(IllegalStateException::class.java) {
            rebuilder.rewriteSaepPolicyMetadata(manifest(duplicate = true), resourceId)
        }
    }
}
