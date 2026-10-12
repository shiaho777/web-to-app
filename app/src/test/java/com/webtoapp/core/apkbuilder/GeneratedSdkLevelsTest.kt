package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GeneratedSdkLevelsTest {

    @Test
    fun `defaults keep the shell template levels`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = false,
            minSdkOverride = null,
            targetSdkOverride = null
        )
        assertThat(resolved.minSdk).isEqualTo(23)
        assertThat(resolved.targetSdk).isEqualTo(35)
        assertThat(resolved.minSdkRewrite).isNull()
        assertThat(resolved.targetSdkRewrite).isNull()
    }

    @Test
    fun `web type honors both overrides`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = false,
            minSdkOverride = 26,
            targetSdkOverride = 34
        )
        assertThat(resolved.minSdk).isEqualTo(26)
        assertThat(resolved.targetSdk).isEqualTo(34)
        assertThat(resolved.minSdkRewrite).isEqualTo(26)
        assertThat(resolved.targetSdkRewrite).isEqualTo(34)
    }

    @Test
    fun `minSdk below the template floor is raised to 23`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = false,
            minSdkOverride = 21,
            targetSdkOverride = 35
        )
        assertThat(resolved.minSdk).isEqualTo(23)
        assertThat(resolved.minSdkRewrite).isEqualTo(23)
    }

    @Test
    fun `minSdk above targetSdk is clamped to targetSdk`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = false,
            minSdkOverride = 36,
            targetSdkOverride = 28
        )
        assertThat(resolved.minSdk).isEqualTo(28)
        assertThat(resolved.targetSdk).isEqualTo(28)
        assertThat(resolved.minSdkRewrite).isEqualTo(28)
    }

    @Test
    fun `targetSdk above compileSdk cap is clamped to 36`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = false,
            minSdkOverride = null,
            targetSdkOverride = 40
        )
        assertThat(resolved.targetSdk).isEqualTo(36)
        assertThat(resolved.targetSdkRewrite).isEqualTo(36)
    }

    @Test
    fun `process-exec types pin targetSdk 28 and clamp minSdk`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = true,
            minSdkOverride = 34,
            targetSdkOverride = 36
        )
        assertThat(resolved.targetSdk).isEqualTo(28)
        assertThat(resolved.targetSdkRewrite).isEqualTo(28)
        assertThat(resolved.minSdk).isEqualTo(28)
        assertThat(resolved.minSdkRewrite).isEqualTo(28)
    }

    @Test
    fun `process-exec with no minSdk override leaves minSdk rewrite null`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = true,
            minSdkOverride = null,
            targetSdkOverride = 36
        )
        assertThat(resolved.minSdk).isEqualTo(23)
        assertThat(resolved.minSdkRewrite).isNull()
        assertThat(resolved.targetSdkRewrite).isEqualTo(28)
    }

    @Test
    fun `zero and negative overrides are treated as unset`() {
        val resolved = GeneratedSdkLevels.resolve(
            requiresProcessExec = false,
            minSdkOverride = 0,
            targetSdkOverride = -1
        )
        assertThat(resolved.minSdkRewrite).isNull()
        assertThat(resolved.targetSdkRewrite).isNull()
        assertThat(resolved.minSdk).isEqualTo(23)
        assertThat(resolved.targetSdk).isEqualTo(35)
    }

    @Test
    fun `default raised minSdk is 24 until targetSdk is lower`() {
        assertThat(GeneratedSdkLevels.defaultRaisedMinSdk(35)).isEqualTo(24)
        assertThat(GeneratedSdkLevels.defaultRaisedMinSdk(23)).isEqualTo(23)
    }

    @Test
    fun `clampMinSdk drops values above the cap`() {
        assertThat(GeneratedSdkLevels.clampMinSdk(34, 28)).isEqualTo(28)
        assertThat(GeneratedSdkLevels.clampMinSdk(26, 35)).isEqualTo(26)
        assertThat(GeneratedSdkLevels.clampMinSdk(null, 28)).isNull()
    }
}
