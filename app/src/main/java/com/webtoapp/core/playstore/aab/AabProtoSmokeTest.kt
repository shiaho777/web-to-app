package com.webtoapp.core.playstore.aab

@Suppress("unused")
internal object AabProtoSmokeTest {

    fun buildSampleBundleConfig(): com.android.bundle.Config.BundleConfig {
        val bundletool = com.android.bundle.Config.Bundletool.newBuilder()
            .setVersion("1.18.1")
            .build()

        return com.android.bundle.Config.BundleConfig.newBuilder()
            .setBundletool(bundletool)
            .setType(com.android.bundle.Config.BundleConfig.BundleType.REGULAR)
            .build()
    }

    fun buildSampleManifestXmlNode(): com.android.aapt.Resources.XmlNode {
        val element = com.android.aapt.Resources.XmlElement.newBuilder()
            .setName("manifest")
            .setNamespaceUri("")
            .build()

        return com.android.aapt.Resources.XmlNode.newBuilder()
            .setElement(element)
            .build()
    }

    fun buildEmptyResourceTable(packageName: String): com.android.aapt.Resources.ResourceTable {
        val pkgId = com.android.aapt.Resources.PackageId.newBuilder()
            .setId(0x7f)
            .build()

        val pkg = com.android.aapt.Resources.Package.newBuilder()
            .setPackageId(pkgId)
            .setPackageName(packageName)
            .build()

        return com.android.aapt.Resources.ResourceTable.newBuilder()
            .addPackage(pkg)
            .build()
    }

    fun buildDefaultConfiguration(): com.android.aapt.ConfigurationOuterClass.Configuration {
        return com.android.aapt.ConfigurationOuterClass.Configuration.newBuilder()
            .build()
    }

    fun buildArm64Abi(): com.android.bundle.Targeting.Abi {
        return com.android.bundle.Targeting.Abi.newBuilder()
            .setAlias(com.android.bundle.Targeting.Abi.AbiAlias.ARM64_V8A)
            .build()
    }
}
