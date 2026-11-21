package com.webtoapp.core.python

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PythonDepsFingerprintTest {

    @get:Rule
    val tmp = TemporaryFolder()

    @Test
    fun `fingerprint is stable for identical requirements`() {
        val req = tmp.newFile("requirements.txt")
        req.writeText("flask==3.0.0\nrequests\n")
        val a = PythonDependencyManager.requirementsFingerprint(req)
        val b = PythonDependencyManager.requirementsFingerprint(req)
        assertThat(a).isEqualTo(b)
        assertThat(a).isNotNull()
        assertThat(a!!).hasLength(64)
    }

    @Test
    fun `fingerprint changes when requirements change`() {
        val req = tmp.newFile("requirements.txt")
        req.writeText("flask==3.0.0\n")
        val before = PythonDependencyManager.requirementsFingerprint(req)
        req.writeText("flask==3.0.0\nrequests\n")
        val after = PythonDependencyManager.requirementsFingerprint(req)
        assertThat(after).isNotEqualTo(before)
    }

    @Test
    fun `marker matches only after a successful write with the same fingerprint`() {
        val site = tmp.newFolder(".pypackages")
        val req = tmp.newFile("requirements.txt")
        req.writeText("flask==3.0.0\n")
        val fingerprint = requireNotNull(PythonDependencyManager.requirementsFingerprint(req))

        assertThat(PythonDependencyManager.depsMarkerMatches(site, fingerprint)).isFalse()

        PythonDependencyCache.mark(site, fingerprint)
        assertThat(PythonDependencyManager.depsMarkerMatches(site, fingerprint)).isTrue()
        assertThat(PythonDependencyManager.hasInstalledPackages(site)).isFalse()

        req.writeText("flask==3.1.0\n")
        val changed = PythonDependencyManager.requirementsFingerprint(req)
        assertThat(PythonDependencyManager.depsMarkerMatches(site, changed)).isFalse()
    }

    @Test
    fun `legacy pack without marker never matches`() {
        val site = tmp.newFolder(".pypackages")
        File(site, "somepkg").mkdirs()
        File(site, "somepkg/__init__.py").writeText("")
        assertThat(PythonDependencyManager.hasInstalledPackages(site)).isTrue()
        assertThat(PythonDependencyManager.depsMarkerMatches(site, "whatever")).isFalse()
    }

    @Test
    fun `dependency identity covers nested requirements constraints and runtime ABI`() {
        val req = tmp.newFile("requirements.txt").apply { writeText("-r child.txt\n") }
        tmp.newFile("child.txt").writeText("--constraint=constraints.txt # pin package\nflask\n")
        val constraints = tmp.newFile("constraints.txt").apply { writeText("flask==3.0.0\n") }
        val original = PythonDependencyCache.fingerprint(req, "3.14", "arm64-v8a")
        assertThat(original).isNotNull()
        assertThat(PythonDependencyCache.fingerprint(req, "3.14", "x86_64")).isNotEqualTo(original)
        assertThat(PythonDependencyCache.fingerprint(req, "3.15", "arm64-v8a")).isNotEqualTo(original)

        constraints.writeText("flask==3.1.0\n")
        assertThat(PythonDependencyCache.fingerprint(req, "3.14", "arm64-v8a")).isNotEqualTo(original)
        constraints.delete()
        assertThat(PythonDependencyCache.fingerprint(req, "3.14", "arm64-v8a")).isNull()
    }

    @Test
    fun `successful reinstall replaces package contents and removes dropped dependencies`() {
        val site = tmp.newFolder(".pypackages")
        val stage = tmp.newFolder("stage")
        File(site, "flask.py").writeText("old")
        File(site, "removed.py").writeText("legacy")
        PythonDependencyCache.mark(site, "old")

        assertThat(PythonDependencyCache.replace(site, "new", stage) { packages ->
            assertThat(packages.list()).isEmpty()
            File(packages, "flask.py").writeText("new")
            true
        }).isTrue()

        assertThat(File(site, "flask.py").readText()).isEqualTo("new")
        assertThat(File(site, "removed.py").exists()).isFalse()
        assertThat(PythonDependencyCache.matches(site, "new")).isTrue()
        assertThat(stage.list()).isEmpty()
    }

    @Test
    fun `failed reinstall preserves the previous contents and fingerprint`() {
        val site = tmp.newFolder(".pypackages")
        val stage = tmp.newFolder("stage")
        File(site, "flask.py").writeText("old")
        PythonDependencyCache.mark(site, "old")

        assertThat(PythonDependencyCache.replace(site, "new", stage) { packages ->
            File(packages, "flask.py").writeText("partial")
            false
        }).isFalse()

        assertThat(File(site, "flask.py").readText()).isEqualTo("old")
        assertThat(PythonDependencyCache.matches(site, "old")).isTrue()
        assertThat(PythonDependencyCache.matches(site, "new")).isFalse()
        assertThat(stage.list()).isEmpty()
    }
}
