package com.webtoapp.core.apkbuilder

/**
 * SDK levels a generated APK may declare in `<uses-sdk>`.
 *
 * The shell template is compiled at minSdk 23 / targetSdk 35 / compileSdk 36.
 * Export rewrites the packed manifest rather than rebuilding the template.
 * minSdk may only be raised: the DEX and AndroidX set require API 23, so a
 * lower manifest value would install on devices that then crash at launch.
 */
object GeneratedSdkLevels {
    const val TEMPLATE_MIN_SDK = 23
    const val TEMPLATE_TARGET_SDK = 35
    const val COMPILE_SDK_CAP = 36
    const val PROCESS_EXEC_TARGET_SDK = 28

    /** First raise that actually changes install/signing behavior (V1 optional). */
    const val DEFAULT_RAISED_MIN_SDK = 24

    /** Sideload chips. Play AAB export still forces the Play-required level. */
    val TARGET_SDK_CHOICES = listOf(28, 31, 33, 34, 35, 36)

    /** Raise-only floors. The UI drops values above the effective targetSdk. */
    val MIN_SDK_CHOICES = listOf(23, 24, 26, 28, 31, 33, 34, 35, 36)

    data class Resolved(
        val minSdk: Int,
        val targetSdk: Int,
        /** Non-null when the packed manifest should rewrite `targetSdkVersion`. */
        val targetSdkRewrite: Int?,
        /** Non-null when the packed manifest should rewrite `minSdkVersion`. */
        val minSdkRewrite: Int?
    )

    fun resolve(
        requiresProcessExec: Boolean,
        minSdkOverride: Int?,
        targetSdkOverride: Int?
    ): Resolved {
        val targetRaw = if (requiresProcessExec) {
            PROCESS_EXEC_TARGET_SDK
        } else {
            targetSdkOverride?.takeIf { it > 0 }
        }
        val target = (targetRaw ?: TEMPLATE_TARGET_SDK)
            .coerceIn(TEMPLATE_MIN_SDK, COMPILE_SDK_CAP)

        val minRequested = minSdkOverride?.takeIf { it > 0 }
        val min = (minRequested ?: TEMPLATE_MIN_SDK)
            .coerceIn(TEMPLATE_MIN_SDK, target)

        val targetRewrite = when {
            requiresProcessExec -> PROCESS_EXEC_TARGET_SDK
            targetSdkOverride != null && targetSdkOverride > 0 -> target
            else -> null
        }
        val minRewrite = if (minRequested != null) min else null
        return Resolved(
            minSdk = min,
            targetSdk = target,
            targetSdkRewrite = targetRewrite,
            minSdkRewrite = minRewrite
        )
    }

    fun defaultRaisedMinSdk(effectiveTargetSdk: Int): Int =
        minOf(DEFAULT_RAISED_MIN_SDK, effectiveTargetSdk).coerceAtLeast(TEMPLATE_MIN_SDK)

    fun clampMinSdk(minSdk: Int?, effectiveTargetSdk: Int): Int? {
        val requested = minSdk?.takeIf { it > 0 } ?: return minSdk
        return requested.coerceIn(TEMPLATE_MIN_SDK, effectiveTargetSdk)
    }
}
