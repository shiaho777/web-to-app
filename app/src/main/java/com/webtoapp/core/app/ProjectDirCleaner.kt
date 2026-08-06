package com.webtoapp.core.app

import android.content.Context
import com.webtoapp.core.golang.GoRuntime
import com.webtoapp.core.nodejs.NodeRuntime
import com.webtoapp.core.php.PhpAppRuntime
import com.webtoapp.core.python.PythonRuntime
import com.webtoapp.core.wordpress.WordPressManager
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.WebApp
import com.webtoapp.core.logging.AppLogger
import java.io.File

/**
 * Deletes the on-disk project directory owned by a [WebApp] when the app is removed.
 *
 * Each runtime-backed app type stores its source files under `filesDir/<root>/<projectId>`
 * (e.g. `wordpress_projects/<id>`, `nodejs_projects/<id>`). [MainViewModel.deleteApp] previously
 * only dropped the database row, leaving these directories as orphans that accumulated storage
 * (notably the WordPress SQLite DB + wp-content). This resolves the orphaned-project bug reported
 * when deleting a WordPress app left its files behind.
 *
 * Exported artifacts (built_apks/, built_aabs/) are intentionally NOT touched — those are the
 * user's deliverables and independent of the source project.
 *
 * Safe by design: it only deletes directories it resolves through the canonical runtime helpers
 * (or, for HTML, the stored projectId / absolute projectDir), and only when they actually live
 * under the app's private filesDir. It never follows an arbitrary absolute path outside the
 * sandbox, so a corrupted projectDir field cannot cause data loss elsewhere.
 */
object ProjectDirCleaner {

    private const val TAG = "ProjectDirCleaner"

    /** Returns the list of directories that were deleted (empty if nothing applied). */
    fun deleteForApp(context: Context, app: WebApp): List<File> {
        val appContext = context.applicationContext
        val sandboxRoot = appContext.filesDir.canonicalFile
        val deleted = mutableListOf<File>()

        fun deleteIfSandboxed(dir: File) {
            try {
                if (!dir.exists()) return
                val canonical = dir.canonicalFile
                // Guard: only delete inside our own filesDir. A tampered/absolute projectDir must
                // never let us wipe an arbitrary location.
                if (!canonical.path.startsWith(sandboxRoot.path)) {
                    AppLogger.w(TAG, "Refusing to delete dir outside filesDir: $canonical")
                    return
                }
                if (dir.deleteRecursively()) {
                    deleted += canonical
                    AppLogger.i(TAG, "Deleted project dir: $canonical")
                } else {
                    AppLogger.w(TAG, "Failed to delete project dir: $canonical")
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error deleting project dir $dir", e)
            }
        }

        when (app.appType) {
            AppType.WORDPRESS -> {
                app.wordpressConfig?.projectId?.takeIf { it.isNotBlank() }?.let { pid ->
                    deleteIfSandboxed(WordPressManager.getProjectDir(appContext, pid))
                }
            }
            AppType.NODEJS_APP -> {
                app.nodejsConfig?.projectId?.takeIf { it.isNotBlank() }?.let { pid ->
                    deleteIfSandboxed(NodeRuntime(appContext).getProjectDir(pid))
                }
            }
            AppType.PHP_APP -> {
                app.phpAppConfig?.projectId?.takeIf { it.isNotBlank() }?.let { pid ->
                    deleteIfSandboxed(PhpAppRuntime(appContext).getProjectDir(pid))
                }
            }
            AppType.PYTHON_APP -> {
                app.pythonAppConfig?.projectId?.takeIf { it.isNotBlank() }?.let { pid ->
                    deleteIfSandboxed(PythonRuntime(appContext).getProjectDir(pid))
                }
            }
            AppType.GO_APP -> {
                app.goAppConfig?.projectId?.takeIf { it.isNotBlank() }?.let { pid ->
                    deleteIfSandboxed(GoRuntime(appContext).getProjectDir(pid))
                }
            }
            AppType.FRONTEND -> {
                // Frontend projects live under frontend_builds/<projectId>.
                app.htmlConfig?.projectId?.takeIf { it.isNotBlank() }?.let { pid ->
                    deleteIfSandboxed(File(appContext.filesDir, "frontend_builds/$pid"))
                }
            }
            AppType.HTML, AppType.MULTI_WEB -> {
                // HTML projects: prefer the stored absolute projectDir (imported HTML), fall back
                // to html_projects/<projectId>. MULTI_WEB reuses the html_projects storage.
                val cfg = app.htmlConfig
                val pid = cfg?.projectId?.takeIf { it.isNotBlank() }
                val importedDir = cfg?.projectDir?.takeIf { it.isNotBlank() }?.let(::File)
                if (importedDir != null) {
                    deleteIfSandboxed(importedDir)
                } else if (pid != null) {
                    deleteIfSandboxed(File(appContext.filesDir, "html_projects/$pid"))
                }
            }
            // WEB / IMAGE / VIDEO / GALLERY have no source project directory on disk.
            AppType.WEB, AppType.IMAGE, AppType.VIDEO, AppType.GALLERY -> { }
        }

        return deleted
    }
}
