package com.webtoapp.core.feature

data class FeatureManifest(
    val id: String,
    val version: Int = 1,
    val minLiteApi: Int = LITE_FEATURE_API,
    val dependsOn: List<String> = emptyList(),
    val entryClass: String,
    val loadOrder: Int = 100,
    val dex: List<String> = listOf("classes.dex"),
    val nativeLibs: List<String> = emptyList(),
    val manifestDelta: String? = null,
    val assetsPrefix: String? = null
)

data class EnabledFeature(
    val id: String,
    val version: Int = 1,
    val dir: String
)

data class EnabledFeaturesFile(
    val liteApi: Int = LITE_FEATURE_API,
    val features: List<EnabledFeature> = emptyList()
)
