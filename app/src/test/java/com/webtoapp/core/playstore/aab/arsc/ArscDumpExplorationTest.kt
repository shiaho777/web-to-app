package com.webtoapp.core.playstore.aab.arsc

import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.zip.ZipFile

class ArscDumpExplorationTest {

    @Test
    fun `dump real shell-template arsc summary`() {
        val template = File("src/main/assets/template/webview_shell.apk")
        assumeTrue(
            "shell template not built — run ':app:syncShellTemplateApk' first",
            template.exists()
        )

        val arscBytes = ZipFile(template).use { zip ->
            val entry = zip.getEntry("resources.arsc")
                ?: error("resources.arsc missing")
            zip.getInputStream(entry).readBytes()
        }

        val table = ArscReader(arscBytes).read()

        println("=== ARSC dump for webview_shell.apk ===")
        println("Total bytes: ${arscBytes.size}")
        println("Value string pool: ${table.valueStringPool.size} entries")
        println("Packages: ${table.packages.size}")

        for (pkg in table.packages) {
            println("--- Package id=0x${"%02x".format(pkg.id)} name=${pkg.name} ---")
            println("  Type names (${pkg.typeNames.size}): ${pkg.typeNames.take(20)}${if (pkg.typeNames.size > 20) "..." else ""}")
            println("  Key names: ${pkg.keyNames.size}")
            println("  Type chunks: ${pkg.types.size}, type-spec chunks: ${pkg.typeSpecs.size}")

            val typeIds = pkg.types.map { it.typeId }.toSet().sorted()
            for (typeId in typeIds) {
                val typeName = pkg.typeNames.getOrNull(typeId - 1) ?: "?"
                val totalEntries = pkg.types
                    .filter { it.typeId == typeId }
                    .sumOf { it.entries.count { e -> e != null } }
                val configCount = pkg.types.count { it.typeId == typeId }
                println("    $typeName (id=$typeId): $totalEntries entries across $configCount config(s)")
            }

            val stringTypeId = pkg.typeNames.indexOf("string") + 1
            val appNameEntry = pkg.types
                .filter { it.typeId == stringTypeId }
                .flatMap { it.entries.filterNotNull() }
                .firstOrNull { it.name == "app_name" }
            if (appNameEntry != null) {
                val body = appNameEntry.body
                if (body is ArscEntryBody.Simple) {
                    val value = body.value
                    val text = if (value.dataType == 0x03) {
                        table.valueStringPool.getOrElse(value.data) { "<idx=${value.data}>" }
                    } else "<dataType=${value.dataType} data=${value.data}>"
                    println("  app_name = $text")
                } else {
                    println("  app_name = <bag>")
                }
            } else {
                println("  app_name NOT FOUND")
            }
        }
        println("=== End dump ===")
    }
}
