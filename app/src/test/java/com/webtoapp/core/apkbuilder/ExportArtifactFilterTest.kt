package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExportArtifactFilterTest {

    @Test
    fun `node launcher only ships with node apps`() {
        assertThat(requiredNativeLib("libnode_launcher.so", "NODEJS_APP")).isTrue()
        for (appType in listOf("WEB", "HTML", "FRONTEND", "GALLERY", "GO_APP", "PHP_APP", "PYTHON_APP", "WORDPRESS", "MULTI_WEB")) {
            assertThat(requiredNativeLib("libnode_launcher.so", appType)).isFalse()
        }
    }

    @Test
    fun `runtime native libraries are limited to their owning app type`() {
        assertThat(requiredNativeLib("libphp.so", "PHP_APP")).isTrue()
        assertThat(requiredNativeLib("libphp.so", "WORDPRESS")).isTrue()
        assertThat(requiredNativeLib("libphp.so", "WEB")).isFalse()
        assertThat(requiredNativeLib("libgo_exec_loader.so", "GO_APP")).isTrue()
        assertThat(requiredNativeLib("libgo_exec_loader.so", "WEB")).isFalse()
        assertThat(requiredNativeLib("libnode_bridge.so", "NODEJS_APP")).isTrue()
        assertThat(requiredNativeLib("libnode_bridge.so", "WEB")).isFalse()
        assertThat(requiredNativeLib("libpython3.so", "PYTHON_APP")).isTrue()
        assertThat(requiredNativeLib("libpython3.so", "WEB")).isFalse()
        assertThat(requiredNativeLib("libmusl-linker.so", "WEB")).isFalse()
    }

    @Test
    fun `native libraries with no loader are always stripped`() {
        assertThat(requiredNativeLib("libperf_engine.so", "WEB")).isFalse()
        assertThat(requiredNativeLib("libbrowser_kernel.so", "NODEJS_APP")).isFalse()
        assertThat(requiredNativeLib("libcrashhelper.so", "WEB")).isFalse()
    }

    @Test
    fun `libraries with a real runtime caller stay in every app`() {
        assertThat(requiredNativeLib("libandroidx.graphics.path.so", "WEB")).isTrue()
        assertThat(requiredNativeLib("libsys_optimizer.so", "WEB")).isTrue()
    }

    @Test
    fun `gecko web extensions only ship with the gecko engine`() {
        val entry = "assets/web_extensions/wta_native_bridge/content_script.js"
        assertThat(editorOnlyAsset(entry, "WEB", "GECKOVIEW")).isFalse()
        assertThat(editorOnlyAsset(entry, "WEB", "WEBVIEW")).isTrue()
        assertThat(editorOnlyAsset("assets/web_extensions/wta_share/manifest.json", "WEB", "WEBVIEW")).isTrue()
    }

    @Test
    fun `php router only ships with php and wordpress apps`() {
        assertThat(editorOnlyAsset("assets/php_router_server.php", "PHP_APP", "WEBVIEW")).isFalse()
        assertThat(editorOnlyAsset("assets/php_router_server.php", "WORDPRESS", "WEBVIEW")).isFalse()
        assertThat(editorOnlyAsset("assets/php_router_server.php", "WEB", "WEBVIEW")).isTrue()
    }

    @Test
    fun `disguise scripts only ship when browser disguise is enabled`() {
        for (entry in listOf("res/raw/wta_disguise_surface_api_core.js", "res/PH.js", "res/0d.js")) {
            assertThat(editorOnlyAsset(entry, "WEB", "WEBVIEW", browserDisguiseEnabled = true)).isFalse()
            assertThat(editorOnlyAsset(entry, "WEB", "WEBVIEW", browserDisguiseEnabled = false)).isTrue()
        }
        assertThat(editorOnlyAsset("assets/other.js", "WEB", "WEBVIEW", browserDisguiseEnabled = false)).isFalse()
        assertThat(editorOnlyAsset("res/values/strings.xml", "WEB", "WEBVIEW", browserDisguiseEnabled = false)).isFalse()
    }

    @Test
    fun `editor and template assets never reach an exported apk`() {
        for (entry in listOf(
            "assets/template/webview_shell.apk",
            "assets/sample_projects/demo.zip",
            "assets/extensions/plugin.js",
            "assets/omni.ja",
            "assets/help/index.html",
            "assets/schemas/1.json",
            "assets/default_config.json"
        )) {
            assertThat(editorOnlyAsset(entry, "WEB", "WEBVIEW")).isTrue()
        }
    }

    @Test
    fun `runtime assets follow their app type`() {
        assertThat(editorOnlyAsset("assets/nodejs_runtime/server.js", "NODEJS_APP", "WEBVIEW")).isFalse()
        assertThat(editorOnlyAsset("assets/nodejs_runtime/server.js", "WEB", "WEBVIEW")).isTrue()
        assertThat(editorOnlyAsset("assets/go_runtime/main", "GO_APP", "WEBVIEW")).isFalse()
        assertThat(editorOnlyAsset("assets/python_runtime/app.py", "PYTHON_APP", "WEBVIEW")).isFalse()
        assertThat(editorOnlyAsset("assets/frontend_tools/esbuild", "FRONTEND", "WEBVIEW")).isFalse()
    }
}
