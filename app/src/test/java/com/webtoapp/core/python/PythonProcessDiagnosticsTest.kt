package com.webtoapp.core.python

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PythonProcessDiagnosticsTest {

    @Test
    fun `wrapper-reported signal deaths decode to their signal`() {
        assertThat(PythonProcessDiagnostics.signalForExitCode(159)).isEqualTo(PythonProcessDiagnostics.SIGSYS)
        assertThat(PythonProcessDiagnostics.signalForExitCode(137)).isEqualTo(PythonProcessDiagnostics.SIGKILL)
        assertThat(PythonProcessDiagnostics.signalForExitCode(139)).isEqualTo(PythonProcessDiagnostics.SIGSEGV)
        assertThat(PythonProcessDiagnostics.signalForExitCode(134)).isEqualTo(PythonProcessDiagnostics.SIGABRT)
    }

    @Test
    fun `normal and negative exit codes carry no signal`() {
        assertThat(PythonProcessDiagnostics.signalForExitCode(0)).isNull()
        assertThat(PythonProcessDiagnostics.signalForExitCode(1)).isNull()
        assertThat(PythonProcessDiagnostics.signalForExitCode(127)).isNull()
        assertThat(PythonProcessDiagnostics.signalForExitCode(-1)).isNull()
    }

    @Test
    fun `signal names resolve for the common signals`() {
        assertThat(PythonProcessDiagnostics.signalName(31)).isEqualTo("SIGSYS")
        assertThat(PythonProcessDiagnostics.signalName(9)).isEqualTo("SIGKILL")
        assertThat(PythonProcessDiagnostics.signalName(11)).isEqualTo("SIGSEGV")
        assertThat(PythonProcessDiagnostics.signalName(99)).isEqualTo("SIGNAL_99")
    }

    @Test
    fun `seccomp kill is a deterministic environment failure`() {
        assertThat(PythonProcessDiagnostics.isDeterministicEnvironmentFailure(159)).isTrue()
        // Other signal deaths and ordinary failures are not classified as deterministic.
        assertThat(PythonProcessDiagnostics.isDeterministicEnvironmentFailure(137)).isFalse()
        assertThat(PythonProcessDiagnostics.isDeterministicEnvironmentFailure(1)).isFalse()
        assertThat(PythonProcessDiagnostics.isDeterministicEnvironmentFailure(-1)).isFalse()
    }
}
