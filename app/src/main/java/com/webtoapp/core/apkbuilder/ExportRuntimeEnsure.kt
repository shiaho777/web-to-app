package com.webtoapp.core.apkbuilder

import android.content.Context
import com.webtoapp.core.nodejs.NodeDependencyManager
import com.webtoapp.core.python.PythonDependencyManager
import com.webtoapp.core.wordpress.WordPressDependencyManager
import com.webtoapp.data.model.AppType

object ExportRuntimeEnsure {

    fun needsEnsure(
        context: Context,
        appType: AppType,
        needsCronet: Boolean = false,
        neededAbis: Collection<String>? = null
    ): Boolean {
        if (needsCronet && !com.webtoapp.core.webview.CronetDependencyManager.isCronetReady(context)) {
            return true
        }
        return when (appType) {
            AppType.PYTHON_APP -> !PythonDependencyManager.isPythonReady(context)
            AppType.NODEJS_APP -> !NodeDependencyManager.isNodeReady(context) ||
                (neededAbis != null &&
                    NodeDependencyManager.missingExportAbis(context, neededAbis).isNotEmpty())
            AppType.PHP_APP -> !WordPressDependencyManager.isPhpReady(context)
            AppType.WORDPRESS -> {
                !WordPressDependencyManager.isPhpReady(context) ||
                    !WordPressDependencyManager.isWordPressReady(context)
            }
            else -> false
        }
    }

    suspend fun ensure(
        context: Context,
        appType: AppType,
        needsCronet: Boolean = false,
        neededAbis: Collection<String>? = null
    ): Boolean {
        if (needsCronet && !com.webtoapp.core.webview.CronetDependencyManager.isCronetReady(context)) {
            if (!com.webtoapp.core.webview.CronetDependencyManager.downloadCronetRuntime(context)) {
                return false
            }
        }
        return when (appType) {
            AppType.PYTHON_APP -> {
                if (PythonDependencyManager.isPythonReady(context)) true
                else PythonDependencyManager.downloadPythonRuntime(context)
            }
            AppType.NODEJS_APP -> {
                val ready = if (NodeDependencyManager.isNodeReady(context)) true
                    else NodeDependencyManager.downloadNodeRuntime(context)
                // Multi-ABI export: the runtime zip ships arm64/arm32/x86_64, but installs
                // predating per-ABI extraction only cached the device ABI — re-download
                // once so the export can embed libnode.so for every selected ABI.
                if (!ready) false
                else if (neededAbis == null) true
                else NodeDependencyManager.ensureExportAbis(context, neededAbis).isEmpty()
            }
            AppType.PHP_APP -> {
                if (WordPressDependencyManager.isPhpReady(context)) true
                else WordPressDependencyManager.downloadPhpDependency(context)
            }
            AppType.WORDPRESS -> {
                if (
                    WordPressDependencyManager.isPhpReady(context) &&
                    WordPressDependencyManager.isWordPressReady(context)
                ) {
                    true
                } else {
                    WordPressDependencyManager.downloadAllDependencies(context)
                }
            }
            else -> true
        }
    }
}
