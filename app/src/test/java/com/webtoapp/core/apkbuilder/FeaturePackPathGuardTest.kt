package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test

class FeaturePackPathGuardTest {
    @Test
    fun mergerStagesPacksUnderAssetsPrefix() {
        val file = resolveFile(
            "src/main/java/com/webtoapp/core/apkbuilder/FeaturePackMerger.kt",
            "app/src/main/java/com/webtoapp/core/apkbuilder/FeaturePackMerger.kt"
        )
        val text = file.readText()
        assertThat(text).contains("assets/\$assetDir/")
        assertThat(text).contains("val assetDir = \"features/\$id\"")
        assertThat(text).doesNotContain("extra += \"\$assetDir/\$rel\"")
    }

    private fun resolveFile(vararg candidates: String): File {
        for (c in candidates) {
            val f = File(c)
            if (f.isFile) return f
            val fromModule = File("../$c")
            if (fromModule.isFile) return fromModule
        }
        return File(candidates.first())
    }
}
