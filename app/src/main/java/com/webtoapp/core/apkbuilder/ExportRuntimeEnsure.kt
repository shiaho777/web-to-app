package com.webtoapp.core.apkbuilder

import android.content.Context
import com.webtoapp.core.nodejs.NodeDependencyManager
import com.webtoapp.core.python.PythonDependencyManager
import com.webtoapp.core.wordpress.WordPressDependencyManager
import com.webtoapp.data.model.AppType

object ExportRuntimeEnsure {

    suspend fun ensure(context: Context, appType: AppType): Boolean {
        return when (appType) {
            AppType.PYTHON_APP -> {
                if (PythonDependencyManager.isPythonReady(context)) true
                else PythonDependencyManager.downloadPythonRuntime(context)
            }
            AppType.NODEJS_APP -> {
                if (NodeDependencyManager.isNodeReady(context)) true
                else NodeDependencyManager.downloadNodeRuntime(context)
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
