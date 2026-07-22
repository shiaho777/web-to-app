package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import com.webtoapp.core.shell.ShellConfig
import com.webtoapp.data.model.AppType
import com.webtoapp.data.model.NodeJsBuildMode
import com.webtoapp.data.model.NodeJsConfig
import com.webtoapp.data.model.WebApp
import com.webtoapp.util.GsonProvider
import org.junit.Test

class NodeJsBuildModeTargetUrlTest {

    private fun roundTrip(app: WebApp): ShellConfig {
        val apk = app.toApkConfig("com.example.test")
        return GsonProvider.gson.fromJson(ApkConfigJsonFactory.toShellConfigJson(apk), ShellConfig::class.java)!!
    }

    private fun nodeApp(buildMode: NodeJsBuildMode): WebApp = WebApp(
        name = "n",
        url = "https://n.example.com",
        appType = AppType.NODEJS_APP,
        nodejsConfig = NodeJsConfig(buildMode = buildMode, entryFile = "server.js", serverPort = 3000)
    )

    @Test
    fun `STATIC build mode rewrites targetUrl to nodejs scheme`() {
        val shell = roundTrip(nodeApp(NodeJsBuildMode.STATIC))
        assertThat(shell.targetUrl).isEqualTo("nodejs://localhost")
        assertThat(shell.nodejsConfig.mode).isEqualTo("STATIC")
    }

    @Test
    fun `API_BACKEND build mode rewrites targetUrl to nodejs scheme`() {
        val shell = roundTrip(nodeApp(NodeJsBuildMode.API_BACKEND))
        assertThat(shell.targetUrl).isEqualTo("nodejs://localhost")
        assertThat(shell.nodejsConfig.mode).isEqualTo("API_BACKEND")
    }

    @Test
    fun `FULLSTACK build mode rewrites targetUrl to nodejs scheme`() {
        val shell = roundTrip(nodeApp(NodeJsBuildMode.FULLSTACK))
        assertThat(shell.targetUrl).isEqualTo("nodejs://localhost")
        assertThat(shell.nodejsConfig.mode).isEqualTo("FULLSTACK")
    }

    @Test
    fun `SSR build mode rewrites targetUrl to nodejs scheme`() {
        val shell = roundTrip(nodeApp(NodeJsBuildMode.SSR))
        assertThat(shell.targetUrl).isEqualTo("nodejs://localhost")
        assertThat(shell.nodejsConfig.mode).isEqualTo("SSR")
    }
}
