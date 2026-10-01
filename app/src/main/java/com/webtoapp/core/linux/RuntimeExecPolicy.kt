package com.webtoapp.core.linux

import android.content.Context
import java.io.File

/**
 * SELinux W^X policy gate for on-device fork+exec tooling.
 *
 * Apps targeting SDK 29+ lose the ability to exec (or exec-map) files in app
 * data storage — which is exactly where on-demand toolchain binaries (esbuild)
 * are downloaded and extracted. execveat on memfds is blocked too
 * (no execute_no_trans on memfd labels, verified on API 35), so memfd-exec
 * loaders do not bypass the gate; [hasStaticExecBridge] rides on the user-mode
 * exec loader instead.
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
     * True when the user-mode exec loader (libstatic_exec.so) is installed as
     * a native lib. It rebuilds execve in user mode (memfd image + AArch64
     * initial stack + jump to entry), so static ELFs (pmmp PHP) can start
     * even where [canExecAppDataBinaries] is false. AArch64 devices only —
     * the loader neither parses nor trampolines any other architecture, so
     * other ABIs get the plain blocked message instead of a spawn error.
     */
    fun hasStaticExecBridge(context: Context): Boolean {
        val primaryAbi = android.os.Build.SUPPORTED_ABIS.firstOrNull() ?: return false
        if (!primaryAbi.startsWith("arm64-v8a")) return false
        val lib = File(context.applicationInfo.nativeLibraryDir, "libstatic_exec.so")
        return lib.exists() && lib.canRead()
    }

    /**
     * Runtime-layer suffix appended to launch failures under the restriction.
     * (shell-synced runtime string; intentionally not routed through Strings i18n)
     */
    fun restrictionNote(): String =
        " [受 targetSdk≥29 SELinux 限制，无法执行应用数据目录中的本地运行时]"

    fun hostPreviewBlockedMessage(runtimeName: String): String =
        "当前构建 targetSdk≥29，系统安全策略禁止从应用数据目录启动 $runtimeName，本地服务器预览不可用。" +
            "该工具链仅用于宿主侧处理，导出的 APK 不依赖它"
}
