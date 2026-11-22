package com.webtoapp.core.apkbuilder

import com.webtoapp.core.playstore.aab.arsc.ArscEntryBody
import com.webtoapp.core.playstore.aab.arsc.ArscReader
import com.webtoapp.core.playstore.aab.arsc.ArscResourceTable
import com.webtoapp.core.playstore.aab.axml.AxmlConstants
import com.webtoapp.core.playstore.aab.axml.AxmlReader
import com.webtoapp.data.model.NetworkTrustConfig
import com.webtoapp.util.NetworkTrustStorage
import java.io.File
import java.util.zip.ZipFile
import org.bouncycastle.util.encoders.Base64

internal object ApkNetworkSecurityConfig {
    private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
    private const val CA_BUNDLE_METADATA = "com.webtoapp.CUSTOM_CA_BUNDLE"

    fun replacements(
        template: ZipFile,
        config: NetworkTrustConfig,
        antiCapture: Boolean,
        axml: AxmlRebuilder
    ): Map<String, ByteArray> {
        val trustUserCa = config.trustUserCa && !antiCapture
        if (config == NetworkTrustConfig()) return emptyMap()

        val manifest = AxmlReader(template.readEntry("AndroidManifest.xml"))
        val application = manifest.readDocument().children.filterIsInstance<AxmlReader.Element>()
            .single { it.name == "application" }
        val configId = application.resourceId("networkSecurityConfig")
        val resources = ArscReader(template.readEntry("resources.arsc")).read()
        val configPath = resources.resourcePath(configId, "xml")
        val replacements = mutableMapOf<String, ByteArray>()
        val bundleId = if (config.customCaCertificates.isEmpty()) {
            null
        } else {
            val marker = checkNotNull(application.children.filterIsInstance<AxmlReader.Element>()
                .singleOrNull { element ->
                    val name = element.attribute("name")
                    element.name == "meta-data" && name?.typedValueType == AxmlConstants.TYPE_STRING &&
                        manifest.stringAt(name.typedValueData) == CA_BUNDLE_METADATA
                }) { "Shell template has no custom CA bundle resource" }
            val id = marker.resourceId("resource")
            val bundlePath = resources.resourcePath(id, "raw")
            val bundle = buildString {
                for (certificate in config.customCaCertificates) {
                    val certificates = NetworkTrustStorage.parseCertificates(File(certificate.filePath).readBytes())
                    for (x509 in certificates) {
                        appendLine("-----BEGIN CERTIFICATE-----")
                        appendLine(Base64.toBase64String(x509.encoded).chunked(64).joinToString("\n"))
                        appendLine("-----END CERTIFICATE-----")
                    }
                }
            }
            replacements[bundlePath] = bundle.toByteArray(Charsets.US_ASCII)
            id
        }
        replacements[configPath] = axml.buildNetworkSecurityConfig(
            trustSystemCa = config.trustSystemCa,
            trustUserCa = trustUserCa,
            cleartextTrafficPermitted = config.cleartextTrafficPermitted,
            customCaResourceId = bundleId
        )
        for (path in replacements.keys) {
            check(template.getEntry(path)?.isDirectory == false) { "Missing shell template resource: $path" }
        }
        return replacements
    }

    private fun ZipFile.readEntry(path: String): ByteArray =
        getInputStream(checkNotNull(getEntry(path)) { "Missing shell template entry: $path" })
            .use { it.readBytes() }

    private fun AxmlReader.Element.attribute(name: String): AxmlReader.Attribute? =
        attributes.singleOrNull { it.namespaceUri == ANDROID_NS && it.name == name }

    private fun AxmlReader.Element.resourceId(name: String): Int {
        val reference = checkNotNull(attribute(name)) { "Missing android:$name on ${this.name}" }
        check(reference.typedValueType == AxmlConstants.TYPE_REFERENCE && reference.typedValueData != 0) {
            "android:$name must reference a shell template resource"
        }
        return reference.typedValueData
    }

    private fun ArscResourceTable.resourcePath(id: Int, resourceType: String): String {
        val pkg = packages.single { it.id == id ushr 24 }
        val typeId = (id ushr 16) and 0xff
        val entryIndex = id and 0xffff
        check(pkg.typeNames.getOrNull(typeId - 1) == resourceType) {
            "Shell template resource must be of type $resourceType"
        }
        val paths = pkg.types.filter { it.typeId == typeId }
            .mapNotNull { type -> type.entries.firstOrNull { it?.index == entryIndex } }
            .map { entry ->
                val value = (entry.body as ArscEntryBody.Simple).value
                check(value.dataType == AxmlConstants.TYPE_STRING)
                valueStringPool[value.data]
            }
            .distinct()
        return paths.single().also { path ->
            check(path.startsWith("res/") && path.split('/').none { it == "." || it == ".." }) {
                "Invalid shell template resource path: $path"
            }
        }
    }
}
