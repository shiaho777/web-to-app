package com.webtoapp.ui.screens

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BuildStartGateTest {

    @Test
    fun `a tap while the builder or runtime is still opening is remembered`() {
        assertThat(
            buildStartAction(
                isBuilding = false,
                builderReady = false,
                runtimeEnsureInProgress = false
            )
        ).isEqualTo(BuildStartAction.Remember)
        assertThat(
            buildStartAction(
                isBuilding = false,
                builderReady = true,
                runtimeEnsureInProgress = true
            )
        ).isEqualTo(BuildStartAction.Remember)
    }

    @Test
    fun `a tap starts immediately only when the builder is ready and nothing else is in flight`() {
        assertThat(
            buildStartAction(
                isBuilding = false,
                builderReady = true,
                runtimeEnsureInProgress = false
            )
        ).isEqualTo(BuildStartAction.Start)
        assertThat(
            buildStartAction(
                isBuilding = true,
                builderReady = true,
                runtimeEnsureInProgress = false
            )
        ).isEqualTo(BuildStartAction.IgnoreAlreadyBuilding)
    }

    @Test
    fun `a remembered tap waits out the runtime download then starts`() {
        assertThat(
            armedBuildStep(
                builderReady = false,
                runtimeEnsureInProgress = true,
                builderFailed = false
            )
        ).isEqualTo(ArmedBuild.Wait)
        assertThat(
            armedBuildStep(
                builderReady = true,
                runtimeEnsureInProgress = true,
                builderFailed = false
            )
        ).isEqualTo(ArmedBuild.Wait)
        assertThat(
            armedBuildStep(
                builderReady = true,
                runtimeEnsureInProgress = false,
                builderFailed = false
            )
        ).isEqualTo(ArmedBuild.Start)
    }

    @Test
    fun `a remembered tap fails when the builder never opens`() {
        assertThat(
            armedBuildStep(
                builderReady = false,
                runtimeEnsureInProgress = false,
                builderFailed = true
            )
        ).isEqualTo(ArmedBuild.Fail)
    }
}
