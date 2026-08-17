package com.webtoapp.core.linux

import android.content.Context
import java.io.File

/**
 * SELinux W^X policy gate for on-device fork+exec runtimes.
 *
 * Apps targeting SDK 29+ lose the ability to exec (or exec-map) files in app
 * data storage — which is exactly where the on-demand server runtimes (Python /
 * PHP / WordPress / Go) are downloaded and extracted. execveat on memfds is
 * blocked too (no execute_no_trans on memfd labels, verified on API 35), so
 * memfd-exec loaders do not bypass the gate; mmap PROT_EXEC on memfds stays
 * allowed (same class ART JIT uses), which is what [hasMuslExecBridge] rides on.
 *
 * Node.js host previews are unaffected (JNI native lib, no fork+exec).
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
     * True when the patched musl linker is installed as a native lib. It execs
     * from nativeLibraryDir (SELinux allows) and bridges exec-mapping of
     * app_data ELFs through executable memfds, so musl-linked runtimes
     * (Python) can start even where [canExecAppDataBinaries] is false.
     */
    fun hasMuslExecBridge(context: Context): Boolean {
        val linker = File(context.applicationInfo.nativeLibraryDir, "libmusl-linker.so")
        return linker.exists() && linker.canExecute()
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
