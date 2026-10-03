package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Guards [AppType.requiresProcessExec] — the single source of truth for "server-runtime app
 * type". These types must keep targetSdk <= 28 (targetSdk >= 29 enforces W^X and breaks the
 * fork+exec of their bundled binaries), and the Play policy checker blocks them from AAB export.
 */
class AppTypeRequiresProcessExecTest {

    @Test
    fun `server runtime types require process exec`() {
        assertThat(AppType.NODEJS_APP.requiresProcessExec).isTrue()
        assertThat(AppType.PHP_APP.requiresProcessExec).isTrue()
        assertThat(AppType.PYTHON_APP.requiresProcessExec).isTrue()
        assertThat(AppType.GO_APP.requiresProcessExec).isTrue()
        assertThat(AppType.WORDPRESS.requiresProcessExec).isTrue()
    }

    @Test
    fun `webview-only types do not require process exec`() {
        // The types that are free to raise targetSdk for sideload / third-party distribution.
        assertThat(AppType.WEB.requiresProcessExec).isFalse()
        assertThat(AppType.IMAGE.requiresProcessExec).isFalse()
        assertThat(AppType.VIDEO.requiresProcessExec).isFalse()
        assertThat(AppType.HTML.requiresProcessExec).isFalse()
        assertThat(AppType.GALLERY.requiresProcessExec).isFalse()
        assertThat(AppType.FRONTEND.requiresProcessExec).isFalse()
        assertThat(AppType.MULTI_WEB.requiresProcessExec).isFalse()
    }

    @Test
    fun `requires_process_exec set matches predicate for every app type`() {
        AppType.entries.forEach { type ->
            val expected = type in AppType.REQUIRES_PROCESS_EXEC
            assertThat(type.requiresProcessExec).isEqualTo(expected)
        }
    }

    @Test
    fun `requires_process_exec set is exactly the five server runtimes`() {
        assertThat(AppType.REQUIRES_PROCESS_EXEC).containsExactly(
            AppType.NODEJS_APP,
            AppType.PHP_APP,
            AppType.PYTHON_APP,
            AppType.GO_APP,
            AppType.WORDPRESS
        )
    }
}
