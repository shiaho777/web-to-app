package com.webtoapp.core.linux

import android.content.Context

/**
 * SELinux W^X policy gate for on-device fork+exec runtimes.
 *
 * Apps targeting SDK 29+ lose the ability to exec (or mmap PROT_EXEC) files in app
 * data storage — which is exactly where the on-demand server runtimes (Python / PHP /
 * WordPress) are downloaded and extracted. Binaries shipped as APK native libs
 * (Node.js via the JNI bridge) and memfd-exec loaders (Go) are not affected.
 *
 * Generated APKs keep targetSdk 28, so this only ever gates the *host preview*
 * runtimes; inside a generated app the probe always passes.
 */
object RuntimeExecPolicy {

    fun canExecAppDataBinaries(context: Context): Boolean {
        val target = try {
            context.applicationInfo.targetSdkVersion
        } catch (_: Exception) {
            28
        }
        return target < 29
    }

    /**
     * Runtime-layer suffix appended to launch failures under the restriction.
     * (shell-synced runtime string; intentionally not routed through Strings i18n)
     */
    fun restrictionNote(): String =
        " [受 targetSdk≥29 SELinux 限制，无法执行应用数据目录中的本地运行时]"

    fun hostPreviewBlockedMessage(runtimeName: String): String =
        "当前构建 targetSdk≥29，系统安全策略禁止从应用数据目录启动 $runtimeName 运行时，本地服务器预览不可用"
}
