package com.webtoapp.ui.screens

/**
 * What a tap on "start build" should do.
 *
 * The button is on screen before [com.webtoapp.core.apkbuilder.ApkBuilder] exists
 * (its constructor opens the signing key after the navigation animation) and while
 * a runtime download is still running. Those taps used to return or land on a
 * disabled button, so the first press did nothing and only the next one built.
 */
internal enum class BuildStartAction {
    Start,
    Remember,
    IgnoreAlreadyBuilding,
}

internal fun buildStartAction(
    isBuilding: Boolean,
    builderReady: Boolean,
    runtimeEnsureInProgress: Boolean,
): BuildStartAction = when {
    isBuilding -> BuildStartAction.IgnoreAlreadyBuilding
    !builderReady || runtimeEnsureInProgress -> BuildStartAction.Remember
    else -> BuildStartAction.Start
}

/** What to do with a tap that was remembered until the builder and runtime were ready. */
internal enum class ArmedBuild {
    Wait,
    Start,
    Fail,
}

internal fun armedBuildStep(
    builderReady: Boolean,
    runtimeEnsureInProgress: Boolean,
    builderFailed: Boolean,
): ArmedBuild = when {
    builderReady && !runtimeEnsureInProgress -> ArmedBuild.Start
    builderFailed && !builderReady -> ArmedBuild.Fail
    else -> ArmedBuild.Wait
}
