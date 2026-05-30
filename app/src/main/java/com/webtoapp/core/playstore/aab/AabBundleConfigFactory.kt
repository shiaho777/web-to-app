package com.webtoapp.core.playstore.aab

import com.android.bundle.Config

internal object AabBundleConfigFactory {

    private const val BUNDLETOOL_VERSION = "1.18.1"

    fun build(): Config.BundleConfig {
        return Config.BundleConfig.newBuilder()
            .setBundletool(
                Config.Bundletool.newBuilder()
                    .setVersion(BUNDLETOOL_VERSION)
                    .build()
            )
            .setType(Config.BundleConfig.BundleType.REGULAR)
            .setOptimizations(buildOptimizations())
            .setCompression(buildCompression())
            .build()
    }

    private fun buildOptimizations(): Config.Optimizations {
        val splits = Config.SplitsConfig.newBuilder()

            .addSplitDimension(
                Config.SplitDimension.newBuilder()
                    .setValue(Config.SplitDimension.Value.ABI)
                    .build()
            )
            .addSplitDimension(
                Config.SplitDimension.newBuilder()
                    .setValue(Config.SplitDimension.Value.SCREEN_DENSITY)
                    .build()
            )
            .addSplitDimension(
                Config.SplitDimension.newBuilder()
                    .setValue(Config.SplitDimension.Value.LANGUAGE)
                    .build()
            )
            .build()

        return Config.Optimizations.newBuilder()
            .setSplitsConfig(splits)
            .setUncompressNativeLibraries(

                Config.UncompressNativeLibraries.newBuilder()
                    .setEnabled(true)
                    .setAlignment(Config.UncompressNativeLibraries.PageAlignment.PAGE_ALIGNMENT_16K)
                    .build()
            )
            .setUncompressDexFiles(

                Config.UncompressDexFiles.newBuilder()
                    .setEnabled(false)
                    .build()
            )
            .build()
    }

    private fun buildCompression(): Config.Compression {

        return Config.Compression.newBuilder()
            .addUncompressedGlob("**.3g2")
            .addUncompressedGlob("**.3gp")
            .addUncompressedGlob("**.3gpp")
            .addUncompressedGlob("**.3gpp2")
            .addUncompressedGlob("**.aac")
            .addUncompressedGlob("**.amr")
            .addUncompressedGlob("**.awb")
            .addUncompressedGlob("**.git")
            .addUncompressedGlob("**.gif")
            .addUncompressedGlob("**.imy")
            .addUncompressedGlob("**.jet")
            .addUncompressedGlob("**.jpeg")
            .addUncompressedGlob("**.jpg")
            .addUncompressedGlob("**.m4a")
            .addUncompressedGlob("**.m4v")
            .addUncompressedGlob("**.mid")
            .addUncompressedGlob("**.midi")
            .addUncompressedGlob("**.mkv")
            .addUncompressedGlob("**.mp2")
            .addUncompressedGlob("**.mp3")
            .addUncompressedGlob("**.mp4")
            .addUncompressedGlob("**.mpg")
            .addUncompressedGlob("**.mpeg")
            .addUncompressedGlob("**.ogg")
            .addUncompressedGlob("**.opus")
            .addUncompressedGlob("**.png")
            .addUncompressedGlob("**.rtttl")
            .addUncompressedGlob("**.smf")
            .addUncompressedGlob("**.tflite")
            .addUncompressedGlob("**.wav")
            .addUncompressedGlob("**.webm")
            .addUncompressedGlob("**.webp")
            .addUncompressedGlob("**.wma")
            .addUncompressedGlob("**.wmv")
            .addUncompressedGlob("**.xmf")
            .build()
    }
}
