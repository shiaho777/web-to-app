package com.webtoapp.core.playstore.aab.arsc

import com.google.common.truth.Truth.assertThat
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class ArscRealTableTest {

    @Test
    fun `parse and convert real shell-template resources arsc`() {
        val template = File("src/main/assets/template/webview_shell.apk")
        assumeTrue(
            "shell template not built — run ':app:syncShellTemplateApk' first",
            template.exists()
        )

        val arscBytes = ZipFile(template).use { zip ->
            val entry = zip.getEntry("resources.arsc")
                ?: error("resources.arsc missing from shell template")
            zip.getInputStream(entry).readBytes()
        }

        val table = ArscReader(arscBytes).read()

        assertThat(table.packages).hasSize(1)
        val pkg = table.packages.single()
        assertThat(pkg.id).isEqualTo(0x7f)
        assertThat(pkg.name).contains("com.webtoapp")

        val typeNames = pkg.typeNames.toSet()
        assertThat(typeNames).contains("string")

        assertThat(typeNames.any { it == "mipmap" || it == "drawable" }).isTrue()

        assertThat(table.valueStringPool.size).isGreaterThan(10)

        val proto = ArscToProtoTable.convert(table)

        assertThat(proto.packageCount).isEqualTo(1)
        val protoPkg = proto.getPackage(0)
        assertThat(protoPkg.packageId.id).isEqualTo(0x7f)
        assertThat(protoPkg.packageName).contains("com.webtoapp")

        assertThat(protoPkg.typeCount).isAtLeast(1)
        for (t in protoPkg.typeList) {
            assertThat(t.entryCount).isGreaterThan(0)
        }

        val stringType = protoPkg.typeList.firstOrNull { it.name == "string" }
        assertThat(stringType).isNotNull()
        val hasAppName = stringType!!.entryList.any { it.name == "app_name" }
        assertThat(hasAppName).isTrue()
    }
}
