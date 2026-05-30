package com.webtoapp.core.playstore.aab

import android.content.Context
import com.webtoapp.core.logging.AppLogger
import java.io.File

class AabExporter(private val context: Context) {

    companion object {
        private const val TAG = "AabExporter"
    }

    enum class Stage {
        STARTING,
        ASSEMBLING,
        ASSEMBLED,
        SIGNING,
        SIGNED;
    }

    data class Result(
        val signedAab: File,
        val assembleStats: ApkToAabAssembler.AssembleStats
    )

    fun export(
        sourceApk: File,
        outputAab: File,
        targetSdkOverride: Int? = null,
        onProgress: ((stage: Stage, percent: Int) -> Unit)? = null
    ): Result {
        require(sourceApk.exists()) {
            "Input APK not found: ${sourceApk.absolutePath}"
        }
        outputAab.parentFile?.mkdirs()

        val unsignedAab = File(context.cacheDir, "aab_export_unsigned_${System.currentTimeMillis()}.aab")
        if (unsignedAab.exists()) unsignedAab.delete()

        try {

            onProgress?.invoke(Stage.ASSEMBLING, 10)
            AppLogger.d(TAG, "Stage 1: assembling unsigned AAB from ${sourceApk.name}")

            val stats = try {
                ApkToAabAssembler().assemble(sourceApk, unsignedAab, targetSdkOverride)
            } catch (e: Exception) {
                throw AabExportException(
                    failureStage = FailureStage.ASSEMBLE,
                    message = "Failed to assemble AAB: ${e.message}",
                    cause = e
                )
            }
            AppLogger.d(TAG, "Stage 1 done: $stats")
            onProgress?.invoke(Stage.ASSEMBLED, 70)

            onProgress?.invoke(Stage.SIGNING, 75)
            AppLogger.d(TAG, "Stage 2: signing AAB")

            val signed = try {
                AabSigner(context).sign(unsignedAab, outputAab)
            } catch (e: Exception) {
                throw AabExportException(
                    failureStage = FailureStage.SIGN,
                    message = "Failed to sign AAB: ${e.message}",
                    cause = e
                )
            }
            if (!signed) {
                throw AabExportException(
                    failureStage = FailureStage.SIGN,
                    message = "AAB signer returned false (see log for details)"
                )
            }
            onProgress?.invoke(Stage.SIGNED, 100)

            return Result(signedAab = outputAab, assembleStats = stats)
        } finally {

            if (unsignedAab.exists()) {
                try {
                    unsignedAab.delete()
                } catch (e: Exception) {
                    AppLogger.w(TAG, "Failed to delete unsigned AAB temp", e)
                }
            }
        }
    }
}

enum class FailureStage {
    ASSEMBLE,
    SIGN,

    UNKNOWN,
}

class AabExportException(
    val failureStage: FailureStage,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
