package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Regression guard for the GO_APP / NODEJS_APP "duplicate entry: lib/<abi>/<lib>.so" build
 * failure: the template APK ships these runtime libs AND the per-app-type injection step writes
 * them (16KB-aligned) for the device ABI. modifyApk skips the template's device-ABI copy using
 * [ApkBuilder.injectedDeviceLibEntries], so this locks the exact entry names that must be skipped
 * to avoid emitting a duplicate zip entry.
 */
class ApkBuilderInjectedLibsTest {

    @Test
    fun `gecko runtime entries replace matching template entries for every selected abi`() {
        val runtimeEntries = ApkBuilder.geckoRuntimeEntryNames(
            nativeLibNamesByAbi = mapOf(
                "arm64-v8a" to listOf("libcrashhelper.so", "libxul.so"),
                "x86_64" to listOf("libcrashhelper.so")
            ),
            abiFilters = listOf("arm64-v8a", "x86_64")
        )

        assertThat(runtimeEntries).containsExactly(
            "lib/arm64-v8a/libcrashhelper.so",
            "lib/arm64-v8a/libxul.so",
            "lib/x86_64/libcrashhelper.so",
            "assets/omni.ja"
        )
    }

    @Test
    fun `go app skips the device-abi go exec loader from the template`() {
        assertThat(ApkBuilder.injectedDeviceLibEntries("GO_APP", "arm64-v8a"))
            .containsExactly("lib/arm64-v8a/libgo_exec_loader.so")
    }

    @Test
    fun `node app skips the device-abi node libs from the template`() {
        assertThat(ApkBuilder.injectedDeviceLibEntries("NODEJS_APP", "arm64-v8a"))
            .containsExactly(
                "lib/arm64-v8a/libc++_shared.so",
                "lib/arm64-v8a/libnode_bridge.so",
                "lib/arm64-v8a/libnode.so"
            )
    }

    @Test
    fun `php and wordpress skip the device-abi php lib`() {
        assertThat(ApkBuilder.injectedDeviceLibEntries("PHP_APP", "armeabi-v7a"))
            .containsExactly("lib/armeabi-v7a/libphp.so")
        assertThat(ApkBuilder.injectedDeviceLibEntries("WORDPRESS", "armeabi-v7a"))
            .containsExactly("lib/armeabi-v7a/libphp.so")
    }

    @Test
    fun `python app skips the device-abi python libs`() {
        assertThat(ApkBuilder.injectedDeviceLibEntries("PYTHON_APP", "arm64-v8a"))
            .containsExactly("lib/arm64-v8a/libpython3.so", "lib/arm64-v8a/libmusl-linker.so")
    }

    @Test
    fun `non-runtime app types skip nothing`() {
        assertThat(ApkBuilder.injectedDeviceLibEntries("WEB", "arm64-v8a")).isEmpty()
        assertThat(ApkBuilder.injectedDeviceLibEntries("HTML", "arm64-v8a")).isEmpty()
    }

    @Test
    fun `node app skips injected libnode for every selected non-device abi`() {
        // Multi-ABI export injects libnode.so per selected ABI (upstream ships
        // arm64-v8a / armeabi-v7a / x86_64 — never 32-bit x86). Only libnode.so is
        // injected for extra ABIs: the template already carries the aligned
        // bridge/launcher/cxx libs, so those template entries must NOT be skipped.
        assertThat(
            ApkBuilder.injectedMultiAbiLibEntries(
                "NODEJS_APP", "arm64-v8a", listOf("arm64-v8a", "x86_64")
            )
        ).containsExactly("lib/x86_64/libnode.so")

        assertThat(
            ApkBuilder.injectedMultiAbiLibEntries(
                "NODEJS_APP", "arm64-v8a", emptyList() // empty = all selected
            )
        ).containsExactly("lib/armeabi-v7a/libnode.so", "lib/x86_64/libnode.so")
    }

    @Test
    fun `multi-abi injection never targets unsupported or non-node types`() {
        // x86 (32-bit) has no upstream libnode.so — nothing to inject/skip for it.
        assertThat(
            ApkBuilder.injectedMultiAbiLibEntries("NODEJS_APP", "arm64-v8a", listOf("x86"))
        ).isEmpty()
        assertThat(
            ApkBuilder.injectedMultiAbiLibEntries("PHP_APP", "arm64-v8a", emptyList())
        ).isEmpty()
        assertThat(
            ApkBuilder.injectedMultiAbiLibEntries("WEB", "arm64-v8a", emptyList())
        ).isEmpty()
    }
}
