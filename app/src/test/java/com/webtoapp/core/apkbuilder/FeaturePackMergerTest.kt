package com.webtoapp.core.apkbuilder

import com.webtoapp.core.feature.FeatureIds
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class FeaturePackMergerTest {

    @Test
    fun featurePackEntriesAreWrittenUnderAssets() {
        val context = RuntimeEnvironment.getApplication()
        val packRoot = File(context.filesDir, "feature_packs/${FeatureIds.COMPAT}")
        packRoot.mkdirs()
        File(packRoot, "feature.json").writeText(
            """
            {
              "id": "${FeatureIds.COMPAT}",
              "version": 1,
              "minLiteApi": 1,
              "dependsOn": [],
              "entryClass": "com.webtoapp.feature.compat.CompatFeatureModule",
              "loadOrder": 0,
              "dex": ["classes.dex"],
              "nativeLibs": []
            }
            """.trimIndent()
        )
        File(packRoot, "classes.dex").writeBytes(byteArrayOf(0x64, 0x65, 0x78, 0x0a))

        val plan = CapabilityPlan(
            features = listOf(FeatureIds.COMPAT),
            reasons = listOf("test"),
            abiFilters = listOf("arm64-v8a"),
            liteOnly = false
        )
        val result = FeaturePackMerger.prepare(context, plan)

        assertTrue(
            "expected staged entries, missing=${result.missingFeatures}, enabled=${String(result.enabledJson)}",
            result.extraEntries.isNotEmpty()
        )
        assertTrue(result.extraEntries.all { it.first.startsWith("assets/features/${FeatureIds.COMPAT}/") })
        assertTrue(result.extraEntries.any { it.first.endsWith("feature.json") })
        assertTrue(result.extraEntries.any { it.first.endsWith("classes.dex") })
        assertTrue(result.extraEntries.none { it.first.startsWith("features/") && !it.first.startsWith("assets/") })

        val enabled = JSONObject(String(result.enabledJson, Charsets.UTF_8))
        val first = enabled.getJSONArray("features").getJSONObject(0)
        assertEquals(FeatureIds.COMPAT, first.getString("id"))
        assertEquals("features/${FeatureIds.COMPAT}", first.getString("dir"))
        assertEquals(emptyList<String>(), result.missingFeatures)
    }
}
