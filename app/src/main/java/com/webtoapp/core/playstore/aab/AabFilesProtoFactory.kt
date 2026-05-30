package com.webtoapp.core.playstore.aab

import com.android.bundle.Files
import com.android.bundle.Targeting

internal object AabFilesProtoFactory {

    fun buildNativeLibraries(abis: Collection<String>): Files.NativeLibraries {
        val builder = Files.NativeLibraries.newBuilder()
        for (abi in abis.distinct().sorted()) {
            builder.addDirectory(
                Files.TargetedNativeDirectory.newBuilder()
                    .setPath("lib/$abi")
                    .setTargeting(
                        Targeting.NativeDirectoryTargeting.newBuilder()
                            .setAbi(abiOf(abi))
                            .build()
                    )
                    .build()
            )
        }
        return builder.build()
    }

    private fun abiOf(abi: String): Targeting.Abi {
        val alias = when (abi.lowercase()) {
            "armeabi" -> Targeting.Abi.AbiAlias.ARMEABI
            "armeabi-v7a" -> Targeting.Abi.AbiAlias.ARMEABI_V7A
            "arm64-v8a" -> Targeting.Abi.AbiAlias.ARM64_V8A
            "x86" -> Targeting.Abi.AbiAlias.X86
            "x86_64" -> Targeting.Abi.AbiAlias.X86_64
            "mips" -> Targeting.Abi.AbiAlias.MIPS
            "mips64" -> Targeting.Abi.AbiAlias.MIPS64
            "riscv64" -> Targeting.Abi.AbiAlias.RISCV64
            else -> Targeting.Abi.AbiAlias.UNSPECIFIED_CPU_ARCHITECTURE
        }
        return Targeting.Abi.newBuilder().setAlias(alias).build()
    }

    fun buildAssets(assetDirectories: Collection<String>): Files.Assets {
        val builder = Files.Assets.newBuilder()
        for (dir in assetDirectories.distinct().sorted()) {
            builder.addDirectory(
                Files.TargetedAssetsDirectory.newBuilder()
                    .setPath(dir)

                    .setTargeting(Targeting.AssetsDirectoryTargeting.getDefaultInstance())
                    .build()
            )
        }
        return builder.build()
    }
}
