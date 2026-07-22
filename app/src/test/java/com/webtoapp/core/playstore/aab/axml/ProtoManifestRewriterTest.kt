package com.webtoapp.core.playstore.aab.axml

import com.android.aapt.Resources
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ProtoManifestRewriterTest {

    private val androidNs = ProtoManifestRewriter.ANDROID_NAMESPACE

    @Test
    fun `rewrite replaces existing targetSdkVersion`() {
        val manifest = manifest {
            usesSdk(minSdk = 23, targetSdk = 28)
            application()
        }

        val rewritten = ProtoManifestRewriter.rewriteTargetSdk(manifest, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        val targetSdk = ProtoManifestRewriter.extractTargetSdkVersion(rewritten)
        assertThat(targetSdk).isEqualTo(ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)
    }

    @Test
    fun `rewrite does not touch minSdkVersion`() {
        val manifest = manifest {
            usesSdk(minSdk = 23, targetSdk = 28)
        }

        val rewritten = ProtoManifestRewriter.rewriteTargetSdk(manifest, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        val usesSdkEl = rewritten.element.childList
            .first { it.hasElement() && it.element.name == "uses-sdk" }
            .element
        val minSdkAttr = usesSdkEl.attributeList.first { it.name == "minSdkVersion" }
        assertThat(minSdkAttr.compiledItem.prim.intDecimalValue).isEqualTo(23)
    }

    @Test
    fun `rewrite preserves unrelated attributes on uses-sdk`() {
        val manifest = manifestBuilder().also { mb ->
            val usesSdk = Resources.XmlElement.newBuilder()
                .setName("uses-sdk")
                .addAttribute(intAttr("minSdkVersion", 0x0101020c, 23))
                .addAttribute(intAttr("maxSdkVersion", 0x01010271, 30))
                .build()
            mb.addChild(Resources.XmlNode.newBuilder().setElement(usesSdk))
        }.build()
        val node = Resources.XmlNode.newBuilder().setElement(manifest).build()

        val rewritten = ProtoManifestRewriter.rewriteTargetSdk(node, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        val usesSdkEl = rewritten.element.childList
            .first { it.hasElement() && it.element.name == "uses-sdk" }
            .element
        val attrNames = usesSdkEl.attributeList.map { it.name }
        assertThat(attrNames).containsExactly(
            "minSdkVersion",
            "maxSdkVersion",
            "targetSdkVersion"
        )

        val maxSdkAttr = usesSdkEl.attributeList.first { it.name == "maxSdkVersion" }
        assertThat(maxSdkAttr.compiledItem.prim.intDecimalValue).isEqualTo(30)
    }

    @Test
    fun `rewrite inserts uses-sdk when manifest has none`() {
        val manifest = manifest {

            application()
        }

        val rewritten = ProtoManifestRewriter.rewriteTargetSdk(manifest, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        val children = rewritten.element.childList

        assertThat(children.first().element.name).isEqualTo("uses-sdk")
        assertThat(ProtoManifestRewriter.extractTargetSdkVersion(rewritten)).isEqualTo(ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        val appCount = children.count {
            it.hasElement() && it.element.name == "application"
        }
        assertThat(appCount).isEqualTo(1)
    }

    @Test
    fun `rewrite adds targetSdkVersion when uses-sdk has only minSdk`() {
        val manifest = manifestBuilder().also { mb ->
            val usesSdk = Resources.XmlElement.newBuilder()
                .setName("uses-sdk")
                .addAttribute(intAttr("minSdkVersion", 0x0101020c, 23))
                .build()
            mb.addChild(Resources.XmlNode.newBuilder().setElement(usesSdk))
        }.build()
        val node = Resources.XmlNode.newBuilder().setElement(manifest).build()

        val rewritten = ProtoManifestRewriter.rewriteTargetSdk(node, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        assertThat(ProtoManifestRewriter.extractTargetSdkVersion(rewritten)).isEqualTo(ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)
    }

    @Test
    fun `rewrite stamps android namespace on the target attribute`() {
        val manifest = manifest { application() }

        val rewritten = ProtoManifestRewriter.rewriteTargetSdk(manifest, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        val usesSdkEl = rewritten.element.childList
            .first { it.hasElement() && it.element.name == "uses-sdk" }
            .element
        val targetAttr = usesSdkEl.attributeList
            .first { it.name == "targetSdkVersion" }
        assertThat(targetAttr.namespaceUri).isEqualTo(androidNs)

        assertThat(targetAttr.resourceId).isEqualTo(0x01010270)

        assertThat(targetAttr.value).isEqualTo(ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK.toString())
        assertThat(targetAttr.compiledItem.prim.intDecimalValue).isEqualTo(ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)
    }

    @Test
    fun `rewrite leaves original manifest untouched`() {
        val manifest = manifest { usesSdk(minSdk = 23, targetSdk = 28) }
        val original = manifest.toByteArray()

        ProtoManifestRewriter.rewriteTargetSdk(manifest, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)

        assertThat(manifest.toByteArray()).isEqualTo(original)
        assertThat(ProtoManifestRewriter.extractTargetSdkVersion(manifest)).isEqualTo(28)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `rewrite rejects non-manifest root`() {
        val notManifest = Resources.XmlNode.newBuilder()
            .setElement(Resources.XmlElement.newBuilder().setName("layout"))
            .build()

        ProtoManifestRewriter.rewriteTargetSdk(notManifest, ProtoManifestRewriter.DEFAULT_PLAY_TARGET_SDK)
    }

    private fun manifest(block: ManifestBuilderScope.() -> Unit): Resources.XmlNode {
        val mb = manifestBuilder()
        ManifestBuilderScope(mb).apply(block)
        return Resources.XmlNode.newBuilder().setElement(mb.build()).build()
    }

    private fun manifestBuilder(): Resources.XmlElement.Builder =
        Resources.XmlElement.newBuilder().setName("manifest")

    private inner class ManifestBuilderScope(private val mb: Resources.XmlElement.Builder) {
        fun usesSdk(minSdk: Int, targetSdk: Int) {
            val el = Resources.XmlElement.newBuilder()
                .setName("uses-sdk")
                .addAttribute(intAttr("minSdkVersion", 0x0101020c, minSdk))
                .addAttribute(intAttr("targetSdkVersion", 0x01010270, targetSdk))
                .build()
            mb.addChild(Resources.XmlNode.newBuilder().setElement(el))
        }

        fun application() {
            val el = Resources.XmlElement.newBuilder().setName("application").build()
            mb.addChild(Resources.XmlNode.newBuilder().setElement(el))
        }
    }

    private fun intAttr(name: String, resId: Int, value: Int): Resources.XmlAttribute =
        Resources.XmlAttribute.newBuilder()
            .setName(name)
            .setNamespaceUri(androidNs)
            .setResourceId(resId)
            .setValue(value.toString())
            .setCompiledItem(
                Resources.Item.newBuilder().setPrim(
                    Resources.Primitive.newBuilder().setIntDecimalValue(value)
                )
            )
            .build()
}
