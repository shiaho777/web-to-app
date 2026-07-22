package com.webtoapp.core.playstore.aab.axml

import com.android.aapt.Resources

object ProtoManifestRewriter {

    const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"

    private const val ATTR_RES_ID_TARGET_SDK_VERSION = 0x01010270

    private const val ATTR_RES_ID_MIN_SDK_VERSION = 0x0101020c

    fun rewriteTargetSdk(
        manifest: Resources.XmlNode,
        targetSdkVersion: Int
    ): Resources.XmlNode {
        require(manifest.hasElement()) { "Root XmlNode is not an element" }
        val root = manifest.element
        require(root.name == "manifest") {
            "Expected root element <manifest>, got <${root.name}>"
        }

        val newRoot = rewriteManifestElement(root, targetSdkVersion)
        return manifest.toBuilder().setElement(newRoot).build()
    }

    private fun rewriteManifestElement(
        manifest: Resources.XmlElement,
        targetSdkVersion: Int
    ): Resources.XmlElement {

        val children = manifest.childList
        var usesSdkIndex = -1
        for (i in children.indices) {
            val c = children[i]
            if (c.hasElement() && c.element.name == "uses-sdk") {
                usesSdkIndex = i
                break
            }
        }

        return if (usesSdkIndex >= 0) {

            val rewrittenUsesSdk = rewriteUsesSdkElement(
                children[usesSdkIndex].element,
                targetSdkVersion
            )
            val builder = manifest.toBuilder()
            builder.setChild(
                usesSdkIndex,
                children[usesSdkIndex].toBuilder().setElement(rewrittenUsesSdk).build()
            )
            builder.build()
        } else {

            val newUsesSdk = buildUsesSdkElement(targetSdkVersion)
            val newNode = Resources.XmlNode.newBuilder().setElement(newUsesSdk).build()
            val builder = manifest.toBuilder()
            builder.addChild(0, newNode)
            builder.build()
        }
    }

    private fun rewriteUsesSdkElement(
        usesSdk: Resources.XmlElement,
        targetSdkVersion: Int
    ): Resources.XmlElement {
        val attrs = usesSdk.attributeList
        val targetAttrIndex = attrs.indexOfFirst {
            it.namespaceUri == ANDROID_NAMESPACE && it.name == "targetSdkVersion"
        }

        val newAttr = buildIntAttribute(
            name = "targetSdkVersion",
            resourceId = ATTR_RES_ID_TARGET_SDK_VERSION,
            value = targetSdkVersion
        )

        val builder = usesSdk.toBuilder()
        if (targetAttrIndex >= 0) {
            builder.setAttribute(targetAttrIndex, newAttr)
        } else {
            builder.addAttribute(newAttr)
        }
        return builder.build()
    }

    private fun buildUsesSdkElement(targetSdkVersion: Int): Resources.XmlElement {
        return Resources.XmlElement.newBuilder()
            .setName("uses-sdk")
            .addAttribute(
                buildIntAttribute(
                    name = "targetSdkVersion",
                    resourceId = ATTR_RES_ID_TARGET_SDK_VERSION,
                    value = targetSdkVersion
                )
            )
            .build()
    }

    private fun buildIntAttribute(
        name: String,
        resourceId: Int,
        value: Int
    ): Resources.XmlAttribute {
        return Resources.XmlAttribute.newBuilder()
            .setName(name)
            .setNamespaceUri(ANDROID_NAMESPACE)
            .setResourceId(resourceId)
            .setValue(value.toString())
            .setCompiledItem(
                Resources.Item.newBuilder()
                    .setPrim(
                        Resources.Primitive.newBuilder()
                            .setIntDecimalValue(value)
                            .build()
                    )
                    .build()
            )
            .build()
    }

    fun extractTargetSdkVersion(manifest: Resources.XmlNode): Int? {
        if (!manifest.hasElement()) return null
        val root = manifest.element
        if (root.name != "manifest") return null
        for (child in root.childList) {
            if (!child.hasElement()) continue
            val el = child.element
            if (el.name != "uses-sdk") continue
            for (attr in el.attributeList) {
                if (attr.namespaceUri == ANDROID_NAMESPACE &&
                    attr.name == "targetSdkVersion"
                ) {
                    if (attr.hasCompiledItem() && attr.compiledItem.hasPrim() &&
                        attr.compiledItem.prim.hasIntDecimalValue()
                    ) {
                        return attr.compiledItem.prim.intDecimalValue
                    }
                    return attr.value.toIntOrNull()
                }
            }
        }
        return null
    }

    const val DEFAULT_PLAY_TARGET_SDK = 36
}
