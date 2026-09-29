package com.webtoapp.core.apkbuilder

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Fixture provenance (issue #1115 P0 spike):
 *  - `arsc/base_7f.arsc`    – aapt2 link of a two-string app package (id 0x7f)
 *  - `arsc/feature_80.arsc` – aapt2 link --package-id 0x80 of a package holding
 *    strings `stack_marker`/`stack_other` + drawable `stack_dot` whose value is
 *    the file path `res/drawable-nodpi-v4/stack_dot.png`.
 *
 * The same graft output was installed on an emulator and resolved
 * `getString(0x80020000)`/`getDrawable(0x80010000)` correctly at runtime.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ArscPackageGrafterTest {

    private val grafter = ArscPackageGrafter()

    private fun fixture(name: String): ByteArray =
        javaClass.classLoader.getResourceAsStream("arsc/$name")!!.readBytes()

    @Test
    fun `graft appends stack package and remaps strings`() {
        val result = grafter.graft(
            baseArsc = fixture("base_7f.arsc"),
            stackArsc = fixture("feature_80.arsc"),
            resPathPrefix = "res/wtastack_test/"
        )

        val table = ArscPackageGrafter.ArscTable.parse(result.arsc)
        assertThat(table.packageCount).isEqualTo(2)
        assertThat(table.packages.map { it.id }).containsExactly(0x7f, 0x80)
        assertThat(table.packages[1].name).isEqualTo("com.spike.feature")

        // Every string value in the grafted package resolves through the
        // rebuilt global pool.
        val resolved = mutableListOf<String>()
        table.packages[1].forEachStringValue { _, idx -> resolved += table.globalStrings[idx] }
        assertThat(resolved).containsExactly(
            "res/wtastack_test/drawable-nodpi-v4/stack_dot.png",
            "STACK_OK_0x80",
            "SECOND_ENTRY"
        )

        // Callers use resPaths to copy stack res files into the output zip.
        assertThat(result.resPaths).containsExactly(
            "res/drawable-nodpi-v4/stack_dot.png",
            "res/wtastack_test/drawable-nodpi-v4/stack_dot.png"
        )

        // Base package strings are untouched.
        assertThat(table.globalStrings).contains("BASE_OK_0x7f")
    }

    @Test
    fun `graft rejects duplicate package id`() {
        val first = grafter.graft(
            baseArsc = fixture("base_7f.arsc"),
            stackArsc = fixture("feature_80.arsc"),
            resPathPrefix = "res/wtastack_test/"
        )
        assertThrows(IllegalArgumentException::class.java) {
            grafter.graft(first.arsc, fixture("feature_80.arsc"), "res/wtastack_test/")
        }
    }

    @Test
    fun `graft rejects app package id range`() {
        assertThrows(IllegalArgumentException::class.java) {
            grafter.graft(
                baseArsc = fixture("base_7f.arsc"),
                stackArsc = fixture("base_7f.arsc"), // 0x7f is not a stack package
                resPathPrefix = "res/wtastack_test/"
            )
        }
    }
}
